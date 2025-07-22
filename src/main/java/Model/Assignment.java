package model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "status")
    private String status;

    // Constructors
    public Assignment() {
    }

    public Assignment(Course course, Lecture lecture, String title, String description, LocalDateTime dueDate, String status) {
        this.course = course;
        this.lecture = lecture;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Lecture getLecture() {
        return lecture;
    }

    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

        // Helper methods for backward compatibility with int-based access
    public int getIdCourse() {
        return course != null ? course.getIdCourse() : 0;
    }
    
    public void setIdCourse(int idCourse) {
        // This is a placeholder - in practice, you'd need to load the Course entity
        // For now, we'll need to handle this in the DAO layer
    }
    
    public int getIdLecture() {
        return lecture != null ? lecture.getId() : 0;
    }
    
    public void setIdLecture(int idLecture) {
        // This is a placeholder - in practice, you'd need to load the Lecture entity
        // For now, we'll need to handle this in the DAO layer
    }

    public String getCourseName() {
        return course != null ? course.getName() : "";
    }

    public String getLectureTitle() {
        return lecture != null ? lecture.getTitle() : "";
    }

    // For JSP compatibility
    public java.util.Date getDueDateAsJavaUtilDate() {
        return dueDate != null ? java.sql.Timestamp.valueOf(dueDate) : null;
    }
}
