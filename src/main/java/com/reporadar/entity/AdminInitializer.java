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
        if (administratorRepository.count() == 0) {
            Administrator admin = new Administrator();
            admin.setName("Admin");
            admin.setEmail("admin@reporadar.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            administratorRepository.save(admin);
            System.out.println(">>> Admin creado correctamente");
        }
    }
}