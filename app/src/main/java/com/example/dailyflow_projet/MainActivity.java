package com.example.dailyflow_projet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.ItemTouchHelper;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private MaterialToolbar toolbar;
    private WindowInsetsControllerCompat windowInsetsController;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyStateView;
    private MaterialButton addFirstTaskButton;
    private MaterialButton addTaskButton;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private TextView avatarText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup window insets controller for immersive mode
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        avatarText = findViewById(R.id.avatarText);

        // Check authentication status and update UI
        updateAuthUI();

        // UI components
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyStateView = findViewById(R.id.emptyStateView);
        addFirstTaskButton = emptyStateView.findViewById(R.id.addFirstTaskButton);
        addTaskButton = findViewById(R.id.addTaskButton);
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this, taskList);
        recyclerView.setAdapter(taskAdapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadTasks);

        // Add Task Button Click (FAB, empty state button, and main screen button)
        View.OnClickListener addTaskClickListener = v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        };
        fabAddTask.setOnClickListener(addTaskClickListener);
        addFirstTaskButton.setOnClickListener(addTaskClickListener);
        addTaskButton.setOnClickListener(addTaskClickListener);

        // Load existing tasks
        loadTasks();

        // Attach the swipe-to-delete callback
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(taskAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        }
    }

    private void updateAuthUI() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("MainActivity", "User is authenticated. UID: " + currentUser.getUid());
            Log.d("MainActivity", "User email: " + currentUser.getEmail());
            
            // Update avatar with first letter of email
            String userEmail = currentUser.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                String firstLetter = userEmail.substring(0, 1).toUpperCase();
                avatarText.setText(firstLetter);
            }
        } else {
            Log.e("MainActivity", "No user is authenticated");
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, ActivityLogin.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); // Refresh tasks when returning to this activity
    }

    private void updateEmptyState() {
        if (taskList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void loadTasks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("MainActivity", "No user logged in");
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
            return;
        }

        Log.d("MainActivity", "Loading tasks for user: " + currentUser.getUid());
        
        db.collection("Task")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("MainActivity", "Successfully got " + queryDocumentSnapshots.size() + " tasks");
                    taskList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Task task = document.toObject(Task.class);
                        if (task != null) {
                            taskList.add(task);
                            Log.d("MainActivity", "Added task: " + task.getTitle());
                        } else {
                            Log.e("MainActivity", "Failed to convert document to task");
                        }
                    }
                    taskAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error loading tasks", e);
                    Toast.makeText(this, "Failed to load tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }
}
