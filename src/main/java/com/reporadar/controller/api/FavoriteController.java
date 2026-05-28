package com.reporadar.controller.api;

import com.reporadar.dto.CategoryResponseDTO;
import com.reporadar.dto.ProjectResponseDTO;
import com.reporadar.dto.TechnologyResponseDTO;
import com.reporadar.entity.AppUser;
import com.reporadar.entity.Project;
import com.reporadar.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<ProjectResponseDTO> getFavorites(
            @AuthenticationPrincipal AppUser user) {

        List<Project> projects = favoriteService.getFavorites(user.getEmail());
        return projects.stream()
                .map(project -> {
                    ProjectResponseDTO dto = new ProjectResponseDTO();
                    dto.setId(project.getId());
                    dto.setName(project.getName());
                    dto.setDescription(project.getDescription());
                    dto.setRepositoryUrl(project.getRepositoryUrl());
                    dto.setAuthor(project.getAuthor());
                    dto.setStars(project.getStars());
                    dto.setImportDate(project.getImportDate());

                    List<CategoryResponseDTO> categories = project.getCategories().stream()
                            .map(c -> new CategoryResponseDTO(c.getId(), c.getName()))
                            .collect(Collectors.toList());

                    List<TechnologyResponseDTO> technologies = project.getTechnologies().stream()
                            .map(t -> new TechnologyResponseDTO(t.getId(), t.getName()))
                            .collect(Collectors.toList());

                    dto.setCategories(categories);
                    dto.setTechnologies(technologies);
                    return dto;
                })
                .toList();
    }

    @PostMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFavorite(
            @PathVariable Long projectId,
            @AuthenticationPrincipal AppUser user) {

        favoriteService.addFavorite(user.getEmail(), projectId);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(
            @PathVariable Long projectId,
            @AuthenticationPrincipal AppUser user) {

        favoriteService.removeFavorite(user.getEmail(), projectId);
    }
}