package com.reporadar.repository;

import com.reporadar.entity.Favorite;
import com.reporadar.entity.Favorite.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    List<Favorite> findByUserId(Long userId);

    boolean existsById(FavoriteId id);
}