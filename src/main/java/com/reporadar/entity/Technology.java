package com.reporadar.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="technology")
public class Technology {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable=false, unique=true, length=50)
    private String name;

    //relaciones

    @ManyToMany(mappedBy="technologies")
    private Set<Project> projects=new HashSet<>();

    public Technology(){}

    //getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<Project> getProjects() {return projects;}
}
