package com.toy.store.service;

import com.toy.store.exception.ResourceNotFoundException;
import com.toy.store.model.Product;
import com.toy.store.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public List<Product> findAll() {
        return productMapper.findAll();
    }

    public List<Product> findAllPaged(int offset, int limit) {
        return productMapper.findAllPaged(offset, limit);
    }

    public List<Product> findByStatus(Product.Status status) {
        return productMapper.findByStatus(status.name());
    }

    public List<Product> findByCategory(String category) {
        return productMapper.findByCategory(category);
    }

    public List<Product> searchProducts(String keyword) {
        return productMapper.searchByNameOrDescription("%" + keyword.toLowerCase() + "%");
    }

    public Product findById(Long id) {
        return productMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("產品", id));
    }

    public Product saveProduct(Product product) {
        if (product.getId() == null) {
            productMapper.insert(product);
        } else {
            productMapper.update(product);
        }
        return product;
    }

    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }

    public long count() {
        return productMapper.count();
    }
}
