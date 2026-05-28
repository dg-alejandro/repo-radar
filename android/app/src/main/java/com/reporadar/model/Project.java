package com.reporadar.model;

import java.time.LocalDateTime;
import java.util.List;

public class Project {
    private Long id;
    private String name;
    private String description;
    private String repositoryUrl;
    private String author;
    private Integer stars;
    private LocalDateTime importDate;
    private List<Category> categories;
    private List<Technology> technologies;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public String getAuthor() { return author; }
    public Integer getStars() { return stars; }

    public LocalDateTime getImportDate(){return importDate;}
    public List<Category> getCategories() { return categories; }
    public List<Technology> getTechnologies() { return technologies; }
}