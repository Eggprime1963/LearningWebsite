package dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Course;

public class CourseDAO {
    private static final Logger logger = Logger.getLogger(CourseDAO.class.getName());

    public Course saveCourse(Course course) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(course);
            transaction.commit();
            return course;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not save course", e);
        } finally {
            em.close();
        }
    }

    public Optional<Course> findCourseById(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Course.class, courseId));
        } finally {
            em.close();
        }
    }

    public List<Course> getCourses() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Course> query = em.createQuery("SELECT c FROM Course c", Course.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    // Method to get courses with teacher names and lecture count
    public List<Object[]> getCoursesWithTeacherNames() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT c, u.username, " +
                         "(SELECT COUNT(l) FROM Lecture l WHERE l.course.idCourse = c.idCourse) " +
                         "FROM Course c JOIN User u ON c.idTeacher = u.id";
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public void updateCourse(Course course) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(course);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not update course", e);
        } finally {
            em.close();
        }
    }

    public void deleteCourse(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            
            // First check if course exists
            Course course = em.find(Course.class, courseId);
            if (course == null) {
                logger.log(Level.WARNING, "Course with ID {0} not found for deletion", courseId);
                transaction.rollback();
                return;
            }
            
            // Check for dependencies before deletion
            Long enrollmentCount = em.createQuery(
                "SELECT COUNT(e) FROM Enrollment e WHERE e.course.idCourse = :courseId", Long.class)
                .setParameter("courseId", courseId)
                .getSingleResult();
                
            Long lectureCount = em.createQuery(
                "SELECT COUNT(l) FROM Lecture l WHERE l.course.idCourse = :courseId", Long.class)
                .setParameter("courseId", courseId)
                .getSingleResult();
            
            if (enrollmentCount > 0 || lectureCount > 0) {
                logger.log(Level.WARNING, 
                    "Cannot delete course ID {0}: has {1} enrollments and {2} lectures", 
                    new Object[]{courseId, enrollmentCount, lectureCount});
                throw new RuntimeException(
                    String.format("Cannot delete course: %d students enrolled and %d lectures exist. " +
                                "Please remove enrollments and lectures first.", 
                                enrollmentCount.intValue(), lectureCount.intValue()));
            }
            
            em.remove(course);
            transaction.commit();
            logger.log(Level.INFO, "Successfully deleted course with ID: {0}", courseId);
            
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Error deleting course with ID: " + courseId, e);
            throw new RuntimeException("Failed to delete course: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Soft delete approach - marks course as inactive instead of physical deletion
     * Note: This requires adding an 'active' boolean field to the Course entity
     * For now, this method is commented out until the field is added
     * 
     * @param courseId the ID of the course to deactivate
     */
    /*
    public void deactivateCourse(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            
            Course course = em.find(Course.class, courseId);
            if (course != null) {
                course.setActive(false); // Requires adding 'active' field to Course entity
                em.merge(course);
                logger.log(Level.INFO, "Course with ID {0} has been deactivated", courseId);
            } else {
                logger.log(Level.WARNING, "Course with ID {0} not found for deactivation", courseId);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Error deactivating course with ID: " + courseId, e);
            throw new RuntimeException("Failed to deactivate course", e);
        } finally {
            em.close();
        }
    }
    */
    
    /**
     * Force delete with cascade - removes all related data
     * USE WITH EXTREME CAUTION - this will delete all student progress!
     * 
     * @param courseId the ID of the course to force delete
     */
    public void forceDeleteCourseWithCascade(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            
            // Step 1: Delete all submissions for assignments in this course
            int deletedSubmissions = em.createQuery(
                "DELETE FROM Submission s WHERE s.assignment IN " +
                "(SELECT a FROM Assignment a WHERE a.lecture IN " +
                "(SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId))")
                .setParameter("courseId", courseId)
                .executeUpdate();
            
            // Step 2: Delete all submissions for lectures in this course  
            int deletedLectureSubmissions = em.createQuery(
                "DELETE FROM Submission s WHERE s.lecture IN " +
                "(SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId)")
                .setParameter("courseId", courseId)
                .executeUpdate();
            
            // Step 3: Delete all assignments in this course
            int deletedAssignments = em.createQuery(
                "DELETE FROM Assignment a WHERE a.lecture IN " +
                "(SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId)")
                .setParameter("courseId", courseId)
                .executeUpdate();
            
            // Step 4: Delete all lectures in this course
            int deletedLectures = em.createQuery(
                "DELETE FROM Lecture l WHERE l.course.idCourse = :courseId")
                .setParameter("courseId", courseId)
                .executeUpdate();
            
            // Step 5: Delete all enrollments for this course
            int deletedEnrollments = em.createQuery(
                "DELETE FROM Enrollment e WHERE e.course.idCourse = :courseId")
                .setParameter("courseId", courseId)
                .executeUpdate();
            
            // Step 6: Finally delete the course
            Course course = em.find(Course.class, courseId);
            if (course != null) {
                em.remove(course);
            }
            
            transaction.commit();
            logger.log(Level.WARNING, 
                "FORCE DELETE completed for course ID {0}: " +
                "Deleted {1} submissions, {2} lecture submissions, {3} assignments, " +
                "{4} lectures, {5} enrollments", 
                new Object[]{courseId, deletedSubmissions, deletedLectureSubmissions, 
                           deletedAssignments, deletedLectures, deletedEnrollments});
            
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Error in force delete for course ID: " + courseId, e);
            throw new RuntimeException("Failed to force delete course", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Check if a course can be safely deleted (has no dependencies)
     * 
     * @param courseId the ID of the course to check
     * @return CourseDeleteInfo object containing deletion feasibility and details
     */
    public CourseDeleteInfo checkCourseDeletionFeasibility(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Check for enrollments
            Long enrollmentCount = em.createQuery(
                "SELECT COUNT(e) FROM Enrollment e WHERE e.course.idCourse = :courseId", Long.class)
                .setParameter("courseId", courseId)
                .getSingleResult();
                
            // Check for lectures
            Long lectureCount = em.createQuery(
                "SELECT COUNT(l) FROM Lecture l WHERE l.course.idCourse = :courseId", Long.class)
                .setParameter("courseId", courseId)
                .getSingleResult();
                
            // Check for assignments
            Long assignmentCount = em.createQuery(
                "SELECT COUNT(a) FROM Assignment a WHERE a.lecture IN " +
                "(SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId)", Long.class)
                .setParameter("courseId", courseId)
                .getSingleResult();
                
            // Check for submissions
            Long submissionCount = em.createQuery(
                "SELECT COUNT(s) FROM Submission s WHERE s.lecture IN " +
                "(SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId) OR " +
                "s.assignment IN (SELECT a FROM Assignment a WHERE a.lecture IN " +
                "(SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId))", Long.class)
                .setParameter("courseId", courseId)
                .getSingleResult();
            
            boolean canSafelyDelete = enrollmentCount == 0 && lectureCount == 0;
            
            return new CourseDeleteInfo(canSafelyDelete, enrollmentCount.intValue(), 
                                      lectureCount.intValue(), assignmentCount.intValue(), 
                                      submissionCount.intValue());
            
        } finally {
            em.close();
        }
    }
    
    /**
     * Inner class to hold course deletion information
     */
    public static class CourseDeleteInfo {
        private final boolean canSafelyDelete;
        private final int enrollmentCount;
        private final int lectureCount;
        private final int assignmentCount;
        private final int submissionCount;
        
        public CourseDeleteInfo(boolean canSafelyDelete, int enrollmentCount, 
                               int lectureCount, int assignmentCount, int submissionCount) {
            this.canSafelyDelete = canSafelyDelete;
            this.enrollmentCount = enrollmentCount;
            this.lectureCount = lectureCount;
            this.assignmentCount = assignmentCount;
            this.submissionCount = submissionCount;
        }
        
        public boolean canSafelyDelete() { return canSafelyDelete; }
        public int getEnrollmentCount() { return enrollmentCount; }
        public int getLectureCount() { return lectureCount; }
        public int getAssignmentCount() { return assignmentCount; }
        public int getSubmissionCount() { return submissionCount; }
        
        public String getDeletionMessage() {
            if (canSafelyDelete) {
                return "Course can be safely deleted.";
            } else {
                return String.format(
                    "Cannot delete course: %d enrollments, %d lectures, %d assignments, %d submissions exist. " +
                    "Consider using force delete if you want to remove all related data.",
                    enrollmentCount, lectureCount, assignmentCount, submissionCount);
            }
        }
    }

    /**
     *
     * @param teacherId
     * @return
     */
    public List<Course> getCoursesByTeacher(int teacherId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Course> query = em.createQuery(
                "SELECT c FROM Course c WHERE c.idTeacher = :teacherId", Course.class);
            query.setParameter("teacherId", teacherId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Map<Integer, Integer> getLessonCounts() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Object[]> results = em.createQuery(
                "SELECT l.course.idCourse, COUNT(l.idLecture) FROM Lecture l GROUP BY l.course.idCourse",
                Object[].class
            ).getResultList();
            Map<Integer, Integer> lessonCounts = new HashMap<>();
            for (Object[] row : results) {
                lessonCounts.put((Integer) row[0], ((Long) row[1]).intValue());
            }
            return lessonCounts;
        } finally {
            em.close();
        }
    }

   public Map<Integer, Integer> getCompletedLectureCounts(int userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Object[]> results = em.createQuery(
                "SELECT l.course.idCourse, COUNT(s.idSubmission) " +
                "FROM Submission s " +
                "JOIN s.lecture l " +
                "WHERE s.student.id = :userId AND s.grade IS NOT NULL " +
                "GROUP BY l.course.idCourse",
                Object[].class
            )
            .setParameter("userId", userId)
            .getResultList();

            Map<Integer, Integer> completionCounts = new HashMap<>();
            for (Object[] row : results) {
                completionCounts.put((Integer) row[0], ((Long) row[1]).intValue());
            }
            return completionCounts;
        } finally {
            em.close();
        }
    }

    public List<Course> getRandomCourses(int count) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Course c ORDER BY FUNCTION('RAND')", Course.class)
                     .setMaxResults(count)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public Map<Integer, Integer> getCompletedAssignmentCounts(int userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Object[]> results = em.createQuery(
                "SELECT l.course.idCourse, COUNT(s.idSubmission) FROM Submission s " +
                "JOIN s.assignment a JOIN a.lecture l " +
                "WHERE s.student.id = :userId AND s.grade IS NOT NULL " +
                "GROUP BY l.course.idCourse",
                Object[].class
            )
            .setParameter("userId", userId)
            .getResultList();

            Map<Integer, Integer> completionCounts = new HashMap<>();
            for (Object[] row : results) {
                completionCounts.put((Integer) row[0], ((Long) row[1]).intValue());
            }
            return completionCounts;
        } finally {
            em.close();
        }
    }


    public List<Course> getCoursesByStudent(int userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT e.course FROM Enrollment e WHERE e.student.id = :userId",
                Course.class
            ).setParameter("userId", userId)
             .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Course> getAllCourses() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Course c", Course.class).getResultList();
        } finally {
            em.close();
        }
    }
    public Course getCourseById(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Course course = em.find(Course.class, id);
            if (course != null) {
                logger.log(Level.INFO, "Found course with ID: {0}", id);
            } else {
                logger.log(Level.WARNING, "Course not found for ID: {0}", id);
            }
            return course;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving course for ID: {0}, Error: {1}", 
                new Object[]{id, e.getMessage()});
            return null;
        } finally {
            em.close();
        }
    }
    
}