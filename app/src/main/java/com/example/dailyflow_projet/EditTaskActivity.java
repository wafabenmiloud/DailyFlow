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

public class EditTaskActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_TITLE = "task_title";
    public static final String EXTRA_TASK_DESCRIPTION = "task_description";
    public static final String EXTRA_TASK_DUE_DATE = "task_due_date";
    public static final String EXTRA_TASK_PRIORITY = "task_priority";
    public static final String EXTRA_TASK_COMPLETED = "task_completed";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputEditText titleInput, descriptionInput, dueDateInput;
    private AutoCompleteTextView priorityInput;
    private SwitchMaterial completedSwitch;
    private MaterialButton saveButton;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String taskId;

    private static final String[] PRIORITY_OPTIONS = {"High", "Medium", "Low"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get task data from intent
        taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
        String title = getIntent().getStringExtra(EXTRA_TASK_TITLE);
        String description = getIntent().getStringExtra(EXTRA_TASK_DESCRIPTION);
        String dueDate = getIntent().getStringExtra(EXTRA_TASK_DUE_DATE);
        String priority = getIntent().getStringExtra(EXTRA_TASK_PRIORITY);
        boolean completed = getIntent().getBooleanExtra(EXTRA_TASK_COMPLETED, false);

        // Initialize date formatter
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // UI components
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        dueDateInput = findViewById(R.id.dueDateInput);
        priorityInput = findViewById(R.id.priorityInput);
        completedSwitch = findViewById(R.id.completedSwitch);
        saveButton = findViewById(R.id.saveButton);

        // Set initial values
        titleInput.setText(title);
        descriptionInput.setText(description);
        dueDateInput.setText(dueDate);
        priorityInput.setText(priority);
        completedSwitch.setChecked(completed);

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

        // Save Button Click
        saveButton.setOnClickListener(v -> updateTask());
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

    private void updateTask() {
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

        // Update task in Firebase
        db.collection("Task")
            .document(taskId)
            .update(
                "title", title,
                "description", description,
                "dueDate", formattedDate,
                "priority", priority,
                "completed", completed
            )
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to update task: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
    }
} 