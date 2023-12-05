package com.app.studentinformationmanagement;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.studentinformationmanagement.models.Certificate;
import com.app.studentinformationmanagement.models.Student;
import com.app.studentinformationmanagement.services.AuthService;
import com.app.studentinformationmanagement.services.StudentService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private ImageView StudentManagement,UserManagement, UserProfile, importButton, exportButton;
    private AuthService authService;
    private Button logoutButton;
    private TextView welcomeText;
    private LinearLayout studentManagementLayout, userManagementLayout, userProfileLayout;
    private ActivityResultLauncher<String> importFileLauncher;


    String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String userEmail = getIntent().getStringExtra("USER_EMAIL");

        welcomeText = findViewById(R.id.welcomeText);
        studentManagementLayout = findViewById(R.id.studentManagementLayout); // Assume this is the ID for the Student Management section
        userManagementLayout = findViewById(R.id.userManagementLayout);
        userProfileLayout = findViewById(R.id.userProfileLayout);

        StudentManagement = findViewById(R.id.studentManagement);
        UserManagement = findViewById(R.id.userManagement);
        UserProfile = findViewById(R.id.userProfile);

        importButton = findViewById(R.id.importFile);
        exportButton = findViewById(R.id.exportFile);

        logoutButton = findViewById(R.id.logoutButton);

        importFileLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        importStudentsFromFile(uri);
                    }
                }
        );

        authService = new AuthService(this);
        if (userEmail != null) {
            fetchUserRole(userEmail);
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });


        StudentManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openStudentManagement(userRole);
            }
        });

        UserManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserManagement(userRole);
            }
        });

        UserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserProfile(userEmail);
            }
        });
        if (userRole != null && userRole.equals("Employee")) {
            importButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(HomeActivity.this, "You do not have permission to import students", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            importButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectImportFile();
                }
            });
        }
        if (userRole != null && userRole.equals("Employee")) {
            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(HomeActivity.this, "You do not have permission to export students", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exportStudentsToCSV();
                }
            });
        }
    }

    private void exportStudentsToCSV() {
        StudentService studentService = new StudentService();
        studentService.getAllStudents(new StudentService.StudentListDataCallback() {
            @Override
            public void onDataReceived(List<Student> students) {
                writeStudentsToCSV(students);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(HomeActivity.this, "Error fetching students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeStudentsToCSV(List<Student> students) {
        String fileName = "students.csv";
        File csvFile = new File(getExternalFilesDir(null), fileName);

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(csvFile);
            for (Student student : students) {
                fileWriter.append(student.getId()).append(",");
                fileWriter.append(student.getName()).append(",");
                fileWriter.append(String.valueOf(student.getAge())).append(",");
                fileWriter.append(student.getPhoneNumber()).append(",");
                Certificate certificate = student.getCertificates().get(0);
                fileWriter.append(certificate.getId()).append(",");
                fileWriter.append(certificate.getName()).append(",");
                fileWriter.append(certificate.getIssuingAuthority()).append(",");
                fileWriter.append(certificate.getDateOfIssue()).append("\n");
            }

            Toast.makeText(this, "CSV file written successfully to " + csvFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error writing CSV file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void importStudentsFromFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");

                Student student = new Student();
                student.setId(tokens[0]);
                student.setName(tokens[1]);
                student.setAge(Integer.parseInt(tokens[2]));
                student.setPhoneNumber(tokens[3]);
                String certificateId = tokens[4];
                String certName = tokens[5];
                String issuingAuthority = tokens[6];
                String dateOfIssue = tokens[7];
                Certificate certificate = new Certificate(certificateId, certName, issuingAuthority, dateOfIssue);
                student.setCertificates(Collections.singletonList(certificate));

                addStudentToFirestore(student);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addStudentToFirestore(Student student) {
        StudentService studentService = new StudentService();
        studentService.addStudent(student, new StudentService.AddStudentCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(HomeActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(HomeActivity.this, "Error adding student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImportFile() {
        importFileLauncher.launch("text/csv");
    }

    private void fetchUserRole(String userEmail) {
        authService.getUserRoleByEmail(userEmail, new AuthService.RoleResultHandler() {
            @Override
            public void onSuccess(String role) {
                userRole = role;
                welcomeText.setText("Welcome, " + role);
            }

            @Override
            public void onFailure(Exception exception) {
                Log.d("TAG", "Error fetching user role: " + exception.getMessage());
            }
        });
    }

    private void openStudentManagement(String role) {
        Intent intent = new Intent(this, StudentManagementActivity.class);
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
    }

    private void openUserManagement(String role) {
        Intent intent = new Intent(this, UserManagementActivity.class);
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
    }

    private void openUserProfile(String userEmail) {
        authService.getUserIdByEmail(userEmail, new AuthService.UserIdByEmailCallback() {
            @Override
            public void onSuccess(String userId) {
                Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(HomeActivity.this, "Error fetching user ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the activity stack
        startActivity(intent);
        finish();
    }
}
