package com.reporadar.security;

import com.reporadar.entity.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    //con value inyectamos valores desde el archivo de configuracion(application.properties)
    @Value("${jwt.secret}")//cadena larga y aleatoria usada para firmar los tokens
    private String secret;

    @Value("${jwt.expiration}")//tiempo de vida del token en ms
    private long expiration;

    //este metodo convierte el texto plano del string secret en una llave criptografica real
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(AppUser user) {
        return Jwts.builder()
                .subject(user.getEmail())//guarda el identificador principal del usuario
                .issuedAt(new Date())//fecha de creacion
                .expiration(new Date(System.currentTimeMillis() + expiration))//cuando debe de dejar de funcionar el token
                .signWith(getSigningKey())//sella el token
                .compact();//construye el string final
    }

    //este metodo hace lo contrario a generateToken, comprueba que la firma sea valida y extrae el email que se guardo antes
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())//verifica que la firma sea autentica
                .build()
                .parseSignedClaims(token)//lee el contenido
                .getPayload()
                .getSubject();//retorna el email
    }

    //metodo que valida el token, intenta extraer el email, si el token expiro, fue manipulado o la ifrma es incorrecta, jwt
    //lanzara una excepcion
    public boolean isTokenValid(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
