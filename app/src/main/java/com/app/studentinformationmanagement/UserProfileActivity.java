package com.app.studentinformationmanagement;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.app.studentinformationmanagement.services.UserService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UserProfileActivity extends AppCompatActivity {


    private EditText editTextName, editTextAge, editTextPhoneNumber, editTextEmail;
    private Button buttonSave;
    private String userId;
    private UserService userService;
    private SwitchCompat statusSwitch, switchEdit;
    private Spinner spinnerRole;
    private ImageView imageViewProfilePicture;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userId = getIntent().getStringExtra("USER_ID");

        initializeViews();
        initializeUserService();
        if (userId != null) {
            revertChanges();
        }
        setImageForUser(userId);
    }

    private void initializeViews() {
        imageViewProfilePicture = findViewById(R.id.imageViewUserProfile);
        editTextName = findViewById(R.id.editTextUserProfileName);
        editTextAge = findViewById(R.id.editTextUserProfileAge);
        editTextPhoneNumber = findViewById(R.id.editTextUserProfilePhone);
        editTextEmail = findViewById(R.id.editTextEmailUserProfile);
        statusSwitch = findViewById(R.id.switchStatusUserProfile);
        spinnerRole = findViewById(R.id.spinnerRoleUserProfile);
        switchEdit = findViewById(R.id.switchEditUserProfile);
        buttonSave = findViewById(R.id.buttonSaveUserProfile);

        imageViewProfilePicture.setOnClickListener(view -> {
            mGetContent.launch("image/*");
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        switchEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableEditing();
            } else {
                revertChanges();
            }
        });
        buttonSave.setOnClickListener(v -> uploadUserImage());
    }

    private void enableEditing() {
        imageViewProfilePicture.setEnabled(true);
        editTextName.setEnabled(true);
        editTextAge.setEnabled(true);
        editTextPhoneNumber.setEnabled(true);
        buttonSave.setEnabled(true);
    }

    private void revertChanges() {
        fetchUserDetails(userId);
        imageViewProfilePicture.setEnabled(false);
        editTextName.setEnabled(false);
        editTextAge.setEnabled(false);
        editTextPhoneNumber.setEnabled(false);
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
                Glide.with(UserProfileActivity.this)
                        .asBitmap()
                        .load(imageUri)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                // Save bitmap to file and get Uri
                                fileUri = saveImageToFile(resource, "profile_picture");
                                imageViewProfilePicture.setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });


            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(UserProfileActivity.this, "Error fetching image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Uri saveImageToFile(Bitmap bitmap, String filename) {
        // ContextWrapper to get the app's file directory
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        // Creating a file in the app's internal storage
        File file = new File(wrapper.getFilesDir(), filename + ".jpg");
        try (OutputStream stream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(file);
    }

    private void uploadUserImage() {
        UserService userService = new UserService();
        if (fileUri != null) {
            userService.uploadProfileImage(fileUri, userId, new UserService.UploadImageCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    saveUserDetails(imageUrl);
                }

                @Override
                public void onFailure(Exception exception) {
                    Toast.makeText(UserProfileActivity.this, "Failed to upload image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveUserDetails(null);
        }

    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        fileUri = uri;
                        imageViewProfilePicture.setImageURI(uri);
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
                Toast.makeText(UserProfileActivity.this, "Error fetching user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        if (imageUrl != null) {
            updatedUser.setProfilePictureUrl(imageUrl);
        } else {
            updatedUser.setProfilePictureUrl(fileUri != null ? fileUri.toString() : null);
        }

        if(statusSwitch.isChecked()) {
            updatedUser.setStatus("Normal");
        } else {
            updatedUser.setStatus("Blocked");
        }
        updatedUser.setRole(spinnerRole.getSelectedItem().toString());

        userService.updateUser(updatedUser, new UserService.UpdateUserCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(UserProfileActivity.this, "User details updated successfully", Toast.LENGTH_SHORT).show();
                revertChanges();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(UserProfileActivity.this, "Error updating user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        revertChanges();
    }
}