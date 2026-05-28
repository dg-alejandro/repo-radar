package com.reporadar.repository;

import com.reporadar.entity.Project;
import com.reporadar.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> { //al extender JpaRepository heredamos un monton de metodos ya hechos
                                                                         //ahorrandonos escribir SQL. Añadimos Project(pues este es su repositorio)
                                                                         //y long, pues el tipo del id de tabla en la entidad Project

    boolean existsByRepositoryUrl(String repositoryUrl);//evitar duplicados al importar desde Github

    List<Project> findByAdministratorId(Long administratorId);//proyectos añadidos por un administrador en concreto

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);//en lugar de List<Project>, usamos Page<Project> junto con Pageable para paginar los resultados
                                                                        //sin paginación, una consulta podría devolver miles de filas de golpe(no en este caso, pero es lo mismo), lo que es lento e ineficiente
                                                                        //Pageable define qué trozo de resultados queremos: qué página y cuántos elementos por páginag
                                                                        //Page<Project> devuelve ese trozo junto con mas información: total de resultados, total de páginas, y si hay página siguiente

    Page<Project> findByStatusAndCategoriesId(ProjectStatus status, Pageable pageable, Long categoryId);

    Page<Project> findByStatusAndTechnologiesId(ProjectStatus status, Pageable pageable, Long technologyId);

    //busca proyectos publicados cuyo nombre o descripción contengan el texto indicado (sin distinguir mayúsculas)
    //se necesita status dos veces porque el OR parte la condicion en dos ramas independientes, cada rama necesita
    //su propio status porque sql evalua asi: WHERE status = ? AND name LIKE ?
    //OR status = ? AND description LIKE
    Page<Project> findByStatusAndNameContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
            ProjectStatus status1, String name,
            ProjectStatus status2, String description,
            Pageable pageable);

    Optional<Project> findByIdAndStatus(Long id, ProjectStatus status);//para buscar proyectos por id

    //busca proyectos aplicando los tres filtros a la vez. Cada filtro es opcional:
    //si llega null, se ignora gracias al patron (:param IS NULL OR <condicion>).
    //Usamos LEFT JOIN para que un proyecto sin categorias o tecnologias siga apareciendo
    //cuando esos filtros no se aplican, y DISTINCT para evitar filas duplicadas
    //cuando un proyecto tiene varias categorias o tecnologias.
    @Query("""
            SELECT DISTINCT p FROM Project p
            LEFT JOIN p.categories c
            LEFT JOIN p.technologies t
            WHERE p.status = :status
              AND (:q IS NULL
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:technologyId IS NULL OR t.id = :technologyId)
            """)
    Page<Project> searchPublished(
            @Param("status") ProjectStatus status,
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("technologyId") Long technologyId,
            Pageable pageable);
}
