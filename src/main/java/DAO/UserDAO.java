package dao;

import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.User;

public class UserDAO {

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
}