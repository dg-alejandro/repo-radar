package com.reporadar.service;

import com.reporadar.entity.AppUser;
import com.reporadar.repository.AppUserRepository;
import com.reporadar.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

//clase encargada del registro y autenticacion de usuarios
@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    //metodo que se encarga del registro de un nuevo usuario
    public String register(String email, String password) {
        //revisa si el email ya esta registrado
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
        }
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);//creamos un nuevo usuario con su email y su contraseña hasheada
        return jwtService.generateToken(user);
    }

    //metodo que se encarga del logueo de un usuario, busca por email y comprueba que la contraseña coincide con la que
    //esta guardada en la base de datos(la cifrada)
    public String login(String email, String password) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }
        return jwtService.generateToken(user);
    }
}
