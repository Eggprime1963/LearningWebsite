package dao;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.User;

public class UserDAO {
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    public void save(User user) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(user); // merge works for both new and existing users
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not save user", e);
        } finally {
            em.close();
        }
    }

    public Optional<User> findById(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            User user = em.find(User.class, id);
            return Optional.ofNullable(user);
        } finally {
            em.close();
        }
    }

    public Optional<User> findByEmailOrUsername(String identifier) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier", User.class);
            query.setParameter("identifier", identifier);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
     public Optional<User> findByGoogleId(String googleId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.googleId = :googleId", User.class);
            query.setParameter("googleId", googleId);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty(); // Return empty optional if no user is found
        } finally {
            em.close();
        }
    }

    public Optional<User> findUserById(int userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            User user = em.find(User.class, userId);
            return Optional.ofNullable(user);
        } finally {
            em.close();
        }
    }
    
    public Optional<User> authenticate(String identifier, String password) {
        Optional<User> userOpt = findByEmailOrUsername(identifier);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
    
    // Admin-related methods
    public List<User> getAllUsers() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u ORDER BY u.id", User.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<User> getUsersByRole(String role) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.role = :role ORDER BY u.username", User.class);
            query.setParameter("role", role);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public void updateUserRole(int userId, String newRole) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            User user = em.find(User.class, userId);
            if (user != null) {
                user.setRole(newRole);
                em.merge(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not update user role", e);
        } finally {
            em.close();
        }
    }
    
    public void deleteUser(int userId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            
            // First check if user exists
            User user = em.find(User.class, userId);
            if (user == null) {
                logger.log(Level.WARNING, "User with ID {0} not found for deletion", userId);
                transaction.rollback();
                return;
            }
            
            // Check for dependencies before deletion
            Long enrollmentCount = em.createQuery(
                "SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
                
            Long submissionCount = em.createQuery(
                "SELECT COUNT(s) FROM Submission s WHERE s.student.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
                
            Long courseCount = em.createQuery(
                "SELECT COUNT(c) FROM Course c WHERE c.idTeacher = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
            
            if (enrollmentCount > 0 || submissionCount > 0 || courseCount > 0) {
                logger.log(Level.WARNING, 
                    "Cannot delete user ID {0}: has {1} enrollments, {2} submissions, and {3} courses", 
                    new Object[]{userId, enrollmentCount, submissionCount, courseCount});
                throw new RuntimeException(
                    String.format("Cannot delete user: %d enrollments, %d submissions, and %d courses exist. " +
                                "Please remove dependencies first or use force delete.", 
                                enrollmentCount.intValue(), submissionCount.intValue(), courseCount.intValue()));
            }
            
            em.remove(user);
            transaction.commit();
            logger.log(Level.INFO, "Successfully deleted user with ID: {0}", userId);
            
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Error deleting user with ID: " + userId, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Force delete user with cascade - removes all related data
     * USE WITH EXTREME CAUTION - this will delete all user's data including grades!
     * 
     * @param userId the ID of the user to force delete
     */
    public void forceDeleteUserWithCascade(int userId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            
            // Step 1: Delete all submissions by this user
            int deletedSubmissions = em.createQuery(
                "DELETE FROM Submission s WHERE s.student.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
            
            // Step 2: Delete all enrollments by this user
            int deletedEnrollments = em.createQuery(
                "DELETE FROM Enrollment e WHERE e.student.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
            
            // Step 3: If user is a teacher, handle their courses
            List<Integer> courseIds = em.createQuery(
                "SELECT c.idCourse FROM Course c WHERE c.idTeacher = :userId", Integer.class)
                .setParameter("userId", userId)
                .getResultList();
            
            if (!courseIds.isEmpty()) {
                // Delete all submissions for courses taught by this user
                for (Integer courseId : courseIds) {
                    em.createQuery(
                        "DELETE FROM Submission s WHERE s.lecture IN " +
                        "(SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId) OR " +
                        "s.assignment IN (SELECT a FROM Assignment a WHERE a.lecture IN " +
                        "(SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId))")
                        .setParameter("courseId", courseId)
                        .executeUpdate();
                    
                    em.createQuery(
                        "DELETE FROM Assignment a WHERE a.lecture IN " +
                        "(SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId)")
                        .setParameter("courseId", courseId)
                        .executeUpdate();
                    
                    em.createQuery(
                        "DELETE FROM Lecture l WHERE l.course.idCourse = :courseId")
                        .setParameter("courseId", courseId)
                        .executeUpdate();
                    
                    em.createQuery(
                        "DELETE FROM Enrollment e WHERE e.course.idCourse = :courseId")
                        .setParameter("courseId", courseId)
                        .executeUpdate();
                }
                
                // Delete the courses
                int deletedCourses = em.createQuery(
                    "DELETE FROM Course c WHERE c.idTeacher = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
                
                logger.log(Level.WARNING, "Deleted {0} courses taught by user {1}", 
                    new Object[]{deletedCourses, userId});
            }
            
            // Step 4: Finally delete the user
            User user = em.find(User.class, userId);
            if (user != null) {
                em.remove(user);
            }
            
            transaction.commit();
            logger.log(Level.WARNING, 
                "FORCE DELETE completed for user ID {0}: " +
                "Deleted {1} submissions, {2} enrollments, and {3} courses", 
                new Object[]{userId, deletedSubmissions, deletedEnrollments, courseIds.size()});
            
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Error in force delete for user ID: " + userId, e);
            throw new RuntimeException("Failed to force delete user", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Check if a user can be safely deleted (has no dependencies)
     * 
     * @param userId the ID of the user to check
     * @return UserDeleteInfo object containing deletion feasibility and details
     */
    public UserDeleteInfo checkUserDeletionFeasibility(int userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Check for enrollments
            Long enrollmentCount = em.createQuery(
                "SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
                
            // Check for submissions
            Long submissionCount = em.createQuery(
                "SELECT COUNT(s) FROM Submission s WHERE s.student.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
                
            // Check for courses (if teacher)
            Long courseCount = em.createQuery(
                "SELECT COUNT(c) FROM Course c WHERE c.idTeacher = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
            
            boolean canSafelyDelete = enrollmentCount == 0 && submissionCount == 0 && courseCount == 0;
            
            return new UserDeleteInfo(canSafelyDelete, enrollmentCount.intValue(), 
                                    submissionCount.intValue(), courseCount.intValue());
            
        } finally {
            em.close();
        }
    }
    
    /**
     * Inner class to hold user deletion information
     */
    public static class UserDeleteInfo {
        private final boolean canSafelyDelete;
        private final int enrollmentCount;
        private final int submissionCount;
        private final int courseCount;
        
        public UserDeleteInfo(boolean canSafelyDelete, int enrollmentCount, 
                             int submissionCount, int courseCount) {
            this.canSafelyDelete = canSafelyDelete;
            this.enrollmentCount = enrollmentCount;
            this.submissionCount = submissionCount;
            this.courseCount = courseCount;
        }
        
        public boolean canSafelyDelete() { return canSafelyDelete; }
        public int getEnrollmentCount() { return enrollmentCount; }
        public int getSubmissionCount() { return submissionCount; }
        public int getCourseCount() { return courseCount; }
        
        public String getDeletionMessage() {
            if (canSafelyDelete) {
                return "User can be safely deleted.";
            } else {
                return String.format(
                    "Cannot delete user: %d enrollments, %d submissions, %d courses exist. " +
                    "Consider using force delete if you want to remove all related data.",
                    enrollmentCount, submissionCount, courseCount);
            }
        }
    }
    
    public long getUserCount() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM User u", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public long getUserCountByRole(String role) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.role = :role", Long.class);
            query.setParameter("role", role);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}