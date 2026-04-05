package com.reporadar.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity //le decimos a JPA que esta clase representa una tabla de nuestra bd
@Table(name="project")//le decimos el nombre exacto de la tabla

public class Project {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)//le indicamos a JPA que mysql genera el valor con auto_increment
    private Long id;

    @Column(name="repository_url", nullable=false, unique=true, length=250)//indicamos a que columna sql corresponde esta variable.Además indicamos atributos como
                                                                           //not null, unique etc, coincidiendo con la linea sql del campo a encontrar
    private String repositoryUrl;

    @Column(name="name", nullable=false, length=150)//solo haria falta el campo name="" si el nombre de la variable java y el campo sql difieren. En este caso no
                                                    //difieren, pero decido añadirlo por buenas prácticas. Aun asi, jpa sabria que la variable java name "apunta"
                                                    //al campo name de la bd, pues son iguales
    private String name;

    @Column(name="description", columnDefinition="TEXT")
    private String description;

    @Column(name="author", nullable=false, length=100)
    private String author;

    @Column(name="stars", nullable=false)
    private int stars=0;//la linea sql indica default 0

    @Column(name="import_date", nullable=false, updatable=false)//el updatable le dice a JPA que no incluya import_date en ningun UPDATE, pues dicha fecha no puede cambiar
    private LocalDateTime importDate;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)//indicamos el tipo de enumerado
    @Column(name="status", nullable=false)
    private ProjectStatus status=ProjectStatus.HIDDEN;//Creamos variable del enumerado que tenemos creado, ademas le damos un valor inicial que corresponda a su valor default en sql

    @PrePersist//prepersist le dice a jpa que antes de ejecutar el insert, ejecute este metodo
    protected void onCreate(){
        this.importDate=LocalDateTime.now();//añadimos esta instruccion pues en sql el default de import_date es la fecha actual
                                            //lo hacemos asi y no asignamos el valor .now() en la creacion de la variable, para que se refleje el momento exacto de
                                            //persistencia, no el de la construccion del objeto java
    }

    //relaciones

    @ManyToMany//le decimos a JPA que es una relacion muchos a muchos(un proyecto puede tener varias categorias y una categoria pertenecer a varios proyectos)
    @JoinTable(
            name="project_category",//indicamos nombre de tabla intermedia en la bd
            joinColumns=@JoinColumn(name="project_id"),//indicamos cual es el campo de la tabla intermedia(project_category)que identifica a Project
            inverseJoinColumns=@JoinColumn(name="category_id")//hacemos lo mismo pero "hacia el otro lado", identificando a Category
    )
    private Set<Category> categories=new HashSet<>();//decido usar set y no list porque no tendria sentido tener duplicados en una relacion n:m
                                                       //uso este set para poder leer y modificar las categorias de un proyecto

    @ManyToMany
    @JoinTable(
            name="project_technology",
            joinColumns=@JoinColumn(name="project_id"),
            inverseJoinColumns=@JoinColumn(name="technology_id")
    )
    private Set<Technology> technologies=new HashSet<>();

    @ManyToMany(mappedBy="favorites")
    private Set<AppUser> favoritedBy=new HashSet<>();

    @ManyToOne//la diferencia con manytomany es que no tenemos tabla intermedia, osea que la relacion se resuelve con una fk directamente
    @JoinColumn(name="administrator_id")
    private Administrator administrator;//hay un solo administrador por proyecto

    public Project(){} //constructor vacio

    //getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public LocalDateTime getImportDate() { return importDate; }
    public void setImportDate(LocalDateTime importDate) { this.importDate = importDate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public Set<Category> getCategories() {return categories;}

    public Set<Technology> getTechnologies() {return technologies;}

    public Set<AppUser> getFavoritedBy() {return favoritedBy;}

    public Administrator getAdministrator() {return administrator;}

    public void setAdministrator(Administrator administrator) {this.administrator = administrator;}
}
