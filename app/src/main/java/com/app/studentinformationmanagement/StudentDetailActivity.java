package com.app.studentinformationmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.studentinformationmanagement.models.Certificate;
import com.app.studentinformationmanagement.models.Student;
import com.app.studentinformationmanagement.services.StudentService;

import java.util.Collections;
import java.util.List;

public class StudentDetailActivity extends AppCompatActivity {
    private EditText editTextStudentName, editTextStudentAge, editTextStudentPhone;
    private EditText editTextCertificateName, editTextCertificateIssuingAuthority, editTextCertificateDateOfIssue;
    private Button buttonSave;
    private SwitchCompat switchEdit;
    private String studentId, userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);
        userRole = getIntent().getStringExtra("USER_ROLE");

        editTextStudentName = findViewById(R.id.editTextStudentName);
        editTextStudentAge = findViewById(R.id.editTextStudentAge);
        editTextStudentPhone = findViewById(R.id.editTextStudentPhone);
        editTextCertificateName = findViewById(R.id.editTextCertificateName);
        editTextCertificateIssuingAuthority = findViewById(R.id.editTextCertificateIssuingAuthority);
        editTextCertificateDateOfIssue = findViewById(R.id.editTextCertificateDateofIssue);
        buttonSave = findViewById(R.id.buttonSave);
        switchEdit = findViewById(R.id.switchEdit);

        studentId = getIntent().getStringExtra("STUDENT_ID");
        if (studentId != null) {
            fetchStudentDetails(studentId);
        }

        if (userRole != null && userRole.equals("Employee")) {
            switchEdit.setEnabled(false);
        }else{
            switchEdit.setEnabled(true);
            switchEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    enableEditing();
                } else {
                    revertChanges();
                }
            });
        }

        buttonSave.setOnClickListener(v -> saveStudentDetails());
    }

    private void fetchStudentDetails(String studentId) {
        StudentService studentService = new StudentService();

        studentService.getStudentById(studentId, new StudentService.StudentDataCallback() {
            @Override
            public void onDataReceived(Student student) {
                editTextStudentName.setText(student.getName());
                editTextStudentAge.setText(String.valueOf(student.getAge()));
                editTextStudentPhone.setText(student.getPhoneNumber());

                List<Certificate> certificates = student.getCertificates();
                if (certificates != null && !certificates.isEmpty()) {
                    Certificate latestCertificate = certificates.get(certificates.size() - 1);
                    editTextCertificateName.setText(latestCertificate.getName());
                    editTextCertificateIssuingAuthority.setText(latestCertificate.getIssuingAuthority());
                    editTextCertificateDateOfIssue.setText(latestCertificate.getDateOfIssue());
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(StudentDetailActivity.this, "Error fetching student details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void enableEditing() {
        editTextStudentName.setEnabled(true);
        editTextStudentAge.setEnabled(true);
        editTextStudentPhone.setEnabled(true);
        editTextCertificateName.setEnabled(true);
        editTextCertificateIssuingAuthority.setEnabled(true);
        editTextCertificateDateOfIssue.setEnabled(true);

        buttonSave.setEnabled(true);
    }

    private void revertChanges() {
        fetchStudentDetails(studentId);
        editTextStudentName.setEnabled(false);
        editTextStudentAge.setEnabled(false);
        editTextStudentPhone.setEnabled(false);
        editTextCertificateName.setEnabled(false);
        editTextCertificateIssuingAuthority.setEnabled(false);
        editTextCertificateDateOfIssue.setEnabled(false);
        buttonSave.setEnabled(false);
    }

    private void saveStudentDetails() {
        Certificate updatedCertificate = new Certificate();
        updatedCertificate.setName(editTextCertificateName.getText().toString());
        updatedCertificate.setIssuingAuthority(editTextCertificateIssuingAuthority.getText().toString());
        updatedCertificate.setDateOfIssue(editTextCertificateDateOfIssue.getText().toString());

        Student updatedStudent = new Student();
        updatedStudent.setId(studentId);
        updatedStudent.setName(editTextStudentName.getText().toString());
        updatedStudent.setAge(Integer.parseInt(editTextStudentAge.getText().toString()));
        updatedStudent.setPhoneNumber(editTextStudentPhone.getText().toString());
        updatedStudent.setCertificates(Collections.singletonList(updatedCertificate));

        StudentService studentService = new StudentService();
        studentService.updateStudent(updatedStudent, new StudentService.UpdateStudentCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(StudentDetailActivity.this, "Student details updated successfully", Toast.LENGTH_SHORT).show();
                switchEdit.setChecked(false); // Turn off edit mode
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(StudentDetailActivity.this, "Error updating student details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        revertChanges();
    }
}
