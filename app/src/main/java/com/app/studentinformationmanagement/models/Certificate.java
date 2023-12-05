package com.app.studentinformationmanagement.models;

import java.util.Date;

public class Certificate {
    private String id;
    private String name;
    private String issuingAuthority;
    private String dateOfIssue;

    public Certificate() {}
    // Constructor
    public Certificate(String id, String name, String issuingAuthority, String dateOfIssue) {
        this.id = id;
        this.name = name;
        this.issuingAuthority = issuingAuthority;
        this.dateOfIssue = dateOfIssue;
    }

    // Getters and Setters
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
        this.name = name;
    }

    public String getIssuingAuthority() {
        return issuingAuthority;
    }

    public void setIssuingAuthority(String issuingAuthority) {
        this.issuingAuthority = issuingAuthority;
    }

    public String getDateOfIssue() {
        return dateOfIssue;
    }

    public void setDateOfIssue(String dateOfIssue) {
        this.dateOfIssue = dateOfIssue;
    }

    // Additional methods or logic can be added as needed
}
