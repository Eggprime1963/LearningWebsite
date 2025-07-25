package dao;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Payment;

public class PaymentDAO {
    private static final Logger logger = Logger.getLogger(PaymentDAO.class.getName());

    public Payment savePayment(Payment payment) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(payment);
            transaction.commit();
            logger.log(Level.INFO, "Payment saved successfully with ID: {0}", payment.getIdPayment());
            return payment;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Error saving payment", e);
            throw new RuntimeException("Could not save payment", e);
        } finally {
            em.close();
        }
    }

    public void updatePayment(Payment payment) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(payment);
            transaction.commit();
            logger.log(Level.INFO, "Payment updated successfully with ID: {0}", payment.getIdPayment());
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Error updating payment", e);
            throw new RuntimeException("Could not update payment", e);
        } finally {
            em.close();
        }
    }

    public Optional<Payment> findById(int paymentId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Payment payment = em.find(Payment.class, paymentId);
            return Optional.ofNullable(payment);
        } finally {
            em.close();
        }
    }

    public Optional<Payment> findByVnpTxnRef(String vnpTxnRef) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.vnpTxnRef = :vnpTxnRef", Payment.class);
            query.setParameter("vnpTxnRef", vnpTxnRef);
            List<Payment> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding payment by vnpTxnRef: " + vnpTxnRef, e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<Payment> getPaymentsByUser(int userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.user.id = :userId ORDER BY p.createdDate DESC", 
                Payment.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting payments for user: " + userId, e);
            return List.of();
        } finally {
            em.close();
        }
    }

    public List<Payment> getPaymentsByCourse(int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.course.idCourse = :courseId ORDER BY p.createdDate DESC", 
                Payment.class);
            query.setParameter("courseId", courseId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting payments for course: " + courseId, e);
            return List.of();
        } finally {
            em.close();
        }
    }

    public boolean hasUserPaidForCourse(int userId, int courseId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(p) FROM Payment p WHERE p.user.id = :userId AND p.course.idCourse = :courseId AND p.status = 'SUCCESS'", 
                Long.class);
            query.setParameter("userId", userId);
            query.setParameter("courseId", courseId);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking payment status for user: " + userId + " course: " + courseId, e);
            return false;
        } finally {
            em.close();
        }
    }

    public List<Payment> getAllPayments() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p ORDER BY p.createdDate DESC", Payment.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting all payments", e);
            return List.of();
        } finally {
            em.close();
        }
    }

    public List<Payment> getSuccessfulPayments() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.status = 'SUCCESS' ORDER BY p.createdDate DESC", 
                Payment.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting successful payments", e);
            return List.of();
        } finally {
            em.close();
        }
    }
}
