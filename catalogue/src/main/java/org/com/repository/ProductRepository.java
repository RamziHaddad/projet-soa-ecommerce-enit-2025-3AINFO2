package org.com.repository;
import java.util.List;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import org.com.entities.Product;
import org.com.exceptions.EntityAlreadyExistsException;
import org.com.exceptions.EntityNotFoundException;
@ApplicationScoped
public class ProductRepository {

    @Inject
    EntityManager em;

    public List<Product> findAll() {
        return em.createQuery("from Product", Product.class).getResultList();
    }

    public Product findById(UUID id) throws EntityNotFoundException {
        Product p = em.find(Product.class, id);
        if (p != null) {
            return p;
        }
        throw new EntityNotFoundException("cannot find product");
    }

    public List<Product> findByCategory(UUID categoryId) {
        return em.createQuery("from Product where categoryId = :categoryId", Product.class)
                .setParameter("categoryId", categoryId)
                .getResultList();
    }

    
    public Product insert(Product p) throws EntityAlreadyExistsException {
        if (p.getId() == null) {
            p.setId(UUID.randomUUID());
            try {
                em.persist(p);
                return p;
            } catch (EntityExistsException e) {
                throw new EntityAlreadyExistsException("product already exists");
            }
        }
        throw new EntityAlreadyExistsException("product has already an id");
    }

    
    public Product update(Product p) throws EntityNotFoundException {
        try {
            return em.merge(p);
        } catch (IllegalArgumentException e) {
        }
        throw new EntityNotFoundException("cannot find product");
    }

 
    public void delete(UUID id) {
        Product p = em.find(Product.class, id);
        if (p != null) {
            em.remove(p);
        }
    }}