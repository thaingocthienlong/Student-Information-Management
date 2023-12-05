package com.app.studentinformationmanagement;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.app.studentinformationmanagement.models.User;
import com.app.studentinformationmanagement.services.AuthService;
import com.app.studentinformationmanagement.services.UserService;
import com.google.firebase.auth.FirebaseUser;

import java.security.SecureRandom;

public class AddUserActivity extends AppCompatActivity {

    EditText editTextName, editTextAge, editTextPhoneNumber, editTextEmail;
    Button buttonFinish, buttonCancel;
    SwitchCompat statusSwitch;
    Spinner spinnerRole;
    ImageView imageViewProfilePicture;
    Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        imageViewProfilePicture = findViewById(R.id.imageViewProfile);
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextPhoneNumber = findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.editTextEmail);
        statusSwitch = findViewById(R.id.switchStatus);
        spinnerRole = findViewById(R.id.spinnerRole);
        buttonFinish = findViewById(R.id.buttonFinish);
        buttonCancel = findViewById(R.id.buttonCancel);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        imageViewProfilePicture.setOnClickListener(view -> {
            mGetContent.launch("image/*");
        });
        buttonFinish.setOnClickListener(view -> createNewUser());
        buttonCancel.setOnClickListener(view -> {
            Intent intent = new Intent(AddUserActivity.this, UserManagementActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void createNewUser() {
        String name = editTextName.getText().toString();
        String ageStr = editTextAge.getText().toString();
        String phone = editTextPhoneNumber.getText().toString();
        String email = editTextEmail.getText().toString();
        String role = spinnerRole.getSelectedItem().toString();
        String status = statusSwitch.isChecked() ? "Normal" : "Locked";

        if (validateInput(name, ageStr, phone, email, role)) {
            String randomPassword = generateRandomPassword();

            AuthService authService = new AuthService(this);
            authService.createUserWithEmailPassword(email, randomPassword, new AuthService.AuthResultHandler() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    String userId = user.getUid();
                    String password = randomPassword;
                    uploadUserImage(user, name, ageStr, phone, email, password, role, status);
                }

                @Override
                public void onFailure(Exception exception) {
                    Toast.makeText(AddUserActivity.this, "Failed to create user: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void uploadUserImage(FirebaseUser user, String name, String ageStr, String phone, String email, String password, String role, String status) {
        String userId = user.getUid();
        UserService userService = new UserService();
        userService.uploadProfileImage(fileUri, user.getUid(), new UserService.UploadImageCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                User newUser = new User(userId, name, Integer.parseInt(ageStr), phone, email, status, role, imageUrl);
                addNewUserToFirestore(newUser, password);
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(AddUserActivity.this, "Failed to upload image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewUserToFirestore(User newUser , String password) {
        UserService userService = new UserService();
        userService.addUserToFirestore(newUser, password, new UserService.FirestoreUserCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddUserActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddUserActivity.this, UserManagementActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(AddUserActivity.this, "Failed to create user: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateRandomPassword() {
        String letters = "abcdefghijklmnopqrstuvwxyz";
        String capitalLetters = letters.toUpperCase();
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*";
        String combinedChars = letters + capitalLetters + numbers + specialChars;
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        password.append(letters.charAt(random.nextInt(letters.length())));
        password.append(capitalLetters.charAt(random.nextInt(capitalLetters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        for (int i = 4; i < 10; i++) {
            password.append(combinedChars.charAt(random.nextInt(combinedChars.length())));
        }

        return password.toString();

    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        imageViewProfilePicture.setImageURI(uri);
                        fileUri = uri;
                    }
                }
            });

    private boolean validateInput(String name, String ageStr, String phone, String email, String role) {
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
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (role.isEmpty()) {
            Toast.makeText(this, "Role cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}