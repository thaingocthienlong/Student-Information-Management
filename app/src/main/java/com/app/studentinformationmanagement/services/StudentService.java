package com.app.studentinformationmanagement.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.app.studentinformationmanagement.AddCertificateActivity;
import com.app.studentinformationmanagement.models.Student;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentService {
    private Context context;
    private FirebaseFirestore db;

    public StudentService() {
        db = FirebaseFirestore.getInstance();
    }

    public void addStudent(Student student, AddStudentCallback callback) {
        db.collection("students").document(student.getId()).set(student)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }

    public void getStudentById(String studentId, StudentDataCallback callback) {
        db.collection("students").document(studentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Student student = documentSnapshot.toObject(Student.class);
                        callback.onDataReceived(student);
                    } else {
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }

    public void updateStudent(Student student, UpdateStudentCallback callback) {
        db.collection("students").document(student.getId())
                .set(student)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e));
    }

    public void deleteStudent(String studentId, StudentDeleteCallback callback) {
        db.collection("students").document(studentId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
    }

    public void getAllStudents(StudentListDataCallback callback) {
        db.collection("students").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Student> students = task.getResult().toObjects(Student.class);
                        callback.onDataReceived(students);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    public interface AddStudentCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface UpdateStudentCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface StudentDataCallback {
        void onDataReceived(Student student);
        void onError(Exception e);
    }
    public interface StudentListDataCallback {
        void onDataReceived(List<Student> students);
        void onError(Exception e);
    }
    public interface StudentDeleteCallback {
        void onSuccess();
        void onError(Exception e);
    }
}
