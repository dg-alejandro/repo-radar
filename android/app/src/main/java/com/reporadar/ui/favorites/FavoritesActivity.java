package com.reporadar.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reporadar.MainActivity;
import com.reporadar.ProjectAdapter;
import com.reporadar.R;
import com.reporadar.RetrofitClient;
import com.reporadar.api.ApiService;
import com.reporadar.model.Project;
import com.reporadar.session.SessionManager;
import com.reporadar.ui.auth.LoginActivity;
import com.reporadar.ui.detail.ProjectDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lista los favoritos del usuario autenticado. Reutiliza el mismo
 * {@link ProjectAdapter} que MainActivity.
 *
 * Si por lo que sea el usuario llega sin sesion, se le manda a login.
 */
public class FavoritesActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ProjectAdapter adapter;
    private ProgressBar progress;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mis favoritos");
        }

        sessionManager = SessionManager.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new ProjectAdapter(new ArrayList<>(), project -> {
            Intent i = new Intent(this, ProjectDetailActivity.class);
            i.putExtra(MainActivity.EXTRA_PROJECT_ID, project.getId());
            startActivity(i);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        loadFavorites();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadFavorites() {
        ApiService authApi = RetrofitClient
                .getAuthenticatedInstance(this)
                .create(ApiService.class);

        showLoading(true);
        authApi.getFavorites().enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(@NonNull Call<List<Project>> call,
                                   @NonNull Response<List<Project>> response) {
                showLoading(false);
                if (response.code() == 401) {
                    sessionManager.clear();
                    Toast.makeText(FavoritesActivity.this,
                            "Sesion caducada, vuelve a iniciar sesion", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(FavoritesActivity.this, LoginActivity.class));
                    finish();
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    List<Project> favs = response.body();
                    adapter.setProjects(favs);
                    tvEmpty.setVisibility(favs.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    adapter.setProjects(new ArrayList<>());
                    tvEmpty.setText("No se pudieron cargar tus favoritos (HTTP "
                            + response.code() + ")");
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Project>> call, @NonNull Throwable t) {
                showLoading(false);
                adapter.setProjects(new ArrayList<>());
                Toast.makeText(FavoritesActivity.this,
                        "Error de conexion: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
