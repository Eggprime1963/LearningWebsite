package dao;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Lecture;

public class LectureDAO {
    private static final Logger logger = Logger.getLogger(LectureDAO.class.getName());

    public Lecture saveLecture(Lecture lecture) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(lecture);
            transaction.commit();
            return lecture;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error saving lecture", e);
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateLecture(Lecture lecture) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(lecture);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error updating lecture", e);
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Lecture> findLectureById(int lectureId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Lecture lecture = em.find(Lecture.class, lectureId);
            return Optional.ofNullable(lecture);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding lecture with ID: " + lectureId, e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public void deleteLecture(int lectureId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            
            // First check if lecture exists
            Lecture lecture = em.find(Lecture.class, lectureId);
            if (lecture == null) {
                logger.log(Level.WARNING, "Lecture with ID {0} not found for deletion", lectureId);
                transaction.rollback();
                return;
            }
            
            // Check for dependencies before deletion
            Long assignmentCount = em.createQuery(
                "SELECT COUNT(a) FROM Assignment a WHERE a.lecture.idLecture = :lectureId", Long.class)
                .setParameter("lectureId", lectureId)
                .getSingleResult();
                
            Long submissionCount = em.createQuery(
                "SELECT COUNT(s) FROM Submission s WHERE s.lecture.idLecture = :lectureId", Long.class)
                .setParameter("lectureId", lectureId)
                .getSingleResult();
            
            if (assignmentCount > 0 || submissionCount > 0) {
                logger.log(Level.WARNING, 
                    "Cannot delete lecture ID {0}: has {1} assignments and {2} submissions", 
                    new Object[]{lectureId, assignmentCount, submissionCount});
                throw new RuntimeException(
                    String.format("Cannot delete lecture: %d assignments and %d submissions exist. " +
                                "Please remove dependencies first or use force delete.", 
                                assignmentCount.intValue(), submissionCount.intValue()));
            }
            
            em.remove(lecture);
            transaction.commit();
            logger.log(Level.INFO, "Successfully deleted lecture with ID: {0}", lectureId);
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error deleting lecture with ID: " + lectureId, e);
            throw new RuntimeException("Failed to delete lecture: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Force delete lecture with cascade - removes all related data
     * USE WITH CAUTION - this will delete all assignments and submissions!
     * 
     * @param lectureId the ID of the lecture to force delete
     */
    public void forceDeleteLectureWithCascade(int lectureId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            
            // Step 1: Delete all submissions for assignments in this lecture
            int deletedAssignmentSubmissions = em.createQuery(
                "DELETE FROM Submission s WHERE s.assignment IN " +
                "(SELECT a FROM Assignment a WHERE a.lecture.idLecture = :lectureId)")
                .setParameter("lectureId", lectureId)
                .executeUpdate();
            
            // Step 2: Delete all direct submissions for this lecture
            int deletedLectureSubmissions = em.createQuery(
                "DELETE FROM Submission s WHERE s.lecture.idLecture = :lectureId")
                .setParameter("lectureId", lectureId)
                .executeUpdate();
            
            // Step 3: Delete all assignments in this lecture
            int deletedAssignments = em.createQuery(
                "DELETE FROM Assignment a WHERE a.lecture.idLecture = :lectureId")
                .setParameter("lectureId", lectureId)
                .executeUpdate();
            
            // Step 4: Finally delete the lecture
            Lecture lecture = em.find(Lecture.class, lectureId);
            if (lecture != null) {
                em.remove(lecture);
            }
            
            transaction.commit();
            logger.log(Level.WARNING, 
                "FORCE DELETE completed for lecture ID {0}: " +
                "Deleted {1} assignment submissions, {2} lecture submissions, {3} assignments", 
                new Object[]{lectureId, deletedAssignmentSubmissions, deletedLectureSubmissions, deletedAssignments});
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error in force delete for lecture ID: " + lectureId, e);
            throw new RuntimeException("Failed to force delete lecture", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Check if a lecture can be safely deleted (has no dependencies)
     * 
     * @param lectureId the ID of the lecture to check
     * @return LectureDeleteInfo object containing deletion feasibility and details
     */
    public LectureDeleteInfo checkLectureDeletionFeasibility(int lectureId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Check for assignments
            Long assignmentCount = em.createQuery(
                "SELECT COUNT(a) FROM Assignment a WHERE a.lecture.idLecture = :lectureId", Long.class)
                .setParameter("lectureId", lectureId)
                .getSingleResult();
                
            // Check for submissions
            Long submissionCount = em.createQuery(
                "SELECT COUNT(s) FROM Submission s WHERE s.lecture.idLecture = :lectureId OR " +
                "s.assignment IN (SELECT a FROM Assignment a WHERE a.lecture.idLecture = :lectureId)", Long.class)
                .setParameter("lectureId", lectureId)
                .getSingleResult();
            
            boolean canSafelyDelete = assignmentCount == 0 && submissionCount == 0;
            
            return new LectureDeleteInfo(canSafelyDelete, assignmentCount.intValue(), 
                                       submissionCount.intValue());
            
        } finally {
            em.close();
        }
    }
    
    /**
     * Inner class to hold lecture deletion information
     */
    public static class LectureDeleteInfo {
        private final boolean canSafelyDelete;
        private final int assignmentCount;
        private final int submissionCount;
        
        public LectureDeleteInfo(boolean canSafelyDelete, int assignmentCount, int submissionCount) {
            this.canSafelyDelete = canSafelyDelete;
            this.assignmentCount = assignmentCount;
            this.submissionCount = submissionCount;
        }
        
        public boolean canSafelyDelete() { return canSafelyDelete; }
        public int getAssignmentCount() { return assignmentCount; }
        public int getSubmissionCount() { return submissionCount; }
        
        public String getDeletionMessage() {
            if (canSafelyDelete) {
                return "Lecture can be safely deleted.";
            } else {
                return String.format(
                    "Cannot delete lecture: %d assignments and %d submissions exist. " +
                    "Consider using force delete if you want to remove all related data.",
                    assignmentCount, submissionCount);
            }
        }
    }

    public List<Lecture> getLecturesByCourseId(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Lecture> query = em.createQuery(
                "SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId ORDER BY l.id ASC",
                Lecture.class
            );
            query.setParameter("courseId", courseId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving lectures for course ID: " + courseId, e);
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<Lecture> getLecturesByCourseForTeacher(int courseId, int teacherId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Lecture> query = em.createQuery(
                "SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId AND l.course.idTeacher = :teacherId ORDER BY l.id ASC",
                Lecture.class
            );
            query.setParameter("courseId", courseId);
            query.setParameter("teacherId", teacherId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving lectures for course ID: " + courseId + " and teacher ID: " + teacherId, e);
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<Lecture> getLecturesByCourseForStudent(int courseId, int studentId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Lecture> query = em.createQuery(
                "SELECT l FROM Lecture l JOIN l.course.enrollments e WHERE l.course.idCourse = :courseId AND e.student.id = :studentId AND l.status = 'active' ORDER BY l.id ASC",
                Lecture.class
            );
            query.setParameter("courseId", courseId);
            query.setParameter("studentId", studentId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving lectures for course ID: " + courseId + " and student ID: " + studentId, e);
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Lecture getLectureById(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Lecture.class, id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving lecture with ID: " + id, e);
            return null;
        } finally {
            em.close();
        }
    }

    public List<Lecture> getAllLectures() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Lecture> query = em.createQuery(
                "SELECT l FROM Lecture l ORDER BY l.course.idCourse, l.id",
                Lecture.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving all lectures", e);
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }
}
