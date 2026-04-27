package com.reporadar.controller.api;

import com.reporadar.dto.TechnologyResponseDTO;
import com.reporadar.service.CategoryService;
import com.reporadar.service.TechnologyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/technologies")
public class TechnologyApiController {

    private final TechnologyService technologyService;

    public TechnologyApiController(TechnologyService technologyService) {
        this.technologyService = technologyService;
    }

    @GetMapping
    public List<TechnologyResponseDTO> getAll() {
        return technologyService.getAllCategories();
    }
}