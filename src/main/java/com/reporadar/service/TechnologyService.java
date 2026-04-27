package com.reporadar.service;

import com.reporadar.dto.TechnologyResponseDTO;
import com.reporadar.repository.TechnologyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnologyService {

    private final TechnologyRepository technologyRepository;

    public TechnologyService(TechnologyRepository technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    public List<TechnologyResponseDTO> getAllCategories() {
        return technologyRepository.findAll().stream()
                .map(c -> new TechnologyResponseDTO(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }
}
