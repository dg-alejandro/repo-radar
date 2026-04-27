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

    //metodo que busca proyectos publicados
    @Transactional
    public List<ProjectResponseDTO> getPublishedProjects(String q,Long categoryId,Long technologyId){
        Pageable limit= PageRequest.of(0,50);//limitamos para conseguir 50 resultados como maximo
        List<Project> projects;

        //decidimos a que repositorio llamar segun los parametros que se hayan enviado
        //si q tiene contenido, se buscan proyectos publicados cuyo nombre o descripcion contengan ese texto
        if(q!=null && !q.isBlank()){
            projects = projectRepository.findByStatusAndNameContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                    ProjectStatus.PUBLISHED,q,ProjectStatus.PUBLISHED,q,limit).getContent();

        }
        //si no hay texto pero si id de categoria, se buscan proyectos publicados que pertenezcan a dicha categoria
        else if(categoryId!=null){
            projects=projectRepository.findByStatusAndCategoriesId(ProjectStatus.PUBLISHED,limit,categoryId).getContent();
        }
        //si no hay texto pero si id de tecnologia, se buscan proyectos publicados que pertenezcan a dicha tecnologia
        else if(technologyId!=null){
            projects=projectRepository.findByStatusAndTechnologiesId(ProjectStatus.PUBLISHED,limit,technologyId).getContent();
        }
        //si ninguno de los filtros se envio, se devuelven los ultimos 50 proyectos publicados.
        else{
            projects=projectRepository.findByStatus(ProjectStatus.PUBLISHED,limit).getContent();
        }

        //map(this::toDto) le dice a java, para cada proyecto que te "llegue", aplica el metodo toDto que esta definido, con
        //.collect guardamos los elementos transformados en una nueva lista, que es lo que se retorna en el metodo
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
