package com.app.studentinformationmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.studentinformationmanagement.models.Student;
import com.app.studentinformationmanagement.services.StudentService;

public class AddStudentActivity extends AppCompatActivity {

    EditText editTextName, editTextAge, editTextPhoneNumber;
    Button buttonNext, buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        editTextName = findViewById(R.id.editTextStudentName);
        editTextAge = findViewById(R.id.editTextStudentAge);
        editTextPhoneNumber = findViewById(R.id.editTextStudentPhoneNumber);
        buttonNext = findViewById(R.id.buttonNext);
        buttonCancel = findViewById(R.id.buttonCancel);

        buttonNext.setOnClickListener(view -> saveDataAndNavigate());
        buttonCancel.setOnClickListener(view -> {finish();});
    }

    private void saveDataAndNavigate() {
        String name = editTextName.getText().toString();
        String ageStr = editTextAge.getText().toString();
        String phone = editTextPhoneNumber.getText().toString();

        if (validateInput(name, ageStr, phone)) {
            SharedPreferences sharedPreferences = getSharedPreferences("StudentPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("studentName", name);
            editor.putInt("studentAge", Integer.parseInt(ageStr));
            editor.putString("studentPhoneNumber", phone);
            editor.apply();

            // Navigate to AddCertificateActivity
            Intent intent = new Intent(this, AddCertificateActivity.class);
            startActivity(intent);
        }
    }


    private boolean validateInput(String name, String ageStr, String phone) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (age <= 0) {
            Toast.makeText(this, "Age must be positive", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!phone.matches("\\+?\\d+")) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
