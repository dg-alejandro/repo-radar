package com.reporadar.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable=false, length=150)
    private String name;

    @Column(name="email", nullable=false, unique=true, length=150)
    private String email;

    @Column(name="password", nullable=false, length=255)
    private String password;

    @Column(name="registered_at", nullable=false)
    private LocalDateTime registeredAt;

    @PrePersist
    protected void onCreate(){
        registeredAt=LocalDateTime.now();
    }

    //relaciones

    @ManyToMany
    @JoinTable(
            name="user_favorite",
            joinColumns=@JoinColumn(name="user_id"),
            inverseJoinColumns=@JoinColumn(name="project_id")
    )

    private Set<Project> favorites=new HashSet<>();

    public AppUser(){}

    //getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    //registeredAt no tiene setter pues esta fecha no se debe de poder modificar

    public Set<Project> getFavorites() {return favorites;}
}
