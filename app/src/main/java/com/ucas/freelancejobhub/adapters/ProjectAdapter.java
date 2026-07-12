package com.ucas.freelancejobhub.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.ucas.freelancejobhub.R;
import com.ucas.freelancejobhub.models.Project;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    public interface ProjectListener {
        void onProjectClicked(Project project);
        void onEditClicked(Project project);
        void onDeleteClicked(Project project);
    }

    private final List<Project> allProjects = new ArrayList<>();
    private final List<Project> visibleProjects = new ArrayList<>();
    private final ProjectListener listener;
    private String currentQuery = "";

    public ProjectAdapter(ProjectListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return visibleProjects.get(position).getId();
    }

    public void submitList(List<Project> projects) {
        allProjects.clear();
        allProjects.addAll(projects);
        filter(currentQuery);
    }

    public void filter(String query) {
        currentQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        visibleProjects.clear();
        if (currentQuery.isEmpty()) {
            visibleProjects.addAll(allProjects);
        } else {
            for (Project project : allProjects) {
                String searchable = (project.getTitle() + " " + project.getClientName() + " "
                        + project.getStatus()).toLowerCase(Locale.ROOT);
                if (searchable.contains(currentQuery)) {
                    visibleProjects.add(project);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getVisibleItemCount() {
        return visibleProjects.size();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = visibleProjects.get(position);
        holder.title.setText(project.getTitle());
        holder.client.setText(holder.itemView.getContext().getString(
                R.string.client_name_value, project.getClientName()));
        holder.budget.setText(NumberFormat.getCurrencyInstance(Locale.US).format(project.getBudget()));
        holder.dueDate.setText(project.getDueDate() == null || project.getDueDate().isEmpty()
                ? holder.itemView.getContext().getString(R.string.no_deadline)
                : holder.itemView.getContext().getString(R.string.due_date_value, project.getDueDate()));
        holder.status.setText(project.getStatus());
        holder.status.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), statusColor(project.getStatus()))));

        holder.itemView.setOnClickListener(view -> listener.onProjectClicked(project));
        holder.edit.setOnClickListener(view -> listener.onEditClicked(project));
        holder.delete.setOnClickListener(view -> listener.onDeleteClicked(project));
    }

    private int statusColor(String status) {
        if ("Active".equals(status)) return R.color.status_active_background;
        if ("Completed".equals(status)) return R.color.status_completed_background;
        if ("Pending Invoice".equals(status)) return R.color.status_pending_background;
        if ("On Hold".equals(status)) return R.color.status_hold_background;
        return R.color.status_planning_background;
    }

    @Override
    public int getItemCount() {
        return visibleProjects.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView client;
        final TextView budget;
        final TextView dueDate;
        final Chip status;
        final ImageButton edit;
        final ImageButton delete;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_project_title);
            client = itemView.findViewById(R.id.text_project_client);
            budget = itemView.findViewById(R.id.text_project_budget);
            dueDate = itemView.findViewById(R.id.text_project_due_date);
            status = itemView.findViewById(R.id.chip_project_status);
            edit = itemView.findViewById(R.id.button_item_edit);
            delete = itemView.findViewById(R.id.button_item_delete);
        }
    }
}
