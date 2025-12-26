package com.example.graphql.product.infrastructure.persistence;

import com.example.graphql.product.domain.model.Review;
import com.example.graphql.product.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaReviewAdapter implements ReviewRepository {
    private final SpringDataReviewRepository springDataRepository;

    @Override public Review save(Review review) { return springDataRepository.save(review); }
    @Override public Optional<Review> findById(Long id) { return springDataRepository.findById(id); }
    @Override public List<Review> findByProductIdIn(List<Long> productIds) { return springDataRepository.findByProductIdIn(productIds); }
    @Override public List<Review> findByProductIdInAndRatingGreaterThanEqual(List<Long> productIds, Integer minRating) { return springDataRepository.findByProductIdInAndRatingGreaterThanEqual(productIds, minRating); }
    @Override public void deleteById(Long id) { springDataRepository.deleteById(id); }
}
