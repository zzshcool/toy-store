package com.toy.store.repository;

import com.toy.store.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByStatus(Product.Status status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Product> searchByNameOrDescription(@Param("keyword") String keyword, Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByCategoryAndSubCategory(String category, String subCategory, Pageable pageable);

    java.util.List<Product> findByName(String name);
}
