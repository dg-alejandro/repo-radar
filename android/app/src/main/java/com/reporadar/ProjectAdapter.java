package com.reporadar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.reporadar.model.Project;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    private List<Project> projects;
    private final OnProjectClickListener listener;

    public ProjectAdapter(List<Project> projects, OnProjectClickListener listener) {
        this.projects = projects;
        this.listener = listener;
    }

    //si la lista de proyectos cambia, este metodo actualiza la lista

    public void setProjects(List<Project> projects) {
        this.projects = projects;
        notifyDataSetChanged();
    }

    //este metodo se ejecuta cuando recyclerview necesita una nueva fila
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ViewHolder(view);
    }

    //este metodo se ejecuta una vez por fila
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project p = projects.get(position);//numero de proyecto que estamos dibujando
        //"pegamos" los datos del objeto project en los textview
        holder.tvName.setText(p.getName());
        holder.tvDescription.setText(p.getDescription() != null ? p.getDescription() : "");
        holder.tvAuthor.setText(p.getAuthor() != null ? "por " + p.getAuthor() : "");
        //cuando se toque una fila, se dispara el metodo del listener en un proyecto determinado
        holder.itemView.setOnClickListener(v -> listener.onProjectClick(p));
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    //almacena las referencias a los elementos del diseño(los ids del xml) para que android no tenga que buscar
    //miles de veces segun scrolleamos en la aplicacion, ayudando con la fluidez de la misma
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvAuthor;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
        }
    }
}