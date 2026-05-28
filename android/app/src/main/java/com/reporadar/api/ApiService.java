package com.reporadar.api;

import com.reporadar.model.AuthRequest;
import com.reporadar.model.AuthResponse;
import com.reporadar.model.Category;
import com.reporadar.model.Project;
import com.reporadar.model.Technology;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

//esta clase sirve para definir todos los endpoints de la api
public interface ApiService {

    //proyectos
    @GET("api/projects")
    Call<List<Project>> getProjects(
            @Query("q") String query,
            @Query("categoryId") Long categoryId,
            @Query("technologyId") Long technologyId
    );

    @GET("api/projects/{id}")
    Call<Project> getProjectById(@Path("id") Long id);

    //categorias y tecnologias
    @GET("api/categories")
    Call<List<Category>> getCategories();

    @GET("api/technologies")
    Call<List<Technology>> getTechnologies();

    //auth
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body AuthRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    //favoritos
    @GET("api/favorites")
    Call<List<Project>> getFavorites();

    @POST("api/favorites/{projectId}")
    Call<Void> addFavorite(@Path("projectId") Long projectId);

    @DELETE("api/favorites/{projectId}")
    Call<Void> removeFavorite(@Path("projectId") Long projectId);
}