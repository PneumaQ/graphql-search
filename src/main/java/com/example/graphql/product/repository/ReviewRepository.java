package com.example.graphql.product.repository;

import com.example.graphql.product.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdIn(List<Long> productIds);
    List<Review> findByProductIdInAndRatingGreaterThanEqual(List<Long> productIds, Integer minRating);
}
