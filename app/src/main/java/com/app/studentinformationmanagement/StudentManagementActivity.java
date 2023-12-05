package com.app.studentinformationmanagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.studentinformationmanagement.adapter.RecyclerItemClickListener;
import com.app.studentinformationmanagement.adapter.StudentActionModeCallback;
import com.app.studentinformationmanagement.adapter.StudentAdapter;
import com.app.studentinformationmanagement.models.Student;
import com.app.studentinformationmanagement.services.StudentService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class StudentManagementActivity extends AppCompatActivity implements StudentAdapter.StudentItemClickListener{

    private RecyclerView studentsRecyclerView;
    private StudentAdapter studentAdapter;
    private ActionMode actionMode;
    private StudentService studentService;
    private ProgressBar loadingIndicator;
    private StudentActionModeCallback actionModeCallback;
    private FloatingActionButton fabAddStudent;
    private String userRole;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_management);

        userRole = getIntent().getStringExtra("USER_ROLE");

        setupRecyclerView(userRole);
        setupSearchEditText();
        loadStudents();
    }

    private void setupRecyclerView(String userRole) {
        List<Student> studentsList = new ArrayList<>();
        loadingIndicator = findViewById(R.id.loadingIndicator);
        studentsRecyclerView = findViewById(R.id.rvStudents);
        loadingIndicator.setVisibility(View.VISIBLE);
        studentAdapter = new StudentAdapter(studentsList, this);
        studentsRecyclerView.setAdapter(studentAdapter);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fabAddStudent = findViewById(R.id.fabAddStudent);

        if (userRole != null && userRole.equals("Employee")) {
            fabAddStudent.setOnClickListener(view -> {
                Toast.makeText(StudentManagementActivity.this, "You do not have permission to add a student", Toast.LENGTH_SHORT).show();
            });

            studentsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, studentsRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    Toast.makeText(StudentManagementActivity.this, "You do not have permission to delete a student", Toast.LENGTH_SHORT).show();
                }
            }));
        } else {
            fabAddStudent.setOnClickListener(view -> {
                Intent intent = new Intent(StudentManagementActivity.this, AddStudentActivity.class);
                startActivity(intent);
            });
            studentsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, studentsRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    if (actionMode == null) {
                        actionModeCallback = new StudentActionModeCallback(StudentManagementActivity.this, studentAdapter, actionMode);
                        actionMode = startSupportActionMode(actionModeCallback);
                        studentAdapter.setActionModeEnabled(true);
                        toggleSelection(position);
                    }
                }
            }));
        }
    }

    private void setupSearchEditText() {
        EditText etSearchStudent = findViewById(R.id.etSearchStudent);
        etSearchStudent.addTextChangedListener(new CustomTextWatcher());
    }

    private class CustomTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            studentAdapter.getFilter().filter(s);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }


    private void toggleSelection(int position) {
        studentAdapter.toggleSelection(position);
        int selectedCount = studentAdapter.getSelectedItemCount();

        if (selectedCount == 0 && actionMode != null) {
            actionMode.finish();
        } else if (actionMode != null) {
            actionMode.setTitle(selectedCount + " selected");
        }
    }

    @Override
    public void onItemClicked(Student student) {
        Intent intent = new Intent(StudentManagementActivity.this, StudentDetailActivity.class);
        intent.putExtra("STUDENT_ID", student.getId());
        intent.putExtra("USER_ROLE", userRole);
        startActivity(intent);
    }

    @Override
    public void onMoreOptionsClicked(View view, int position) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.options_menu);

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (item.getItemId() == R.id.delete) {
                if (userRole != null && userRole.equals("Employee")) {
                    Toast.makeText(StudentManagementActivity.this, "You do not have permission to delete a student", Toast.LENGTH_SHORT).show();
                    return false;
                }
                confirmAndDeleteItem(position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void confirmAndDeleteItem(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    String studentId = studentAdapter.getStudentId(position);
                    deleteStudentFromFirebase(studentId, position);
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteStudentFromFirebase(String studentId, int position) {
        studentService.deleteStudent(studentId, new StudentService.StudentDeleteCallback() {
            @Override
            public void onSuccess() {
                studentAdapter.deleteItem(position);
                Toast.makeText(StudentManagementActivity.this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(StudentManagementActivity.this, "Error deleting student: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onItemLongClicked(int position) {
        if (userRole != null && userRole.equals("Employee")){
            Toast.makeText(StudentManagementActivity.this, "You do not have permission to perform this action", Toast.LENGTH_SHORT).show();
        }else {
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback);
            }
            studentAdapter.toggleSelection(position);
        }
    }
    private void loadStudents() {
        studentService = new StudentService();
        actionModeCallback = new StudentActionModeCallback(this, studentAdapter, actionMode);
        studentService.getAllStudents(new StudentService.StudentListDataCallback() {
            @Override
            public void onDataReceived(List<Student> students) {
                // Update the RecyclerView with the fetched students
                studentAdapter.updateData(students);
            }

            @Override
            public void onError(Exception e) {
                Log.e("StudentManagement", "Error fetching students", e);
            }
        });
        loadingIndicator.setVisibility(View.GONE);
    }

    public  void confirmAndDeleteSelectedItems() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Selected Items")
                .setMessage("Are you sure you want to delete these items?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteSelectedItems())
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteSelectedItems() {
        List<Integer> selectedItemPositions = studentAdapter.getSelectedItemsPositions();
        for (int position : selectedItemPositions) {
            String studentId = studentAdapter.getStudentId(position);
            deleteStudentFromFirebase(studentId, position);
        }

        actionMode.finish();
    }

    public void onActionModeFinished() {
        actionMode = null;
    }

}
