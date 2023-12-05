package com.app.studentinformationmanagement.services;

import android.content.Context;
import android.net.Uri;

import com.app.studentinformationmanagement.models.Student;
import com.app.studentinformationmanagement.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class UserService {
    private Context context;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    public UserService() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public void addUserToFirestore(User user, String password, FirestoreUserCallback callback) {
        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                            db.collection("users").document(user.getId()).update("password", password);
                            callback.onSuccess();
                        }
                )
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void fetchUserImage(String userId, UserImageCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_images/" + userId + ".jpg");

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            callback.onImageFetched(uri);
        }).addOnFailureListener(e -> {
            callback.onError(e);
        });
    }

    public void uploadProfileImage(Uri fileUri, String userId, UploadImageCallback callback) {
        storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + userId + ".jpg");

        profileImageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            callback.onSuccess(imageUrl);
                        })
                )
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getUserById(String userId, UserDataCallback callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onDataReceived(user);
                    } else {
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }

    public void updateUser(User user, UpdateUserCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e));
    }

    public void deleteUser(String userId, UserDeleteCallback callback) {
        deleteUserFromFirestore(userId, new UserDeleteCallback() {
            @Override
            public void onSuccess() {
                deleteUserImageFromStorage(userId, callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    private void deleteUserImageFromStorage(String userId, UserDeleteCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_images/" + userId + ".jpg");

        storageRef.delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    private void deleteUserFromFirestore(String userId, UserDeleteCallback callback) {
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getAllUsers(UserListDataCallback callback) {
        db.collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> users = task.getResult().toObjects(User.class);
                        callback.onDataReceived(users);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    public interface UserImageCallback {
        void onImageFetched(Uri imageUri);
        void onError(Exception e);
    }

    public interface FirestoreUserCallback {
        void onSuccess();
        void onFailure(Exception exception);
    }

    public interface UploadImageCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception exception);
    }

    public interface AddUserCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface UpdateUserCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface UserDataCallback {
        void onDataReceived(User user);
        void onError(Exception e);
    }
    public interface UserListDataCallback {
        void onDataReceived(List<User> users);
        void onError(Exception e);
    }
    public interface UserDeleteCallback {
        void onSuccess();
        void onError(Exception e);
    }
}