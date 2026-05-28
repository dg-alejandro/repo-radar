package com.reporadar.entity;

import com.reporadar.entity.Administrator;
import com.reporadar.repository.AdministratorRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer {

    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(AdministratorRepository administratorRepository,
                            PasswordEncoder passwordEncoder) {
        this.administratorRepository = administratorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        if (administratorRepository.findByEmail("admin@prueba.es").isEmpty()) {
            try {
                Administrator admin = new Administrator();
                admin.setName("Admin");
                admin.setEmail("admin@prueba.es");
                admin.setPassword(passwordEncoder.encode("1234"));
                administratorRepository.save(admin);
                System.out.println(">>> Admin creado correctamente");
            } catch (Exception e) {
                System.out.println(">>> Error al crear admin: " + e.getMessage());
            }
        } else {
            System.out.println(">>> Admin ya existe");
        }
    }
}