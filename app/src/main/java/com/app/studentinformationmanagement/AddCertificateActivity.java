package com.app.studentinformationmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.studentinformationmanagement.models.Certificate;
import com.app.studentinformationmanagement.models.Student;
import com.app.studentinformationmanagement.services.StudentService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class AddCertificateActivity extends AppCompatActivity {

    private LinearLayout certificateContainer;
    private Button buttonAddCertificate, buttonFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_certificate);

        certificateContainer = findViewById(R.id.certificateContainer);
        buttonAddCertificate = findViewById(R.id.buttonAddCertificate);
        buttonFinish = findViewById(R.id.buttonFinish);

        buttonAddCertificate.setOnClickListener(v -> addCertificateFields());
        buttonFinish.setOnClickListener(v -> saveDataToFirebase());
    }

    private void addCertificateFields() {
        LinearLayout newCertificateLayout = new LinearLayout(this);
        newCertificateLayout.setOrientation(LinearLayout.VERTICAL);
        newCertificateLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText editTextCertificateName = new EditText(this);
        editTextCertificateName.setHint("Certificate Name");
        newCertificateLayout.addView(editTextCertificateName);

        EditText editTextIssuingAuthority = new EditText(this);
        editTextIssuingAuthority.setHint("Issuing Authority");
        newCertificateLayout.addView(editTextIssuingAuthority);

        EditText editTextDateOfIssue = new EditText(this);
        editTextDateOfIssue.setHint("Date of Issue");
        editTextDateOfIssue.setFocusable(false); // Make the EditText not focusable
        editTextDateOfIssue.setOnClickListener(v -> showDatePicker(editTextDateOfIssue));
        newCertificateLayout.addView(editTextDateOfIssue);

        certificateContainer.addView(newCertificateLayout);
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    editText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveDataToFirebase() {
        SharedPreferences sharedPreferences = getSharedPreferences("StudentPrefs", MODE_PRIVATE);
        String name = sharedPreferences.getString("studentName", "");
        int age = sharedPreferences.getInt("studentAge", -1);
        String phone = sharedPreferences.getString("studentPhoneNumber", "");

        String studentId = generateUniqueId("Student");
        Student student = new Student(studentId, name, age, phone, new ArrayList<>());

        for (int i = 0; i < certificateContainer.getChildCount(); i++) {
            LinearLayout certificateLayout = (LinearLayout) certificateContainer.getChildAt(i);
            EditText editTextCertificateName = (EditText) certificateLayout.getChildAt(0);
            EditText editTextIssuingAuthority = (EditText) certificateLayout.getChildAt(1);
            EditText editTextDateOfIssue = (EditText) certificateLayout.getChildAt(2);

            String certName = editTextCertificateName.getText().toString();
            String issuingAuthority = editTextIssuingAuthority.getText().toString();
            String dateOfIssue = editTextDateOfIssue.getText().toString();

            validateCertificateData(certName, issuingAuthority, dateOfIssue);

            String certificateId = generateUniqueId("Certificate");
            Certificate certificate = new Certificate(certificateId, certName, issuingAuthority, dateOfIssue);
            student.addCertificate(certificate);
        }

        StudentService studentService = new StudentService();
        studentService.addStudent(student, new StudentService.AddStudentCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddCertificateActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();
                clearSharedPreferences();
                Intent intent = new Intent(AddCertificateActivity.this, StudentManagementActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AddCertificateActivity.this, "Error adding student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean validateCertificateData(String certName, String issuingAuthority, String dateOfIssue) {
        if (certName.isEmpty() || issuingAuthority.isEmpty() || dateOfIssue.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String generateUniqueId(String type) {
        String uid = UUID.randomUUID().toString();
        return type + "_" + uid;
    }

    private void clearSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("StudentPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
