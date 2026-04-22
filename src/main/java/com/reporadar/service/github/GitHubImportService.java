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

import java.time.LocalDateTime;
import java.util.Map;

@Service//esto le dice a spring que esta clase es un componente de la capa de logica de negocio, pudiendose inyectar donde se necesite
public class GitHubImportService {

    private final RestTemplate restTemplate;//es el cliente http de spring, permite hacer peticiones GET,POST,etc a
                                            //urls externas

    //usamos los repositorios porque despues de obtener datos desde github, se persisten en base de datos. Necesitamos
    //el de project para guardar el proyecto, technology para buscar o crear la tecnologia, y administrator
    //para registrar quien hizo la importacion del proyecto
    private final ProjectRepository projectRepository;
    private final TechnologyRepository technologyRepository;
    private final AdministratorRepository administratorRepository;

    //cuando Spring crea el servicio, mira el constructor y los busca automaticamente en su contenedor, ahorrandonos tener
    //que declarar un nuevo objeto GitHubImportService cada vez que lo necesitemos
    public GitHubImportService(RestTemplate restTemplate, ProjectRepository projectRepository, TechnologyRepository technologyRepository,
                               AdministratorRepository administratorRepository) {
        this.restTemplate = restTemplate;
        this.projectRepository = projectRepository;
        this.technologyRepository = technologyRepository;
        this.administratorRepository = administratorRepository;
    }

    private static final String API_BASE="https://api.github.com/repos/";//constante con la url base de la api de github
                                                                         //todos los repos siguen el mismo patron
    //punto de entrada del servicio
    public Project importFromUrl(String repoUrl, Long administratorId){
        Map<String,Object> data=fetchRepoData(repoUrl);//llama a la api y devuelve los datos de repo como mapa(clave-valor)
        Administrator administrator=administratorRepository.findById(administratorId)
                .orElseThrow (()-> new RuntimeException("Administrador no encontrado: " +administratorId));//busca el administrados en la bd, si no lo encuentra,
                                                                                                           //lanza una excepcion
        //crea el proyecto y se guarda en la base de datos
        Project project=buildProject(data,administrator);
        return projectRepository.save(project);
    }

    private Map<String,Object> fetchRepoData(String repoUrl){
        String path = extractRepoPath(repoUrl);

        HttpHeaders headers=new HttpHeaders();
        headers.set("Accept","application/vnd.github+json");//creamos un objeto de cabeceras http, y se añade accept. Con esta cabecera se le dice a github en que formato
                                                            //queremos la respuesta. application/vnd.github+json es el formato oficial de github

        //aqui ocurre la llamada http real:
        ResponseEntity<Map> response=restTemplate.exchange(API_BASE+path, HttpMethod.GET, new HttpEntity<>(headers),Map.class);
        //API_BASE+path:construye la url completa
        //httpmethod.get: tipo de peticion http(get,solo leemos datos)
        //new Http<>Entity<headers> empaqueta las cabeceras en un objeto que resttemplate entiende
        //le decimos a spring en que tipo deserializamos la respuesta JSON(Map.class)
        return response.getBody();
    }

    private Project buildProject(Map<String,Object>data,Administrator administrator){//recibe el map con datos de github y un administrador traido de la bd
        //extraemos datos a partir del json(map) devuelto por github
        String name=(String) data.get("name");//casteamos a string donde toque, porque json devuelve un Object, y hay que especificar que tipo de variable usamos
        String description=(String) data.get("description");
        String repositoryUrl=(String) data.get("html_url");
        String author=(String)((Map<?,?>) data.get("owner")).get("login");//owner no es un valor simple, sino un map anidado dentro del map principal
                                                                          //con data.get("owner") saca el valor asociado, y devuelve un object,
                                                                          //casteamos ese object a map(las "?" indican que no se sabe el tipo exacto de las claves y valores
                                                                          //ya dentro del map, get("login") consigue el valor de la clave login, y devuelve un object
                                                                          //el (String) que envuelve toda la logica anterior, permite convertir el object a string, para poder asignarlo a author
        int stars=((Number) data.get("stargazers_count")).intValue();//jackson, la libreria que convierte json a java, decide internamente si deserializa un numero como integer o como long
                                                                     //nos cubrimos de una posible excepcion usando Number, la clase padre de Integer y Long
        String language=(String) data.get("language");

        //creamos la instancia project vacia, y le asignamos los campos extraidos del json
        Project project=new Project();
        project.setName(name);
        project.setDescription(description);
        project.setRepositoryUrl(repositoryUrl);
        project.setAuthor(author);
        project.setStars(stars);
        project.setImportDate(LocalDateTime.now());//cogemos la fecha en el momento de la importacion
        project.setStatus(ProjectStatus.HIDDEN);//todo proyecto comienza estando oculto, es el administrador el que lo vuelve visible
        project.setAdministrator(administrator);

        //primero comprobamos que el lenguaje no sea null ni una cadena vacia(usamos is blank porque tambien detecta espacios)
        if(language!=null && !language.isBlank()){
            Technology technology=resolveOrCreateTechnology(language);
            project.getTechnologies().add(technology);
        }

        return project;
    }

    //metodo que recibe el nombre del lenguaje detectado por github y devuelve una entidad technology, ya sea existente o recien creada
    private Technology resolveOrCreateTechnology(String name){
        return technologyRepository.findByName(name).orElseGet(()->{
                                                        Technology t = new Technology();
                                                        t.setName(name);
                                                        return technologyRepository.save(t);
                                                    });
        //con findByName buscamos si existe alguna fila en technology con con ese nombre, devuelve un Optional<Technology>, que es un contenedor que puede tener objeto
        //dentro o estar vacio(asi nos ahorramos tener que gestionar null), si optional tiene valor, .orElseGet devuelve dicho valor, y si optional esta vacio, se crea
        //una nueva tecnologia
    }

    //metodo que recibe la url completa del repositorio a importar y devuelve solo la parte que necesita la api(usuario/nombre-repo)
    private String extractRepoPath(String repoUrl){
        String cleaned=repoUrl.trim().replaceAll("/$","");//devuelve la cadena "limpia", con trim eliminamos espacios en blanco, y con replaceAll
                                                                           //eliminamos la barra del final(/$ le dice a replace all que elimine "/" si esta al final)
        int idx=cleaned.indexOf("github.com/");//devuelve la posicion en la que empieza el texto "github.com/"
        if(idx==-1){
            throw new IllegalArgumentException("Url de github no valida: " +repoUrl);//excepcion si no se encuentra github, se pudo haber introducido una url que
                                                                                     //no sea de github
        }
        return cleaned.substring(idx+ "github.com/".length());//esto extrae la parte del string que viene despues de github.com/, a lo cual le sumamos idx
                                                                        //con substring devolvemos una cadena que va desde esa posicion hasta el final, que sera lo
                                                                        //que concatenamos con API_BASE en fetchRepoData()
    }

}
