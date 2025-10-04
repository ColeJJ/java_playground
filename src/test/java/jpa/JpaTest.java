package jpa;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tun.models.Post;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class JpaTest {

    private EntityManager entityManager;

    @BeforeEach
    public void init() {
        entityManager = Persistence.createEntityManagerFactory("myPersistenceUnit").createEntityManager();
    }

    @Test
    public void saveIntoTransactionalContextWithFlush() {
        final EntityTransaction tnx = entityManager.getTransaction();

        tnx.begin();

        try {
            final List<Post> posts = entityManager.createQuery("select p from Post p", Post.class).getResultList();
            final Post p = new Post();
            entityManager.persist(p);
            System.out.println(posts.size());
            entityManager.flush();
            final List<Post> posts2 = entityManager.createQuery("select p from Post p", Post.class).getResultList();
            System.out.println(posts2.size());

            tnx.commit();
        } catch (final Exception e) {
            tnx.rollback();
            e.printStackTrace();
        }
    }
}