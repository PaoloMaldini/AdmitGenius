package com.admitgenius.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "school_programs")
public class SchoolProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    @Column(nullable = false)
    private String name;

    @Column(name = "department")
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "degree_type")
    private DegreeLevel degreeLevel;

    @Column(name = "duration_years")
    private Integer duration; // in years or semesters

    private String tuitionFee;

    private Boolean scholarshipAvailable = false;

    @Column(columnDefinition = "TEXT")
    private String admissionRequirements;

    @ElementCollection
    @CollectionTable(name = "program_keywords", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @OneToMany(mappedBy = "program")
    private List<EssayRequirement> essayRequirements = new ArrayList<>();

    public enum DegreeLevel {
        BACHELOR, MASTER, PHD
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public DegreeLevel getDegreeLevel() {
        return degreeLevel;
    }

    public void setDegreeLevel(DegreeLevel degreeLevel) {
        this.degreeLevel = degreeLevel;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getTuitionFee() {
        return tuitionFee;
    }

    public void setTuitionFee(String tuitionFee) {
        this.tuitionFee = tuitionFee;
    }

    public Boolean getScholarshipAvailable() {
        return scholarshipAvailable;
    }

    public void setScholarshipAvailable(Boolean scholarshipAvailable) {
        this.scholarshipAvailable = scholarshipAvailable;
    }

    public String getAdmissionRequirements() {
        return admissionRequirements;
    }

    public void setAdmissionRequirements(String admissionRequirements) {
        this.admissionRequirements = admissionRequirements;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<EssayRequirement> getEssayRequirements() {
        return essayRequirements;
    }

    public void setEssayRequirements(List<EssayRequirement> essayRequirements) {
        this.essayRequirements = essayRequirements;
    }
}