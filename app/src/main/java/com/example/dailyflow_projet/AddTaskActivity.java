package com.example.dailyflow_projet;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputEditText titleInput, descriptionInput, dueDateInput;
    private AutoCompleteTextView priorityInput;
    private SwitchMaterial completedSwitch;
    private MaterialButton addButton;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    private static final String[] PRIORITY_OPTIONS = {"High", "Medium", "Low"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize date formatter
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // UI components
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        dueDateInput = findViewById(R.id.dueDateInput);
        priorityInput = findViewById(R.id.priorityInput);
        completedSwitch = findViewById(R.id.completedSwitch);
        addButton = findViewById(R.id.addButton);

        // Setup priority dropdown
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            PRIORITY_OPTIONS
        );
        priorityInput.setAdapter(priorityAdapter);

        // Setup date picker
        dueDateInput.setOnClickListener(v -> showDatePicker());
        dueDateInput.setFocusable(false);

        // Add Task Button Click
        addButton.setOnClickListener(v -> addTask());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dueDateInput.setText(dateFormat.format(calendar.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void addTask() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String dueDate = dueDateInput.getText().toString().trim();
        String priority = priorityInput.getText().toString().trim();
        boolean completed = completedSwitch.isChecked();
        String userId = currentUser.getUid();

        if (title.isEmpty() || dueDate.isEmpty() || priority.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate priority
        boolean isValidPriority = false;
        for (String option : PRIORITY_OPTIONS) {
            if (priority.equals(option)) {
                isValidPriority = true;
                break;
            }
        }

        if (!isValidPriority) {
            Toast.makeText(this, "Please select a valid priority", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format date for storage
        String formattedDate = Task.formatDateForStorage(dueDate);

        Task task = new Task(title, description, formattedDate, priority, completed, userId);
        db.collection("Task")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Failed to add task: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
} 