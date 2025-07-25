package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Entity representing additional metadata for courses including prerequisites,
 * learning outcomes, estimated hours, and skill level information.
 */
@Entity
@Table(name = "CourseMetadata")
public class CourseMetadata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "course_id", nullable = false, unique = true)
    private Integer courseId;
    
    @Column(name = "prerequisites", columnDefinition = "TEXT")
    private String prerequisites;
    
    @Column(name = "learning_outcomes", columnDefinition = "TEXT")
    private String learningOutcomes;
    
    @Column(name = "estimated_hours", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer estimatedHours = 0;
    
    @Column(name = "skill_level", length = 50)
    private String skillLevel;
    
    // One-to-one relationship with Course
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;
    
    // Default constructor
    public CourseMetadata() {}
    
    // Constructor with essential fields
    public CourseMetadata(Integer courseId, String prerequisites, String learningOutcomes, 
                         Integer estimatedHours, String skillLevel) {
        this.courseId = courseId;
        this.prerequisites = prerequisites;
        this.learningOutcomes = learningOutcomes;
        this.estimatedHours = estimatedHours;
        this.skillLevel = skillLevel;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }
    
    public String getPrerequisites() {
        return prerequisites;
    }
    
    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }
    
    public String getLearningOutcomes() {
        return learningOutcomes;
    }
    
    public void setLearningOutcomes(String learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
    }
    
    public Integer getEstimatedHours() {
        return estimatedHours;
    }
    
    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }
    
    public String getSkillLevel() {
        return skillLevel;
    }
    
    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }
    
    public Course getCourse() {
        return course;
    }
    
    public void setCourse(Course course) {
        this.course = course;
    }
    
    @Override
    public String toString() {
        return "CourseMetadata{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", prerequisites='" + prerequisites + '\'' +
                ", learningOutcomes='" + learningOutcomes + '\'' +
                ", estimatedHours=" + estimatedHours +
                ", skillLevel='" + skillLevel + '\'' +
                '}';
    }
}
