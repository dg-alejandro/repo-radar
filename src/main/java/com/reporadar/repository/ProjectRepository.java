package com.reporadar.repository;

import com.reporadar.entity.Project;
import com.reporadar.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> { //al extender JpaRepository heredamos un monton de metodos ya hechos
                                                                         //ahorrandonos escribir SQL. Añadimos Project(pues es su repositorio)
                                                                         //y long, pues el tipo del id de tabla en la entidad Project

    boolean existsByRepositoryUrl(String repositoryUrl);//evitar duplicados al importar desde Github

    List<Project> findByAdministratorId(Long administratorId);//proyectos añadidos por un administrador en concreto

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);//en lugar de List<Project>, usamos Page<Project> junto con Pageable para paginar los resultados.
                                                                        //sin paginación, una consulta podría devolver miles de filas de golpe(no en este caso, pero es lo mismo), lo que es lento e ineficiente.
                                                                        //Pageable define qué trozo de resultados queremos: qué página y cuántos elementos por página.
                                                                        //Page<Project> devuelve ese trozo junto con mas información: total de resultados, total de páginas, y si hay página siguiente.

    Page<Project> findByStatusAndCategoriesId(ProjectStatus status, Pageable pageable, Long categoryId);

    Page<Project> findByStatusAndTechnologiesId(ProjectStatus status, Pageable pageable, Long technologyId);

}
