package com.reporadar.service;

import com.reporadar.dto.CategoryResponseDTO;
import com.reporadar.dto.ProjectResponseDTO;
import com.reporadar.dto.TechnologyResponseDTO;
import com.reporadar.entity.Project;
import com.reporadar.entity.ProjectStatus;
import com.reporadar.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository){
        this.projectRepository=projectRepository;
    }

    //metodo que busca proyectos publicados aplicando los tres filtros a la vez.
    //Cada filtro es opcional: si llega null se ignora dentro de la query.
    //Asi una busqueda de texto no se "come" la categoria seleccionada.
    @Transactional
    public List<ProjectResponseDTO> getPublishedProjects(String q, Long categoryId, Long technologyId) {
        Pageable limit = PageRequest.of(0, 50);

        //normalizamos el texto: si viene vacio o solo espacios, lo tratamos como ausente
        String normalizedQ = (q != null && !q.isBlank()) ? q.trim() : null;

        List<Project> projects = projectRepository.searchPublished(
                ProjectStatus.PUBLISHED, normalizedQ, categoryId, technologyId, limit
        ).getContent();

        //map(this::toDto) convierte cada Project en su DTO
        return projects.stream().map(this::toDto).collect(Collectors.toList());
    }

    //Metodo que transforma un objeto project en un projectResponseDTO
    private ProjectResponseDTO toDto(Project project){

        //cogemos todas las categorias de un proyecto, y los convertimos en una "cinta transportadora", map seria el operario, que toma
        //cada elemento y le hace algun cambio, por ultimo con -collect recogemos todos los elementos transformados y los guarda en una
        //nueva list
        List<CategoryResponseDTO>categories=project.getCategories().stream().map(c-> new CategoryResponseDTO(c.getId(),
                c.getName())).collect(Collectors.toList());

        List<TechnologyResponseDTO> technologies = project.getTechnologies().stream()
                .map(t -> new TechnologyResponseDTO(t.getId(), t.getName()))
                .collect(Collectors.toList());

        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setRepositoryUrl(project.getRepositoryUrl());
        dto.setAuthor(project.getAuthor());
        dto.setStars(project.getStars());
        dto.setImportDate(project.getImportDate());
        dto.setCategories(categories);
        dto.setTechnologies(technologies);
        return dto;
    }

    //este metodo convierte un proyecto en especifico buscado por id(gracias a projectRepository) a projectDTO.
    @Transactional
    public ProjectResponseDTO getProjectById(Long id) {
        Project project = projectRepository.findByIdAndStatus(id, ProjectStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
        return toDto(project);
    }
}
