package dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Course;
import model.Enrollment;
import model.User;

public class EnrollmentDAO {
    private static final Logger logger = Logger.getLogger(EnrollmentDAO.class.getName());
    public void save(Enrollment enrollment) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            logger.log(Level.INFO, "Attempting to save enrollment for userId: {0}, courseId: {1}", 
                new Object[]{enrollment.getStudent().getId(), enrollment.getCourse().getIdCourse()});
            transaction.begin();
            
            // Get fresh references to avoid detached entity issues
            User student = em.find(User.class, enrollment.getStudent().getId());
            Course course = em.find(Course.class, enrollment.getCourse().getIdCourse());
            
            if (student == null) {
                throw new RuntimeException("Student not found with ID: " + enrollment.getStudent().getId());
            }
            if (course == null) {
                throw new RuntimeException("Course not found with ID: " + enrollment.getCourse().getIdCourse());
            }
            
            // Create new enrollment with fresh entities
            Enrollment newEnrollment = new Enrollment();
            newEnrollment.setId(enrollment.getId());
            newEnrollment.setStudent(student);
            newEnrollment.setCourse(course);
            newEnrollment.setEnrollmentDate(enrollment.getEnrollmentDate());
            newEnrollment.setStatus(enrollment.getStatus());
            
            em.persist(newEnrollment);
            transaction.commit();
            logger.log(Level.INFO, "Successfully saved enrollment for userId: {0}, courseId: {1}", 
                new Object[]{student.getId(), course.getIdCourse()});
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Failed to save enrollment for userId: {0}, courseId: {1}, Error: {2}", 
                new Object[]{enrollment.getStudent().getId(), enrollment.getCourse().getIdCourse(), e.getMessage()});
            throw new RuntimeException("Could not save enrollment", e);
        } finally {
            em.close();
        }
    }
    public void update(Enrollment enrollment) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(enrollment);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not update enrollment", e);
        } finally {
            em.close();
        }
    }
    
    public Enrollment findById(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Enrollment.class, id);
        } finally {
            em.close();
        }
    }

    public List<Enrollment> findByCourseId(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Enrollment> query = em.createQuery("SELECT e FROM Enrollment e WHERE e.course.id = :courseId", Enrollment.class);
            query.setParameter("courseId", courseId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }



    public void delete(Enrollment enrollment) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(enrollment) ? enrollment : em.merge(enrollment));
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not delete enrollment", e);
        } finally {
            em.close();
        }
    }

    public void deleteByCourseId(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Enrollment e WHERE e.course.id = :courseId")
              .setParameter("courseId", courseId)
              .executeUpdate();
            tx.commit();
        } finally {
            em.close();
        }
        
    }
    public List<User> getStudentsByCourseId(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT e.student FROM Enrollment e WHERE e.course.idCourse = :courseId AND e.student.role = 'student'",
                User.class
            );
            query.setParameter("courseId", courseId);
            List<User> students = query.getResultList();
            logger.log(Level.INFO, "Total students found for courseId {0}: {1}", 
                new Object[]{courseId, students.size()});
            return students;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving students for courseId: {0}, Error: {1}", 
                new Object[]{courseId, e.getMessage()});
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<Enrollment> getEnrollmentsByCourseId(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Enrollment> query = em.createQuery(
                "SELECT e FROM Enrollment e WHERE e.course.idCourse = :courseId AND e.student.role = 'student'",
                Enrollment.class
            );
            query.setParameter("courseId", courseId);
            List<Enrollment> enrollments = query.getResultList();
            logger.log(Level.INFO, "Total enrollments found for courseId {0}: {1}", 
                new Object[]{courseId, enrollments.size()});
            return enrollments;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving enrollments for courseId: {0}, Error: {1}", 
                new Object[]{courseId, e.getMessage()});
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }
    
    public List<Enrollment> getAllEnrollments() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Enrollment> query = em.createQuery("SELECT e FROM Enrollment e", Enrollment.class);
            List<Enrollment> enrollments = query.getResultList();
            logger.log(Level.INFO, "Found {0} total enrollments in database", enrollments.size());
            return enrollments;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all enrollments: {0}", e.getMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }
    
    public boolean isUserEnrolledInCourse(int userId, int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :userId AND e.course.idCourse = :courseId",
                Long.class
            );
            query.setParameter("userId", userId);
            query.setParameter("courseId", courseId);
            Long count = query.getSingleResult();
            logger.log(Level.INFO, "Enrollment check for userId {0} and courseId {1}: {2}", 
                new Object[]{userId, courseId, count > 0});
            return count > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking enrollment for userId: {0}, courseId: {1}, Error: {2}", 
                new Object[]{userId, courseId, e.getMessage()});
            return false;
        } finally {
            em.close();
        }
    }
}