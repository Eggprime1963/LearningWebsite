package dao;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Assignment;

public class AssignmentDAO {
    private static final Logger logger = Logger.getLogger(AssignmentDAO.class.getName());

    public Assignment saveAssignment(Assignment assignment) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(assignment);
            transaction.commit();
            return assignment;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not save assignment", e);
        } finally {
            em.close();
        }
    }

    public void updateAssignment(Assignment assignment) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(assignment);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not update assignment", e);
        } finally {
            em.close();
        }
    }
    
    public Optional<Assignment> getAssignmentById(int assignmentId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Assignment.class, assignmentId));
        } finally {
            em.close();
        }
    }

    public void deleteAssignment(int assignmentId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Assignment assignment = em.find(Assignment.class, assignmentId);
            if (assignment != null) {
                em.remove(assignment);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not delete assignment", e);
        } finally {
            em.close();
        }
    }

  

    public void deleteByCourseId(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Assignment a WHERE a.course.id = :courseId")
              .setParameter("courseId", courseId)
              .executeUpdate();
            tx.commit();
        } finally {
            em.close();
        }
    }

    public List<Assignment> getAssignmentsByLecture(int courseId,int lectureId) //them courseID
    {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Assignment> query = em.createQuery(
                "SELECT a FROM Assignment a WHERE a.lecture.id = :lectureId AND a.status = 'active'",// sua lai cau query
                Assignment.class
            );
            query.setParameter("lectureId", lectureId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving assignments for lecture ID: " + lectureId, e);
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }
    public List<Assignment> getAssignmentsByCourseId(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Assignment> query = em.createQuery(
                "SELECT a FROM Assignment a WHERE a.idCourse = :courseId",
                Assignment.class
            );
            query.setParameter("courseId", courseId);
            List<Assignment> assignments = query.getResultList();
            logger.log(Level.INFO, "Found {0} assignments for courseId: {1}", 
                new Object[]{assignments.size(), courseId});
            return assignments;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving assignments for courseId: {0}, Error: {1}", 
                new Object[]{courseId, e.getMessage()});
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }
}