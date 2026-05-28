package com.reporadar.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.reporadar.MainActivity;
import com.reporadar.R;
import com.reporadar.RetrofitClient;
import com.reporadar.api.ApiService;
import com.reporadar.model.Category;
import com.reporadar.model.Project;
import com.reporadar.model.Technology;
import com.reporadar.session.SessionManager;
import com.reporadar.ui.auth.LoginActivity;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Detalle de un proyecto. Recibe el id como extra.
 *
 * Si el usuario tiene sesion activa, descargamos sus favoritos para saber si
 * este proyecto ya esta marcado, y mostramos el boton "anadir" o "quitar".
 * Si no tiene sesion, el boton lleva a la pantalla de login.
 */
public class ProjectDetailActivity extends AppCompatActivity {

    private ApiService publicApi;
    private SessionManager sessionManager;

    private ProgressBar progress;
    private View content;
    private TextView tvName, tvAuthor, tvStars, tvDescription, tvCategories, tvTechnologies;
    private Button btnOpenRepo, btnFavorite;

    private long projectId;
    private Project loadedProject;
    private boolean isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle");
        }

        publicApi = RetrofitClient.getInstance().create(ApiService.class);
        sessionManager = SessionManager.getInstance(this);

        progress = findViewById(R.id.progress);
        content = findViewById(R.id.content);
        tvName = findViewById(R.id.tvName);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvStars = findViewById(R.id.tvStars);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategories = findViewById(R.id.tvCategories);
        tvTechnologies = findViewById(R.id.tvTechnologies);
        btnOpenRepo = findViewById(R.id.btnOpenRepo);
        btnFavorite = findViewById(R.id.btnFavorite);

        projectId = getIntent().getLongExtra(MainActivity.EXTRA_PROJECT_ID, -1L);
        if (projectId == -1L) {
            Toast.makeText(this, "Proyecto no valido", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnFavorite.setOnClickListener(v -> onFavoriteClick());

        loadProject();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadProject() {
        showLoading(true);
        publicApi.getProjectById(projectId).enqueue(new Callback<Project>() {
            @Override
            public void onResponse(@NonNull Call<Project> call, @NonNull Response<Project> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadedProject = response.body();
                    renderProject(loadedProject);
                    if (sessionManager.isLoggedIn()) {
                        refreshFavoriteState();
                    } else {
                        showLoading(false);
                        btnFavorite.setText("Inicia sesion para guardar favoritos");
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(ProjectDetailActivity.this,
                            "No se pudo cargar el proyecto", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Project> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(ProjectDetailActivity.this,
                        "Error de conexion: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /**
     * Descarga la lista de favoritos del usuario y comprueba si este proyecto
     * esta dentro. No hay endpoint dedicado de "es favorito?", asi que se
     * resuelve con esa llamada. La lista de un usuario nunca es enorme.
     */
    private void refreshFavoriteState() {
        ApiService authApi = RetrofitClient
                .getAuthenticatedInstance(this)
                .create(ApiService.class);

        authApi.getFavorites().enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(@NonNull Call<List<Project>> call,
                                   @NonNull Response<List<Project>> response) {
                isFavorite = false;
                if (response.code() == 401) {
                    handleUnauthorized();
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    for (Project p : response.body()) {
                        if (p != null && Objects.equals(p.getId(), projectId)) {
                            isFavorite = true;
                            break;
                        }
                    }
                }
                updateFavoriteButton();
                showLoading(false);
            }

            @Override
            public void onFailure(@NonNull Call<List<Project>> call, @NonNull Throwable t) {
                isFavorite = false;
                updateFavoriteButton();
                showLoading(false);
            }
        });
    }

    private void renderProject(Project p) {
        tvName.setText(p.getName());
        tvAuthor.setText("Autor: " + p.getAuthor());
        tvStars.setText("★ " + (p.getStars() == null ? 0 : p.getStars()) + " stars");

        String desc = p.getDescription();
        tvDescription.setText((desc == null || desc.isEmpty()) ? "(sin descripcion)" : desc);

        tvCategories.setText(joinCategoryNames(p.getCategories()));
        tvTechnologies.setText(joinTechnologyNames(p.getTechnologies()));

        btnOpenRepo.setOnClickListener(v -> openRepositoryUrl());
    }

    private String joinCategoryNames(List<Category> items) {
        if (items == null || items.isEmpty()) return "(sin categorias)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i).getName());
            if (i < items.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private String joinTechnologyNames(List<Technology> items) {
        if (items == null || items.isEmpty()) return "(sin tecnologias)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i).getName());
            if (i < items.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private void openRepositoryUrl() {
        if (loadedProject == null || loadedProject.getRepositoryUrl() == null) return;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(loadedProject.getRepositoryUrl())));
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show();
        }
    }

    private void onFavoriteClick() {
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        ApiService authApi = RetrofitClient
                .getAuthenticatedInstance(this)
                .create(ApiService.class);

        Call<Void> call = isFavorite
                ? authApi.removeFavorite(projectId)
                : authApi.addFavorite(projectId);

        final boolean wasFavorite = isFavorite;
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.code() == 401) {
                    handleUnauthorized();
                    return;
                }
                if (response.isSuccessful()) {
                    isFavorite = !wasFavorite;
                    updateFavoriteButton();
                    Toast.makeText(ProjectDetailActivity.this,
                            isFavorite ? "Anadido a favoritos" : "Quitado de favoritos",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProjectDetailActivity.this,
                            "No se pudo actualizar el favorito (HTTP " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ProjectDetailActivity.this,
                        "Error de conexion: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    //si el backend responde 401, el token guardado ya no sirve. Limpiamos sesion
    //y mandamos al usuario al login para que vuelva a entrar.
    private void handleUnauthorized() {
        sessionManager.clear();
        Toast.makeText(this, "Sesion caducada, vuelve a iniciar sesion", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, LoginActivity.class));
        showLoading(false);
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (!sessionManager.isLoggedIn()) {
            btnFavorite.setText("Inicia sesion para guardar favoritos");
            return;
        }
        btnFavorite.setText(isFavorite ? "Quitar de favoritos" : "Anadir a favoritos");
    }

    private void showLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        content.setVisibility(loading ? View.GONE : View.VISIBLE);
    }
}
