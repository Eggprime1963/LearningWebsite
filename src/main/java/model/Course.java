package model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
    
    @Column(name = "difficulty")
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "lecture_count")
    private int storedLectureCount;
    
    @Column(name = "price", precision = 10, scale = 2)
    private java.math.BigDecimal price = java.math.BigDecimal.ZERO;
    
    // Enum for difficulty levels
    public enum Difficulty {
        beginner, intermediate, advanced
    }
    
    // Transient field for teacher name (not stored in database)
    @Transient
    private String teacherName;
    
    // Transient field for lecture count (not stored in database)
    @Transient
    private Long lectureCount;
    
    // Transient field for enrollment status (not stored in database)
    @Transient
    private boolean enrolled = false;

    // Correct: mappedBy points to the 'course' field in the Lecture entity
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Lecture> lectures = new ArrayList<>();

    // Correct: mappedBy points to the 'course' field in the Assignment entity
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assignment> assignments = new ArrayList<>();
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments = new ArrayList<>();
    
    // One-to-one relationship with CourseMetadata
    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CourseMetadata courseMetadata;
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

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getStoredLectureCount() {
        return storedLectureCount;
    }

    public void setStoredLectureCount(int storedLectureCount) {
        this.storedLectureCount = storedLectureCount;
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

    public CourseMetadata getCourseMetadata() {
        return courseMetadata;
    }

    public void setCourseMetadata(CourseMetadata courseMetadata) {
        this.courseMetadata = courseMetadata;
    }

    public java.math.BigDecimal getPrice() {
        return price;
    }

    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }

    public boolean isEnrolled() {
        return enrolled;
    }

    public void setEnrolled(boolean enrolled) {
        this.enrolled = enrolled;
    }

}