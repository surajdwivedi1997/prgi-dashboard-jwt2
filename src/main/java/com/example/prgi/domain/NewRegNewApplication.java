package com.example.prgi.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "new_reg_new_application")
public class NewRegNewApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="application_number")
    private String applicationNumber;

    @Column(name="proposed_titles")
    private String proposedTitles;

    @Column(name="publication_state")
    private String publicationState;

    @Column(name="publication_district")
    private String publicationDistrict;

    @Column(name="application_status")
    private String applicationStatus;

    @Column(name="current_status")
    private String currentStatus;

    @Column(name="officer_name")
    private String officerName;

    @Column(name="dm_application_status")
    private String dmApplicationStatus;

    @Column(name="application_submission_date")
    private LocalDate applicationSubmissionDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }
    public String getProposedTitles() { return proposedTitles; }
    public void setProposedTitles(String proposedTitles) { this.proposedTitles = proposedTitles; }
    public String getPublicationState() { return publicationState; }
    public void setPublicationState(String publicationState) { this.publicationState = publicationState; }
    public String getPublicationDistrict() { return publicationDistrict; }
    public void setPublicationDistrict(String publicationDistrict) { this.publicationDistrict = publicationDistrict; }
    public String getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(String applicationStatus) { this.applicationStatus = applicationStatus; }
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    public String getOfficerName() { return officerName; }
    public void setOfficerName(String officerName) { this.officerName = officerName; }
    public String getDmApplicationStatus() { return dmApplicationStatus; }
    public void setDmApplicationStatus(String dmApplicationStatus) { this.dmApplicationStatus = dmApplicationStatus; }
    public LocalDate getApplicationSubmissionDate() { return applicationSubmissionDate; }
    public void setApplicationSubmissionDate(LocalDate applicationSubmissionDate) { this.applicationSubmissionDate = applicationSubmissionDate; }
}
