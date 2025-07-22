package model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;



    
@Entity
@Table(name = "Courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int idCourse;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "teacher_id")
    private int idTeacher; 

    @Column(name = "image")
    private String image;
    
    // Transient field for teacher name (not stored in database)
    @Transient
    private String teacherName;
    
    // Transient field for lecture count (not stored in database)
    @Transient
    private Long lectureCount;

    // Correct: mappedBy points to the 'course' field in the Lecture entity
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Lecture> lectures = new ArrayList<>();

    // Correct: mappedBy points to the 'course' field in the Assignment entity
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assignment> assignments = new ArrayList<>();
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments = new ArrayList<>();
    // Constructors, Getters, and Setters remain the same...
    public Course() {}

    public Course(String name, String description, int idTeacher, String image) {
        this.name = name;
        this.description = description;
        this.idTeacher = idTeacher;
        this.image=image;
        this.lectures = new ArrayList<>();
        this.assignments = new ArrayList<>();
        this.enrollments = new ArrayList<>();
    }

    public int getIdTeacher() {
        return idTeacher;
    }

    public void setIdTeacher(int idTeacher) {
        this.idTeacher = idTeacher;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
   
    public int getIdCourse() {
        return idCourse;
    }

    public void setIdCourse(int idCourse) {
        this.idCourse = idCourse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Long getLectureCount() {
        return lectureCount;
    }

    public void setLectureCount(Long lectureCount) {
        this.lectureCount = lectureCount;
    }


    public List<Lecture> getLectures() {
        return lectures;
    }

    public void setLectures(List<Lecture> lectures) {
        this.lectures = lectures;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    // Helper method to get students through enrollments
    public List<User> getStudents() {
        return enrollments.stream()
                .map(Enrollment::getStudent)
                .collect(java.util.stream.Collectors.toList());
    }

    public void setStudents(List<User> students) {
        this.enrollments = students.stream()
                .map(student -> new Enrollment(student, this, java.time.LocalDateTime.now(), ""))
                .collect(java.util.stream.Collectors.toList());
    }

}