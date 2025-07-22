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
            Lecture lecture = em.find(Lecture.class, lectureId);
            if (lecture != null) {
                em.remove(lecture);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error deleting lecture with ID: " + lectureId, e);
            throw e;
        } finally {
            em.close();
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
