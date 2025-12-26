package com.example.graphql.product.infrastructure.persistence;

import com.example.graphql.product.domain.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdIn(List<Long> productIds);
    List<Review> findByProductIdInAndRatingGreaterThanEqual(List<Long> productIds, Integer minRating);
}
