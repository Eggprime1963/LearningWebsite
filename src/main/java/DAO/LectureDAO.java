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
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not save lecture", e);
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
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not update lecture", e);
        } finally {
            em.close();
        }
    }

    public Optional<Lecture> findLectureById(int lectureId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Lecture.class, lectureId));
        } finally {
            em.close();
        }
    }

    public void deleteLecture(int lectureId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Lecture lecture = em.find(Lecture.class, lectureId);
            if (lecture != null) {
                em.remove(lecture);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Could not delete lecture", e);
        } finally {
            em.close();
        }
    }

    public List<Lecture> getLecturesByCourseId(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Lecture> query = em.createQuery(
                "SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId AND l.status = 'active'",
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
                "SELECT l FROM Lecture l WHERE l.course.idCourse = :courseId AND l.course.teacherId = :teacherId AND l.status = 'active'",
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
                "SELECT l FROM Lecture l JOIN l.course.enrollments e WHERE l.course.idCourse = :courseId AND e.student.idUser = :studentId AND l.status = 'active'",
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
            Lecture lecture = em.find(Lecture.class, id);
            if (lecture != null) {
                logger.log(Level.INFO, "Found lecture with ID: {0}", id);
            } else {
                logger.log(Level.WARNING, "Lecture not found for ID: {0}", id);
            }
            return lecture;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving lecture for ID: {0}, Error: {1}", 
                new Object[]{id, e.getMessage()});
            return null;
        } finally {
            em.close();
        }
    }
}
