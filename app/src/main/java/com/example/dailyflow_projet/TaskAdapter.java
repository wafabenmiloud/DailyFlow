package com.example.dailyflow_projet;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import com.google.android.material.card.MaterialCardView;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    private List<Task> tasks;
    private FirebaseFirestore db;
    private Context context;

    public TaskAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.titleText.setText(task.getTitle());
        holder.descriptionText.setText(task.getDescription());
        holder.dueDateText.setText(task.getFormattedDueDate());
        holder.priorityText.setText(task.getPriority());

        // Update check button icon based on completion status
        updateCheckButtonIcon(holder, task.isCompleted());

        // Change card and text colors based on completion status
        MaterialCardView cardView = (MaterialCardView) holder.itemView;
        if (task.isCompleted()) {
            cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.completed_task_background));
            holder.titleText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.completed_task_text));
            holder.descriptionText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.completed_task_text));
            holder.dueDateText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.completed_task_text));
            holder.priorityText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.completed_task_text));
        } else {
            cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
            holder.titleText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.gray_dark));
            holder.descriptionText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.gray));
            holder.dueDateText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
            holder.priorityText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorAccent));
        }

        // Set up check button click
        holder.checkButton.setOnClickListener(v -> {
            boolean newStatus = !task.isCompleted();
            // Update the task in Firebase
            db.collection("Task")
                .whereEqualTo("userId", task.getUserId())
                .whereEqualTo("title", task.getTitle())
                .whereEqualTo("dueDate", task.getDueDate())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("Task")
                            .document(documentId)
                            .update("completed", newStatus)
                            .addOnSuccessListener(aVoid -> {
                                // Update the local task object and UI
                                task.setCompleted(newStatus);
                                updateCheckButtonIcon(holder, newStatus);
                                Toast.makeText(v.getContext(), 
                                    newStatus ? "Task marked as completed" : "Task marked as incomplete", 
                                    Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(v.getContext(), 
                                    "Failed to update task status: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(v.getContext(), 
                        "Failed to find task: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        });

        // Set up edit button click
        holder.editButton.setOnClickListener(v -> {
            // Find the task document ID
            db.collection("Task")
                .whereEqualTo("userId", task.getUserId())
                .whereEqualTo("title", task.getTitle())
                .whereEqualTo("dueDate", task.getDueDate())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        // Start EditTaskActivity with task data
                        Intent intent = new Intent(context, EditTaskActivity.class);
                        intent.putExtra(EditTaskActivity.EXTRA_TASK_ID, documentId);
                        intent.putExtra(EditTaskActivity.EXTRA_TASK_TITLE, task.getTitle());
                        intent.putExtra(EditTaskActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
                        intent.putExtra(EditTaskActivity.EXTRA_TASK_DUE_DATE, task.getFormattedDueDate());
                        intent.putExtra(EditTaskActivity.EXTRA_TASK_PRIORITY, task.getPriority());
                        intent.putExtra(EditTaskActivity.EXTRA_TASK_COMPLETED, task.isCompleted());
                        context.startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(context, 
                        "Failed to find task: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show()
                );
        });
    }

    private void updateCheckButtonIcon(TaskViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.checkButton.setImageResource(R.drawable.ic_checkbox_checked);
            holder.checkButton.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.completed_task_text));
        } else {
            holder.checkButton.setImageResource(R.drawable.ic_checkbox_unchecked);
            holder.checkButton.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.gray));
        }
    }

    public void deleteTask(int position) {
        Task task = tasks.get(position);
        db.collection("Task")
            .whereEqualTo("userId", task.getUserId())
            .whereEqualTo("title", task.getTitle())
            .whereEqualTo("dueDate", task.getDueDate())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    db.collection("Task")
                        .document(documentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            // Remove from local list and notify adapter
                            tasks.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, 
                                "Task deleted successfully", 
                                Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> 
                            Toast.makeText(context, 
                                "Failed to delete task: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show()
                        );
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(context, 
                    "Failed to find task: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show()
            );
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
}
