package com.toy.store.mapper;

import com.toy.store.model.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 產品 MyBatis Mapper
 */
@Mapper
public interface ProductMapper {

        @Select("SELECT * FROM products WHERE id = #{id}")
        Optional<Product> findById(Long id);

        @Select("SELECT * FROM products")
        List<Product> findAll();

        @Select("SELECT * FROM products WHERE status = #{status} LIMIT #{limit} OFFSET #{offset}")
        List<Product> findByStatus(@Param("status") String status,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        @Select("SELECT COUNT(*) FROM products WHERE status = #{status}")
        long countByStatus(@Param("status") String status);

        @Select("SELECT * FROM products WHERE name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%') LIMIT #{limit} OFFSET #{offset}")
        List<Product> searchByNameOrDescription(@Param("keyword") String keyword,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        @Select("SELECT COUNT(*) FROM products WHERE name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')")
        long countBySearch(@Param("keyword") String keyword);

        @Select("SELECT * FROM products WHERE category = #{category} LIMIT #{limit} OFFSET #{offset}")
        List<Product> findByCategory(@Param("category") String category,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        @Select("SELECT COUNT(*) FROM products WHERE category = #{category}")
        long countByCategory(@Param("category") String category);

        @Select("SELECT * FROM products WHERE category = #{category} AND sub_category = #{subCategory} LIMIT #{limit} OFFSET #{offset}")
        List<Product> findByCategoryAndSubCategory(@Param("category") String category,
                        @Param("subCategory") String subCategory,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        @Select("SELECT COUNT(*) FROM products WHERE category = #{category} AND sub_category = #{subCategory}")
        long countByCategoryAndSubCategory(@Param("category") String category,
                        @Param("subCategory") String subCategory);

        @Select("SELECT * FROM products WHERE name = #{name}")
        List<Product> findByName(String name);

        @Insert("INSERT INTO products (name, description, price, stock, category, sub_category, image_url, status, created_at) "
                        +
                        "VALUES (#{name}, #{description}, #{price}, #{stock}, #{category}, #{subCategory}, #{imageUrl}, #{status}, #{createdAt})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Product product);

        @Update("UPDATE products SET name = #{name}, description = #{description}, price = #{price}, " +
                        "stock = #{stock}, category = #{category}, sub_category = #{subCategory}, " +
                        "image_url = #{imageUrl}, status = #{status} WHERE id = #{id}")
        int update(Product product);

        @Delete("DELETE FROM products WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM products LIMIT #{limit} OFFSET #{offset}")
        List<Product> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

        @Select("SELECT COUNT(*) FROM products")
        long count();
}
