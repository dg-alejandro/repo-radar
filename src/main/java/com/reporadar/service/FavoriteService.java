package com.reporadar.service;

import com.reporadar.entity.AppUser;
import com.reporadar.entity.Favorite;
import com.reporadar.entity.Project;
import com.reporadar.entity.*;
import com.reporadar.entity.Favorite.FavoriteId;
import com.reporadar.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AppUserRepository  appUserRepository;
    private final ProjectRepository  projectRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           AppUserRepository appUserRepository,
                           ProjectRepository projectRepository) {
        this.favoriteRepository = favoriteRepository;
        this.appUserRepository  = appUserRepository;
        this.projectRepository  = projectRepository;
    }

    public List<Project> getFavorites(String email) {
        AppUser user = getUserByEmail(email);
        return favoriteRepository.findByUserId(user.getId())
                .stream()
                .map(Favorite::getProject)
                .toList();
    }

    public void addFavorite(String email, Long projectId) {
        AppUser user    = getUserByEmail(email);
        Project project = getProject(projectId);

        FavoriteId favId = new FavoriteId(user.getId(), project.getId());
        if (!favoriteRepository.existsById(favId)) {
            favoriteRepository.save(new Favorite(user, project));
        }
    }

    public void removeFavorite(String email, Long projectId) {
        AppUser user = getUserByEmail(email);
        FavoriteId favId = new FavoriteId(user.getId(), projectId);
        if (favoriteRepository.existsById(favId)) {
            favoriteRepository.deleteById(favId);
        }
    }

    private AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
    }
}