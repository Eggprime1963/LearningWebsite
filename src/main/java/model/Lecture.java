package model;

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
@Table(name = "Lectures")
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "title")
    private String title;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "content")
    private String content;

    @Column(name = "status")
    private String status;

    public Lecture() {
    }

    public Lecture(Course course, String title, String videoUrl, String content, String status) {
        this.course = course;
        this.title = title;
        this.videoUrl = videoUrl;
        this.content = content;
        this.status = status;
    }

    public Lecture(int id, Course course, String title, String videoUrl, String content, String status) {
        this.id = id;
        this.course = course;
        this.title = title;
        this.videoUrl = videoUrl;
        this.content = content;
        this.status = status;
    }
    

    // --- Getters and Setters ---

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

    // Helper method for backward compatibility with int-based access
    public int getIdCourse() {
        return course != null ? course.getIdCourse() : 0;
    }
    
    public void setIdCourse(int idCourse) {
        // This is a placeholder - in practice, you'd need to load the Course entity
        // For now, we'll need to handle this in the DAO layer
    }

   

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}