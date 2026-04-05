package com.reporadar.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="category")
public class Category {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable=false, unique=true, length=50)
    private String name;

    //relaciones

    @ManyToMany(mappedBy="categories")//indicamos el lado inverso de su relacion
    private Set<Project> projects=new HashSet<>();//lo usare para por ejemplo encontrar que proyectos tienen x categoria

    public Category(){}

    //getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<Project> getProjects() {return projects;}
}
