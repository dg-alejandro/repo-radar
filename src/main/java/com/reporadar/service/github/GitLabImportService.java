package com.reporadar.service.github;

import com.reporadar.entity.Administrator;
import com.reporadar.entity.Project;
import com.reporadar.entity.ProjectStatus;
import com.reporadar.entity.Technology;
import com.reporadar.repository.AdministratorRepository;
import com.reporadar.repository.ProjectRepository;
import com.reporadar.repository.TechnologyRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class GitLabImportService {

    private final RestTemplate restTemplate;
    private final ProjectRepository projectRepository;
    private final TechnologyRepository technologyRepository;
    private final AdministratorRepository administratorRepository;

    public GitLabImportService(RestTemplate restTemplate, ProjectRepository projectRepository,
                               TechnologyRepository technologyRepository, AdministratorRepository administratorRepository) {
        this.restTemplate = restTemplate;
        this.projectRepository = projectRepository;
        this.technologyRepository = technologyRepository;
        this.administratorRepository = administratorRepository;
    }

    private static final String API_BASE = "https://gitlab.com/api/v4/projects/";

    public Project importFromUrl(String repoUrl, Long administratorId) {
        String path = extractRepoPath(repoUrl); //extraemos el path una sola vez y lo reutilizamos

        Map<String, Object> data = fetchRepoData(path);

        Administrator administrator = administratorRepository.findById(administratorId)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado: " + administratorId));

        Project project = buildProject(data, administrator, path); //le pasamos el path para la segunda llamada
        return projectRepository.save(project);
    }

    private Map<String, Object> fetchRepoData(String path) { //recibe el path ya extraído, no la url completa
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        String encodedPath = path.replace("/", "%2F");
        URI uri = URI.create(API_BASE + encodedPath);

        ResponseEntity<Map> response = restTemplate.exchange(
                uri, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        return response.getBody();
    }

    //segunda llamada a la api para obtener el lenguaje principal, mientras que github lo devuelve directamente en el endpoint principal,
    //con gitlab nos hace falta una peticion adicional
    private String fetchLanguage(String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        String encodedPath = path.replace("/", "%2F");
        URI uri = URI.create(API_BASE + encodedPath + "/languages"); //URI.create evita que RestTemplate re-encodee el path

        ResponseEntity<Map> response = restTemplate.exchange(
                uri, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        Map<String, Object> languages = response.getBody();
        if (languages == null || languages.isEmpty()) {
            return null;
        }
        //gitlab devuelve { "Java": 87.5, "Shell": 12.5 }, cogemos la primera clave (la de mayor porcentaje)
        return (String) languages.keySet().iterator().next();
    }

    //cambian los nombres de los campos json con respecto a github
    private Project buildProject(Map<String, Object> data, Administrator administrator, String path) {
        String name = (String) data.get("name");
        String description = (String) data.get("description");
        String repositoryUrl = (String) data.get("web_url");
        String author = (String) ((Map<?, ?>) data.get("namespace")).get("path");
        int stars = ((Number) data.get("star_count")).intValue();
        String language = fetchLanguage(path); //llamada real en lugar de null

        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setRepositoryUrl(repositoryUrl);
        project.setAuthor(author);
        project.setStars(stars);
        project.setImportDate(LocalDateTime.now());
        project.setStatus(ProjectStatus.HIDDEN);
        project.setAdministrator(administrator);

        if (language != null && !language.isBlank()) {
            Technology technology = resolveOrCreateTechnology(language);
            project.getTechnologies().add(technology);
        }

        return project;
    }

    private Technology resolveOrCreateTechnology(String name) {
        return technologyRepository.findByName(name).orElseGet(() -> {
            Technology t = new Technology();
            t.setName(name);
            return technologyRepository.save(t);
        });
    }

    private String extractRepoPath(String repoUrl) {
        String cleaned = repoUrl.trim().replaceAll("/$", "");
        int idx = cleaned.indexOf("gitlab.com/");

        if (idx == -1) {
            throw new IllegalArgumentException("Url de GitLab no válida: " + repoUrl);
        }

        return cleaned.substring(idx + "gitlab.com/".length()); //sin encoding, lo hacemos en cada metodo con URI.create
    }
}