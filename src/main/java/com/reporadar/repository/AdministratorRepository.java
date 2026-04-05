package com.reporadar.repository;

import com.reporadar.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator,Long> {

    Optional<Administrator> findByEmail(String email);//ponemos optional porque dicho email puede no estar vinculado a ningun admin, entonces
                                                      //optional puede contener un admin o simplemente estar vacio
    boolean existsByEmail(String email);
}
