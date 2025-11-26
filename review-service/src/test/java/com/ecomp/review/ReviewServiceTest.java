package com.ecomp.review;

import com.ecomp.review.dto.ReviewDtos.*;
import com.ecomp.review.entity.Review;
import com.ecomp.review.repository.ReviewRepository;
import com.ecomp.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository repository;

    private static final String TEST_USER_ID = "user-123";
    private static final UUID TEST_SERVICE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createReview_Success() {
        ReviewInput input = ReviewInput.builder()
                .serviceId(TEST_SERVICE_ID.toString())
                .rating(5)
                .comment("Excellent service!")
                .build();

        ReviewDto result = reviewService.create(TEST_USER_ID, "testuser", input);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Excellent service!", result.getComment());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void createReview_DuplicateReview_ThrowsException() {
        ReviewInput input = ReviewInput.builder()
                .serviceId(TEST_SERVICE_ID.toString())
                .rating(5)
                .comment("First review")
                .build();

        reviewService.create(TEST_USER_ID, "testuser", input);

        ReviewInput duplicateInput = ReviewInput.builder()
                .serviceId(TEST_SERVICE_ID.toString())
                .rating(4)
                .comment("Second review")
                .build();

        assertThrows(IllegalStateException.class, () -> {
            reviewService.create(TEST_USER_ID, "testuser", duplicateInput);
        });
    }

    @Test
    void getByServiceId_ReturnsReviewsForService() {
        repository.save(Review.builder()
                .serviceId(TEST_SERVICE_ID)
                .userId(TEST_USER_ID)
                .username("user1")
                .rating(5)
                .comment("Great!")
                .build());
        repository.save(Review.builder()
                .serviceId(TEST_SERVICE_ID)
                .userId("other-user")
                .username("user2")
                .rating(4)
                .comment("Good")
                .build());
        repository.save(Review.builder()
                .serviceId(UUID.randomUUID())
                .userId(TEST_USER_ID)
                .username("user1")
                .rating(3)
                .build());

        List<ReviewDto> results = reviewService.getByServiceId(TEST_SERVICE_ID);

        assertEquals(2, results.size());
    }

    @Test
    void getByUserId_ReturnsUserReviews() {
        repository.save(Review.builder()
                .serviceId(UUID.randomUUID())
                .userId(TEST_USER_ID)
                .rating(5)
                .build());
        repository.save(Review.builder()
                .serviceId(UUID.randomUUID())
                .userId(TEST_USER_ID)
                .rating(4)
                .build());
        repository.save(Review.builder()
                .serviceId(UUID.randomUUID())
                .userId("other-user")
                .rating(3)
                .build());

        List<ReviewDto> results = reviewService.getByUserId(TEST_USER_ID);

        assertEquals(2, results.size());
    }

    @Test
    void updateReview_Success() {
        Review review = repository.save(Review.builder()
                .serviceId(TEST_SERVICE_ID)
                .userId(TEST_USER_ID)
                .rating(3)
                .comment("Initial comment")
                .build());

        ReviewInput updateInput = ReviewInput.builder()
                .serviceId(TEST_SERVICE_ID.toString())
                .rating(5)
                .comment("Updated comment")
                .build();

        Optional<ReviewDto> result = reviewService.update(review.getId(), TEST_USER_ID, updateInput);

        assertTrue(result.isPresent());
        assertEquals(5, result.get().getRating());
        assertEquals("Updated comment", result.get().getComment());
    }

    @Test
    void updateReview_WrongUser_ReturnsEmpty() {
        Review review = repository.save(Review.builder()
                .serviceId(TEST_SERVICE_ID)
                .userId(TEST_USER_ID)
                .rating(3)
                .build());

        ReviewInput updateInput = ReviewInput.builder()
                .serviceId(TEST_SERVICE_ID.toString())
                .rating(5)
                .build();

        Optional<ReviewDto> result = reviewService.update(review.getId(), "wrong-user", updateInput);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteReview_Success() {
        Review review = repository.save(Review.builder()
                .serviceId(TEST_SERVICE_ID)
                .userId(TEST_USER_ID)
                .rating(3)
                .build());

        boolean deleted = reviewService.delete(review.getId(), TEST_USER_ID);

        assertTrue(deleted);
        assertFalse(repository.existsById(review.getId()));
    }

    @Test
    void deleteReview_WrongUser_ReturnsFalse() {
        Review review = repository.save(Review.builder()
                .serviceId(TEST_SERVICE_ID)
                .userId(TEST_USER_ID)
                .rating(3)
                .build());

        boolean deleted = reviewService.delete(review.getId(), "wrong-user");

        assertFalse(deleted);
        assertTrue(repository.existsById(review.getId()));
    }

    @Test
    void getStats_CalculatesCorrectly() {
        repository.save(Review.builder().serviceId(TEST_SERVICE_ID).userId("u1").rating(5).build());
        repository.save(Review.builder().serviceId(TEST_SERVICE_ID).userId("u2").rating(5).build());
        repository.save(Review.builder().serviceId(TEST_SERVICE_ID).userId("u3").rating(4).build());
        repository.save(Review.builder().serviceId(TEST_SERVICE_ID).userId("u4").rating(3).build());
        repository.save(Review.builder().serviceId(TEST_SERVICE_ID).userId("u5").rating(1).build());

        ReviewStats stats = reviewService.getStats(TEST_SERVICE_ID);

        assertEquals(5, stats.getTotalReviews());
        assertEquals(3.6, stats.getAverageRating(), 0.1);
        assertEquals(2, stats.getFiveStars());
        assertEquals(1, stats.getFourStars());
        assertEquals(1, stats.getThreeStars());
        assertEquals(0, stats.getTwoStars());
        assertEquals(1, stats.getOneStar());
    }
}
