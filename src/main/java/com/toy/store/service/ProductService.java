package com.toy.store.service;

import com.toy.store.exception.ResourceNotFoundException;
import com.toy.store.model.Product;
import com.toy.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> findByStatus(Product.Status status, Pageable pageable) {
        return productRepository.findByStatus(status, pageable);
    }

    public Page<Product> findByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByNameOrDescription(keyword, pageable);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("產品", id));
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
