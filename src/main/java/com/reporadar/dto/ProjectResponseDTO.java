package com.reporadar.dto;

import java.time.LocalDateTime;
import java.util.List;


//un dto(data transfer object), es un objeto que define exactamente que datos se envian entre capas del
//sistema, en este caso, de backend a cliente.(en el caso especifico de projects, el usuario no tiene que ver
//el estado de proyecto, el id del administrador que hizo la importacion, ni fecha de actualizacion)
public class ProjectResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String repositoryUrl;
    private String author;
    private int stars;
    private LocalDateTime importDate;
    //usamos una lista con los dto de technology y category para que la app android reciba tanto el nombre
    //como su id, muy util porque la app necesita los ids para construir filtros y hacer busquedas.
    private List<CategoryResponseDTO> categories;
    private List<TechnologyResponseDTO> technologies;

    public ProjectResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public LocalDateTime getImportDate() { return importDate; }
    public void setImportDate(LocalDateTime importDate) { this.importDate = importDate; }

    public List<CategoryResponseDTO> getCategories() { return categories; }
    public void setCategories(List<CategoryResponseDTO> categories) { this.categories = categories; }

    public List<TechnologyResponseDTO> getTechnologies() { return technologies; }
    public void setTechnologies(List<TechnologyResponseDTO> technologies) { this.technologies = technologies; }
}