package com.app.studentinformationmanagement;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.app.studentinformationmanagement.models.User;
import com.app.studentinformationmanagement.services.UserService;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;

public class UserDetailActivity extends AppCompatActivity {

    private EditText editTextName, editTextAge, editTextPhoneNumber, editTextEmail;
    private Button buttonSave;
    private String userId;
    private UserService userService;
    private SwitchCompat statusSwitch, switchEdit;
    private Spinner spinnerRole;
    private ImageView imageViewProfilePicture;
    private Uri fileUri;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        userRole = getIntent().getStringExtra("USER_ROLE");
        userId = getIntent().getStringExtra("USER_ID");

        initializeViews();
        initializeUserService();
        if (userId != null) {
            revertChanges();
        }
        setImageForUser(userId);
    }

    private void initializeViews() {
        imageViewProfilePicture = findViewById(R.id.imageViewUserDetail);
        editTextName = findViewById(R.id.editTextUserDetailName);
        editTextAge = findViewById(R.id.editTextUserDetailAge);
        editTextPhoneNumber = findViewById(R.id.editTextUserDetailPhone);
        editTextEmail = findViewById(R.id.editTextEmailUserDetail);
        statusSwitch = findViewById(R.id.switchStatusUserDetail);
        spinnerRole = findViewById(R.id.spinnerRoleUserDetail);
        switchEdit = findViewById(R.id.switchEditUserDetail);
        buttonSave = findViewById(R.id.buttonSaveUserDetail);

        imageViewProfilePicture.setOnClickListener(view -> {
            mGetContent.launch("image/*");
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        if (userRole.equals("Manager") || userRole.equals("Employee")) {
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
        buttonSave.setOnClickListener(v -> uploadUserImage());
    }

    private void enableEditing() {
        imageViewProfilePicture.setEnabled(true);
        editTextName.setEnabled(true);
        editTextAge.setEnabled(true);
        editTextPhoneNumber.setEnabled(true);
        statusSwitch.setEnabled(true);
        spinnerRole.setEnabled(true);
        buttonSave.setEnabled(true);
    }

    private void revertChanges() {
        fetchUserDetails(userId);
        imageViewProfilePicture.setEnabled(false);
        editTextName.setEnabled(false);
        editTextAge.setEnabled(false);
        editTextPhoneNumber.setEnabled(false);
        statusSwitch.setEnabled(false);
        spinnerRole.setEnabled(false);
        buttonSave.setEnabled(false);
    }

    private void initializeUserService() {
        userService = new UserService();
    }

    private void setImageForUser(String userId) {
        userService.fetchUserImage(userId, new UserService.UserImageCallback() {
            @Override
            public void onImageFetched(Uri imageUri) {
                Glide.with(UserDetailActivity.this)
                        .load(imageUri)
                        .into(imageViewProfilePicture);
                fileUri = imageUri;
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(UserDetailActivity.this, "Error fetching image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadUserImage() {
        UserService userService = new UserService();
        userService.uploadProfileImage(fileUri, userId, new UserService.UploadImageCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                saveUserDetails(imageUrl);
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(UserDetailActivity.this, "Failed to upload image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    private void fetchUserDetails(String userId) {
        userService.getUserById(userId, new UserService.UserDataCallback() {
            @Override
            public void onDataReceived(User user) {
                editTextName.setText(user.getName());
                editTextAge.setText(String.valueOf(user.getAge()));
                editTextPhoneNumber.setText(user.getPhoneNumber());
                editTextEmail.setText(user.getEmail());
                if(user.getStatus().equals("Normal")) {
                    statusSwitch.setChecked(true);
                } else {
                    statusSwitch.setChecked(false);
                }

                if(user.getRole().equals("Admin")) {
                    spinnerRole.setSelection(0);
                } else if(user.getRole().equals("Manager")) {
                    spinnerRole.setSelection(1);
                }else {
                    spinnerRole.setSelection(2);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(UserDetailActivity.this, "Error fetching user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserDetails(String imageUrl) {
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(editTextName.getText().toString());
        updatedUser.setAge(Integer.parseInt(editTextAge.getText().toString()));
        updatedUser.setPhoneNumber(editTextPhoneNumber.getText().toString());
        updatedUser.setEmail(editTextEmail.getText().toString());
        updatedUser.setProfilePictureUrl(imageUrl);
        if(statusSwitch.isChecked()) {
            updatedUser.setStatus("Normal");
        } else {
            updatedUser.setStatus("Blocked");
        }
        updatedUser.setRole(spinnerRole.getSelectedItem().toString());

        userService.updateUser(updatedUser, new UserService.UpdateUserCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(UserDetailActivity.this, "User details updated successfully", Toast.LENGTH_SHORT).show();
                revertChanges();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(UserDetailActivity.this, "Error updating user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        revertChanges();
    }

}