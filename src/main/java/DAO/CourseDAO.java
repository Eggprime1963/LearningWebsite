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
            Course course = em.find(Course.class, courseId);
            if (course != null) {
                em.remove(course);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Error deleting course with ID: " + courseId, e);
            throw new RuntimeException("Failed to delete course", e);
        } finally {
            em.close();
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