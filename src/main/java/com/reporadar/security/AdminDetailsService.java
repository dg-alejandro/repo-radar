package com.reporadar.security;

import com.reporadar.entity.Administrator;
import com.reporadar.repository.AdministratorRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminDetailsService implements UserDetailsService {

    private final AdministratorRepository administratorRepository;

    public AdminDetailsService(AdministratorRepository administratorRepository) {
        this.administratorRepository = administratorRepository;
    }

    @Override

    //spring security ejecuta esto una vez alguien intenta iniciar sesion
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Administrator admin = administratorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Admin no encontrado: " + email));

        //se devuelve un objeto user(nativo de spring), al cual se le pasa email, contraseña, y rol
        return new User(
                admin.getEmail(),
                admin.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))//los roles se meten en una lista, porque spring espera una
                                                                 //coleccion de roles o permisos
        );
    }
}