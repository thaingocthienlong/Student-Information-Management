package com.app.studentinformationmanagement.services;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthService {
    private FirebaseAuth mAuth;
    private Context context;

    public AuthService(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
    }

    public void loginUser(String email, String password, AuthResultHandler handler) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            handler.onFailure(new IllegalArgumentException("Email and password cannot be empty"));
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        handler.onSuccess(user);
                    } else {
                        handler.onFailure(task.getException());
                    }
                });
    }

    public void getUserRoleByEmail(String email, RoleResultHandler handler) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", email).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String role = queryDocumentSnapshots.getDocuments().get(0).getString("role");
                        handler.onSuccess(role);
                    } else {
                        handler.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(handler::onFailure);
    }

    public void getUserRole(FirebaseUser user, RoleResultHandler handler) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    handler.onSuccess(document.getString("Role"));
                } else {
                    handler.onFailure(new Exception("Role not found"));
                }
            } else {
                handler.onFailure(task.getException());
            }
        });
    }

    public void getUserIdByEmail(String email, UserIdByEmailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", email).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String userId = queryDocumentSnapshots.getDocuments().get(0).getString("id");
                        callback.onSuccess(userId);
                    } else {}
                })
                .addOnFailureListener(e -> callback.onError(e));
    }


    public void createUserWithEmailPassword(String email, String password, AuthResultHandler handler) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        handler.onSuccess(user);
                    } else {
                        handler.onFailure(task.getException());
                    }
                });
    }

    public interface UserIdByEmailCallback {
        void onSuccess(String userId);
        void onError(Exception e);
    }

    public interface RoleResultHandler {
        void onSuccess(String role);
        void onFailure(Exception exception);
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void signOut() {
        mAuth.signOut();
    }

    public interface AuthResultHandler {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception exception);
    }
}
