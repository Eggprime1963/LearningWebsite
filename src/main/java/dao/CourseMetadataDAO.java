package dao;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.CourseMetadata;

/**
 * Data Access Object for CourseMetadata entity operations.
 * Provides CRUD operations and specialized queries for course metadata.
 */
public class CourseMetadataDAO {
    
    private static final Logger logger = Logger.getLogger(CourseMetadataDAO.class.getName());
    
    /**
     * Saves a new CourseMetadata entity to the database.
     */
    public void saveCourseMetadata(CourseMetadata courseMetadata) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(courseMetadata);
            em.getTransaction().commit();
            logger.info("CourseMetadata saved successfully for course ID: " + courseMetadata.getCourseId());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error saving CourseMetadata for course ID: " + courseMetadata.getCourseId(), e);
            throw new RuntimeException("Failed to save course metadata", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Retrieves CourseMetadata by course ID.
     */
    public CourseMetadata getCourseMetadataByCourseId(Integer courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CourseMetadata> query = em.createQuery(
                "SELECT cm FROM CourseMetadata cm WHERE cm.courseId = :courseId", 
                CourseMetadata.class);
            query.setParameter("courseId", courseId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            logger.info("No metadata found for course ID: " + courseId);
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving CourseMetadata for course ID: " + courseId, e);
            throw new RuntimeException("Failed to retrieve course metadata", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Retrieves all CourseMetadata entities.
     */
    public List<CourseMetadata> getAllCourseMetadata() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CourseMetadata> query = em.createQuery(
                "SELECT cm FROM CourseMetadata cm ORDER BY cm.courseId", 
                CourseMetadata.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving all CourseMetadata", e);
            throw new RuntimeException("Failed to retrieve course metadata list", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Updates an existing CourseMetadata entity.
     */
    public void updateCourseMetadata(CourseMetadata courseMetadata) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(courseMetadata);
            em.getTransaction().commit();
            logger.info("CourseMetadata updated successfully for course ID: " + courseMetadata.getCourseId());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error updating CourseMetadata for course ID: " + courseMetadata.getCourseId(), e);
            throw new RuntimeException("Failed to update course metadata", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Deletes CourseMetadata by course ID.
     */
    public boolean deleteCourseMetadataByCourseId(Integer courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            int deletedCount = em.createQuery(
                "DELETE FROM CourseMetadata cm WHERE cm.courseId = :courseId")
                .setParameter("courseId", courseId)
                .executeUpdate();
            em.getTransaction().commit();
            
            boolean deleted = deletedCount > 0;
            if (deleted) {
                logger.info("CourseMetadata deleted successfully for course ID: " + courseId);
            } else {
                logger.info("No CourseMetadata found to delete for course ID: " + courseId);
            }
            return deleted;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Error deleting CourseMetadata for course ID: " + courseId, e);
            throw new RuntimeException("Failed to delete course metadata", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Retrieves CourseMetadata by skill level.
     */
    public List<CourseMetadata> getCourseMetadataBySkillLevel(String skillLevel) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CourseMetadata> query = em.createQuery(
                "SELECT cm FROM CourseMetadata cm WHERE cm.skillLevel = :skillLevel ORDER BY cm.courseId", 
                CourseMetadata.class);
            query.setParameter("skillLevel", skillLevel);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving CourseMetadata by skill level: " + skillLevel, e);
            throw new RuntimeException("Failed to retrieve course metadata by skill level", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Retrieves CourseMetadata with estimated hours within a range.
     */
    public List<CourseMetadata> getCourseMetadataByHoursRange(Integer minHours, Integer maxHours) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<CourseMetadata> query = em.createQuery(
                "SELECT cm FROM CourseMetadata cm WHERE cm.estimatedHours BETWEEN :minHours AND :maxHours ORDER BY cm.estimatedHours", 
                CourseMetadata.class);
            query.setParameter("minHours", minHours);
            query.setParameter("maxHours", maxHours);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving CourseMetadata by hours range: " + minHours + "-" + maxHours, e);
            throw new RuntimeException("Failed to retrieve course metadata by hours range", e);
        } finally {
            em.close();
        }
    }
}
