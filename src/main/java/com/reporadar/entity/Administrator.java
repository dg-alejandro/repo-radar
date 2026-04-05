package com.reporadar.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="administrator")
public class Administrator {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable=false, length=150)
    private String name;

    @Column(name="email", nullable=false, unique=true, length=150)
    private String email;

    @Column(name="password", nullable=false, length=255)
    private String password;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt=LocalDateTime.now();
    }

    //relaciones

    @OneToMany(mappedBy = "administrator")
    private Set<Project> projects=new HashSet<>();

    public Administrator(){}

    //getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    //createAt no tiene setter pues esta fecha no se debe de poder modificar

    public Set<Project> getProjects() {return projects;}
}

