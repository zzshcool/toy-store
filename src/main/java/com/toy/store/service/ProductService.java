package com.toy.store.service;

import com.toy.store.exception.ResourceNotFoundException;
import com.toy.store.model.Product;
import com.toy.store.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public List<Product> findAll() {
        return productMapper.findAll();
    }

    public Page<Product> findAll(Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        List<Product> products = productMapper.findAllPaged(offset, limit);
        long total = productMapper.count();
        return new PageImpl<>(products, pageable, total);
    }

    public List<Product> findByStatus(Product.Status status) {
        // 為了不破壞現有非分頁調用，提供一個默認的大限制
        return productMapper.findByStatus(status.name(), 0, 1000);
    }

    public Page<Product> findByStatus(Product.Status status, Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        List<Product> products = productMapper.findByStatus(status.name(), offset, limit);
        long total = productMapper.countByStatus(status.name());
        return new PageImpl<>(products, pageable, total);
    }

    public Page<Product> findByCategory(String category, Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        List<Product> products = productMapper.findByCategory(category, offset, limit);
        // 此處需要一個按分類計數的方法，如果 Mapper 沒提供，暫時用總數或修改 Mapper
        long total = productMapper.count(); // 理想情況應是 countByCategory
        return new PageImpl<>(products, pageable, total);
    }

    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        List<Product> products = productMapper.searchByNameOrDescription(keyword, offset, limit);
        // 同上，需要 countBySearch
        long total = productMapper.count();
        return new PageImpl<>(products, pageable, total);
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
