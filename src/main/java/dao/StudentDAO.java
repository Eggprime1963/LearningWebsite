package dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.Enrollment;
import model.Submission;
import model.User;

public class StudentDAO {

    // Get all students enrolled in a course
    public List<User> getStudentsByCourse(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Enrollment> query = em.createQuery(
                "SELECT e FROM Enrollment e WHERE e.course.idCourse = :courseId", Enrollment.class);
            query.setParameter("courseId", courseId);
            List<Enrollment> enrollments = query.getResultList();
            if (enrollments == null) {
                throw new RuntimeException("Enrollment table is null or not initialized.");
            }
            List<User> students = enrollments.stream()
                .map(Enrollment::getStudent)
                .filter(u -> "student".equals(u.getRole()))
                .collect(Collectors.toList());
            if (students.isEmpty()) {
                throw new RuntimeException("No students have enrolled in this course.");
            }
            return students;
        } finally {
            em.close();
        }
    }

    // Add a student to a course
    public void addStudentToCourse(int studentId, int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User student = em.find(User.class, studentId);
            model.Course course = em.find(model.Course.class, courseId);
            Enrollment enrollment = new Enrollment(student, course, java.time.LocalDateTime.now(), "active");
            em.persist(enrollment);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Could not add student to course", e);
        } finally {
            em.close();
        }
    }

    // Remove a student from a course
    public void removeStudentFromCourse(int studentId, int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<Enrollment> query = em.createQuery(
                "SELECT e FROM Enrollment e WHERE e.student.idStudent = :studentId AND e.course.idCourse = :courseId", Enrollment.class);
            query.setParameter("studentId", studentId);
            query.setParameter("courseId", courseId);
            List<Enrollment> enrollments = query.getResultList();
            for (Enrollment enrollment : enrollments) {
                em.remove(enrollment);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Could not remove student from course", e);
        } finally {
            em.close();
        }
    }

    // Get student grades for a specific course
    public Map<Integer, Double> getStudentGrades(List<User> students, model.Course selectedCourse) {
        Map<Integer, Double> studentGrades = new HashMap<>();
        SubmissionDAO submissionDAO = new SubmissionDAO();
        if (selectedCourse != null && students != null) {
            for (User student : students) {
                List<Submission> submissions = submissionDAO.getSubmissionsByStudentAndCourse(student.getId(), selectedCourse.getIdCourse());
                double avg = submissions.stream()
                    .filter(s -> s.getGrade() != null)
                    .mapToDouble(Submission::getGrade)
                    .average()
                    .orElse(Double.NaN);
                studentGrades.put(student.getId(), Double.isNaN(avg) ? null : avg);
            }
        }
        return studentGrades;
    }
}