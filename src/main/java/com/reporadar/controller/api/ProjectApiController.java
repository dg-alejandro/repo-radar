package com.reporadar.controller.api;

import com.reporadar.dto.ProjectResponseDTO;
import com.reporadar.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController//le decimos a spring que esta clase es un controlador REST, en vez de controller(que devuelve vistas html via thymeleaf)
               //devuelve datos directamente, esta clase nos servira para comunicarnos y enviar proyectos a la app movil
@RequestMapping("/api/projects")//indica que todas las rutas de este controlador empiezan por /api/projects
public class ProjectApiController {

    private final ProjectService projectService;

    public ProjectApiController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping//este metodo responde a GET /api/projects y devuelve un responseEntity, que es la respuesta http completa
    public ResponseEntity<List<ProjectResponseDTO>> getProjects(

            //todos los filtros son opcionales
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long technologyId) {

        //devolvemos una lista de dtos para no enviar directamente el objeto de la base de datos, enviando solamente
        //la informacion que necesita la app movil
        List<ProjectResponseDTO> projects = projectService.getPublishedProjects(q, categoryId, technologyId);
        return ResponseEntity.ok(projects);
    }

    //Endpoint "/api/projects/{id}, para buscar proyectos por su id.
    @GetMapping("/{id}")//cuando alguien navegue a dicha direccion, el id del proyecto se guardara en Long id(gracias a @PathVariable)
    public ProjectResponseDTO getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }
}