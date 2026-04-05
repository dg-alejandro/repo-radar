package com.reporadar.repository;

import com.reporadar.entity.Category;
import com.reporadar.entity.Technology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechnologyRepository extends JpaRepository<Technology,Long> {

    Optional<Technology> findByName(String name);

    boolean existsByName(String name);
}


