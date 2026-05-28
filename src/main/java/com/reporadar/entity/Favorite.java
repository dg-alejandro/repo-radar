package com.reporadar.entity;

import com.reporadar.entity.AppUser;
import com.reporadar.entity.Project;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_favorite")
public class Favorite {

    @Embeddable
    public static class FavoriteId implements Serializable {

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "project_id")
        private Long projectId;

        public FavoriteId() {}

        public FavoriteId(Long userId, Long projectId) {
            this.userId = userId;
            this.projectId = projectId;
        }

        public Long getUserId() { return userId; }
        public Long getProjectId() { return projectId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FavoriteId that)) return false;
            return Objects.equals(userId, that.userId) && Objects.equals(projectId, that.projectId);
        }

        @Override
        public int hashCode() { return Objects.hash(userId, projectId); }
    }

    @EmbeddedId
    private FavoriteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    public Favorite() {}

    public Favorite(AppUser user, Project project) {
        this.user = user;
        this.project = project;
        this.id = new FavoriteId(user.getId(), project.getId());
    }

    public FavoriteId getId() { return id; }
    public AppUser getUser() { return user; }
    public Project getProject() { return project; }
    public LocalDateTime getAddedAt() { return addedAt; }
}