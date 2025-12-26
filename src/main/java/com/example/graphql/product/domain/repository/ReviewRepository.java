package com.example.graphql.product.domain.repository;

import com.example.graphql.product.domain.model.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Review save(Review review);
    Optional<Review> findById(Long id);
    List<Review> findByProductIdIn(List<Long> productIds);
    List<Review> findByProductIdInAndRatingGreaterThanEqual(List<Long> productIds, Integer minRating);
    void deleteById(Long id);
}
