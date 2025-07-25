package dao;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Assignment;
import model.Submission;
import model.User;

public class SubmissionDAO {

    private static final Logger logger = Logger.getLogger(SubmissionDAO.class.getName());
    private static final Logger LOGGER = Logger.getLogger(SubmissionDAO.class.getName());
    /**
     * Saves a new submission record to the database.
     *
     * @param submission The Submission object to persist.
     * @return The saved Submission object.
     */
    public Submission saveSubmission(Submission submission) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(submission);
            transaction.commit();
            logger.info("Successfully saved submission for assignment ID: " + submission.getAssignment().getId());
            return submission;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error saving submission", e);
            throw new RuntimeException("Failed to save submission", e);
        } finally {
            em.close();
        }
    }

    /**
     * Updates a submission, typically for grading.
     *
     * @param submission The Submission object with updated data (e.g., a grade).
     */
    public void updateSubmission(Submission submission) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(submission);
            transaction.commit();
            logger.info("Successfully updated submission ID: " + submission.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error updating submission ID: " + submission.getId(), e);
            throw new RuntimeException("Failed to update submission", e);
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves a submission by its unique ID.
     *
     * @param submissionId The ID of the submission.
     * @return An Optional containing the submission if found.
     */
    public Optional<Submission> findSubmissionById(int submissionId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Submission.class, submissionId));
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all submissions for a specific assignment.
     *
     * @param assignmentId The ID of the assignment.
     * @return A list of submissions.
     */
    public List<Submission> getSubmissionsByAssignment(int assignmentId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Submission> query = em.createQuery(
                "SELECT s FROM Submission s WHERE s.assignment.idAss = :assignmentId",
                Submission.class
            );
            query.setParameter("assignmentId", assignmentId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving submissions for assignment ID: " + assignmentId, e);
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<Submission> getSubmissionsByStudent(int studentId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Submission> query = em.createQuery(
                "SELECT s FROM Submission s WHERE s.student.idUser = :studentId",
                Submission.class    
            );
            query.setParameter("studentId", studentId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving submissions for student ID: " + studentId,
                e);
            return Collections.emptyList(); 
        } finally {
            em.close();
        }
    }

    public List<Submission> getSubmissionsByStudentAndCourse(int studentId, int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Submission> query = em.createQuery(
                "SELECT s FROM Submission s WHERE s.student.idUser = :studentId AND s.assignment.course.idCourse = :courseId",
                Submission.class
            );
            query.setParameter("studentId", studentId);
            query.setParameter("courseId", courseId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving submissions for student ID: " + studentId + " and course ID: " + courseId, e);
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    // For teacher: Map assignmentId -> List<Submission>
    public Map<Integer, List<Submission>> getSubmissionsByAssignments(List<Assignment> assignments) {
        Map<Integer, List<Submission>> result = new HashMap<>();
        for (Assignment assignment : assignments) {
            result.put(assignment.getId(), getSubmissionsByAssignment(assignment.getId()));
        }
        return result;
    }

    // For student: Map assignmentId -> Submission
    public Map<Integer, Submission> getSubmissionsByStudentAndAssignments(int studentId, List<Assignment> assignments) {
        Map<Integer, Submission> result = new HashMap<>();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            for (Assignment assignment : assignments) {
                TypedQuery<Submission> query = em.createQuery(
                    "SELECT s FROM Submission s WHERE s.assignment.idAssignment = :assignmentId AND s.student.idUser = :studentId",
                    Submission.class
                );
                query.setParameter("assignmentId", assignment.getId());
                query.setParameter("studentId", studentId);
                List<Submission> submissions = query.getResultList();
                if (!submissions.isEmpty()) {
                    result.put(assignment.getId(), submissions.get(0));
                }
            }
        } finally {
            em.close();
        }
        return result;
    }
  public List<Submission> getSubmissionsBeforeDueDate(int assignmentId, Date dueDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            LocalDateTime dueDateTime = dueDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();

            TypedQuery<Submission> query = em.createQuery(
                "SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId AND s.submissionDate < :dueDate",
                Submission.class
            );
            query.setParameter("assignmentId", assignmentId);
            query.setParameter("dueDate", dueDateTime);

            List<Submission> submissions = query.getResultList();
            LOGGER.log(Level.INFO, "Found {0} submissions for assignmentId: {1}", 
                new Object[]{submissions.size(), assignmentId});

            // Handle null relationships as in the original
            for (Submission s : submissions) {
                if (s.getStudent() == null) {
                    LOGGER.log(Level.WARNING, "No student found for submission ID: {0}", s.getId());
                    s.setStudent(new User());
                }
                if (s.getAssignment() == null) {
                    LOGGER.log(Level.WARNING, "No assignment found for submission ID: {0}", s.getId());
                    s.setAssignment(new Assignment());
                }
            }
            return submissions;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving submissions for assignmentId: {0}, dueDate: {1}", 
                new Object[]{assignmentId, dueDate});
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public void updateGrade(int submissionId, double grade) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Submission submission = em.find(Submission.class, submissionId);
            if (submission != null) {
                submission.setGrade(grade);
                em.merge(submission);
                LOGGER.log(Level.INFO, "Updated grade for submissionId: {0} to {1}", 
                    new Object[]{submissionId, grade});
            } else {
                LOGGER.log(Level.WARNING, "Submission not found for ID: {0}", submissionId);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error updating grade for submissionId: {0}, Error: {1}", 
                new Object[]{submissionId, e.getMessage()});
            throw new RuntimeException("Failed to update grade", e);
        } finally {
            em.close();
        }
    }
}