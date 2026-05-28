package com.reporadar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.reporadar.api.ApiService;
import com.reporadar.model.Category;
import com.reporadar.model.Project;
import com.reporadar.model.Technology;
import com.reporadar.session.SessionManager;
import com.reporadar.ui.auth.LoginActivity;
import com.reporadar.ui.detail.ProjectDetailActivity;
import com.reporadar.ui.favorites.FavoritesActivity;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "extra_project_id";

    private ProjectAdapter adapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private TextView tvEmpty;
    private Spinner spinnerCategory;
    private Spinner spinnerTechnology;

    //categorias y tecnologias cargadas al arrancar para construir los filtros
    private final List<Category> categories = new ArrayList<>();
    private final List<Technology> technologies = new ArrayList<>();

    //estado actual del filtro/busqueda; arrancan vacios y se actualizan con
    //el TextWatcher y los spinners
    private String currentQuery;
    private Long currentCategoryId;
    private Long currentTechnologyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        apiService = RetrofitClient.getInstance().create(ApiService.class);//se prepara la conexion a la api
        sessionManager = SessionManager.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        EditText etSearch = findViewById(R.id.etSearch);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerTechnology = findViewById(R.id.spinnerTechnology);

        //se crea el adaptador con una lista vacia
        //al pulsar una fila se abre la pantalla de detalle
        adapter = new ProjectAdapter(new ArrayList<>(), project -> openDetail(project));

        //le decimos a recycler view que se comporte como una lista vertical, y conecta con el adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //este metodo "escucha" cada vez que escribimos una letra en buscador
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                currentQuery = s.toString().trim();
                loadProjects();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadCategories();
        loadTechnologies();
        loadProjects();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean logged = sessionManager.isLoggedIn();
        menu.findItem(R.id.action_favorites).setVisible(logged);
        menu.findItem(R.id.action_logout).setVisible(logged);
        menu.findItem(R.id.action_login).setVisible(!logged);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            return true;
        }
        if (id == R.id.action_login) {
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        }
        if (id == R.id.action_logout) {
            sessionManager.clear();
            invalidateOptionsMenu();
            Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //el estado de login puede haber cambiado al volver de login/register
        invalidateOptionsMenu();
    }

    private void openDetail(Project project) {
        Intent i = new Intent(this, ProjectDetailActivity.class);
        i.putExtra(EXTRA_PROJECT_ID, project.getId());
        startActivity(i);
    }

    //carga el catalogo completo de categorias y lo mete en el spinner
    //la primera opcion es "Todas" para no filtrar por categoria
    private void loadCategories() {
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                categories.clear();
                categories.addAll(response.body());

                List<String> labels = new ArrayList<>();
                labels.add("Todas las categorias");
                for (Category c : categories) labels.add(c.getName());

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_item,
                        labels);
                spinnerAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(spinnerAdapter);

                spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        currentCategoryId = (position == 0) ? null : categories.get(position - 1).getId();
                        loadProjects();
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                //si falla la carga de categorias el filtro se queda vacio,
                //pero la app sigue funcionando con el listado completo
            }
        });
    }

    //mismo patron que loadCategories pero contra /api/technologies
    private void loadTechnologies() {
        apiService.getTechnologies().enqueue(new Callback<List<Technology>>() {
            @Override
            public void onResponse(Call<List<Technology>> call, Response<List<Technology>> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                technologies.clear();
                technologies.addAll(response.body());

                List<String> labels = new ArrayList<>();
                labels.add("Todas las tecnologias");
                for (Technology t : technologies) labels.add(t.getName());

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_item,
                        labels);
                spinnerAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                spinnerTechnology.setAdapter(spinnerAdapter);

                spinnerTechnology.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        currentTechnologyId = (position == 0) ? null : technologies.get(position - 1).getId();
                        loadProjects();
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onFailure(Call<List<Technology>> call, Throwable t) {
                //si falla la carga el filtro queda vacio, pero la app sigue funcionando
            }
        });
    }

    private void loadProjects() {
        //limpio el texto que viene del buscador
        //si no escrib nada o son espacios, lo dejo como null para que la api me traiga todos los proyectos que tenga
        String q = (currentQuery != null && !currentQuery.isEmpty()) ? currentQuery : null;

        //le pido a mi servicio (retrofit) que busque los proyectos
        //uso .enqueue para que la descarga sea en segundo plano y no se trabe la pantalla
        apiService.getProjects(q, currentCategoryId, currentTechnologyId).enqueue(new Callback<List<Project>>() {

            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                //entro aquí si el servidor responde algo,aunque sea un error
                //primero reviso si la respuesta es exitosa y si trae datos
                if (response.isSuccessful() && response.body() != null) {

                    //guardo la lista de proyectos que manda el servidor
                    List<Project> projects = response.body();

                    //le paso los proyectos al adaptador para que los dibuje en la lista
                    adapter.setProjects(projects);

                    //si la lista llego vacia, muestro el mensaje de "No hay resultados"
                    //si si hay proyectos, escondo ese mensaje (GONE)
                    tvEmpty.setVisibility(projects.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                //aqui entramos si algo falla o el servidor se cae
                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
