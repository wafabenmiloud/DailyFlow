package com.example.dailyflow_projet;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    public TextView titleText;
    public TextView descriptionText;
    public TextView dueDateText;
    public TextView priorityText;
    public ImageButton editButton;
    public ImageButton checkButton;

    public TaskViewHolder(View itemView) {
        super(itemView);
        titleText = itemView.findViewById(R.id.titleText);
        descriptionText = itemView.findViewById(R.id.descriptionText);
        dueDateText = itemView.findViewById(R.id.dueDateText);
        priorityText = itemView.findViewById(R.id.priorityText);
        editButton = itemView.findViewById(R.id.editButton);
        checkButton = itemView.findViewById(R.id.checkButton);
    }
}