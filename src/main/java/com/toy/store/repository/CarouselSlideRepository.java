package com.toy.store.repository;

import com.toy.store.model.CarouselSlide;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface CarouselSlideRepository extends CrudRepository<CarouselSlide, Long> {
    List<CarouselSlide> findAllByOrderBySortOrderAsc();
}
