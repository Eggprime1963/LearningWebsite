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
            
            // First check if assignment exists
            Assignment assignment = em.find(Assignment.class, assignmentId);
            if (assignment == null) {
                logger.log(Level.WARNING, "Assignment with ID {0} not found for deletion", assignmentId);
                transaction.rollback();
                return;
            }
            
            // Check for dependencies before deletion
            Long submissionCount = em.createQuery(
                "SELECT COUNT(s) FROM Submission s WHERE s.assignment.idAssignment = :assignmentId", Long.class)
                .setParameter("assignmentId", assignmentId)
                .getSingleResult();
            
            if (submissionCount > 0) {
                logger.log(Level.WARNING, 
                    "Cannot delete assignment ID {0}: has {1} submissions", 
                    new Object[]{assignmentId, submissionCount});
                throw new RuntimeException(
                    String.format("Cannot delete assignment: %d submissions exist. " +
                                "Please remove submissions first or use force delete.", 
                                submissionCount.intValue()));
            }
            
            em.remove(assignment);
            transaction.commit();
            logger.log(Level.INFO, "Successfully deleted assignment with ID: {0}", assignmentId);
            
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Error deleting assignment with ID: " + assignmentId, e);
            throw new RuntimeException("Failed to delete assignment: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Force delete assignment with cascade - removes all related submissions
     * USE WITH CAUTION - this will delete all student submissions and grades!
     * 
     * @param assignmentId the ID of the assignment to force delete
     */
    public void forceDeleteAssignmentWithCascade(int assignmentId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            
            // Step 1: Delete all submissions for this assignment
            int deletedSubmissions = em.createQuery(
                "DELETE FROM Submission s WHERE s.assignment.idAssignment = :assignmentId")
                .setParameter("assignmentId", assignmentId)
                .executeUpdate();
            
            // Step 2: Finally delete the assignment
            Assignment assignment = em.find(Assignment.class, assignmentId);
            if (assignment != null) {
                em.remove(assignment);
            }
            
            transaction.commit();
            logger.log(Level.WARNING, 
                "FORCE DELETE completed for assignment ID {0}: Deleted {1} submissions", 
                new Object[]{assignmentId, deletedSubmissions});
            
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Error in force delete for assignment ID: " + assignmentId, e);
            throw new RuntimeException("Failed to force delete assignment", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Check if an assignment can be safely deleted (has no dependencies)
     * 
     * @param assignmentId the ID of the assignment to check
     * @return AssignmentDeleteInfo object containing deletion feasibility and details
     */
    public AssignmentDeleteInfo checkAssignmentDeletionFeasibility(int assignmentId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Check for submissions
            Long submissionCount = em.createQuery(
                "SELECT COUNT(s) FROM Submission s WHERE s.assignment.idAssignment = :assignmentId", Long.class)
                .setParameter("assignmentId", assignmentId)
                .getSingleResult();
            
            boolean canSafelyDelete = submissionCount == 0;
            
            return new AssignmentDeleteInfo(canSafelyDelete, submissionCount.intValue());
            
        } finally {
            em.close();
        }
    }
    
    /**
     * Inner class to hold assignment deletion information
     */
    public static class AssignmentDeleteInfo {
        private final boolean canSafelyDelete;
        private final int submissionCount;
        
        public AssignmentDeleteInfo(boolean canSafelyDelete, int submissionCount) {
            this.canSafelyDelete = canSafelyDelete;
            this.submissionCount = submissionCount;
        }
        
        public boolean canSafelyDelete() { return canSafelyDelete; }
        public int getSubmissionCount() { return submissionCount; }
        
        public String getDeletionMessage() {
            if (canSafelyDelete) {
                return "Assignment can be safely deleted.";
            } else {
                return String.format(
                    "Cannot delete assignment: %d submissions exist. " +
                    "Consider using force delete if you want to remove all student work.",
                    submissionCount);
            }
        }
    }

  

    public void deleteByCourseId(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Assignment a WHERE a.course.idCourse = :courseId")
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
                "SELECT a FROM Assignment a WHERE a.course.idCourse = :courseId",
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