package com.app.studentinformationmanagement.models;

import java.util.List;

public class Student {
    private String id;
    private String name;
    private int age;
    private String phoneNumber;
    private List<Certificate> certificates;

    public Student() {}

    public Student(String id, String name, int age, String phoneNumber, List<Certificate> certificates) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.certificates = certificates;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age <= 0) {
            throw new IllegalArgumentException("Age must be positive");
        }
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("\\+?\\d+")) { // Simple regex for phone number validation
            throw new IllegalArgumentException("Invalid phone number");
        }
        this.phoneNumber = phoneNumber;
    }

    public List<Certificate> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<Certificate> certificates) {
        this.certificates = certificates;
    }

    public void addCertificate(Certificate certificate) {
        this.certificates.add(certificate);
    }

    public void removeCertificate(Certificate certificate) {
        this.certificates.remove(certificate);
    }

    public void updateCertificate(Certificate oldCertificate, Certificate newCertificate) {
        int index = this.certificates.indexOf(oldCertificate);
        if (index != -1) {
            this.certificates.set(index, newCertificate);
        }
    }
}
