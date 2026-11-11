package com.vehiclerental.review.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.review.entity.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Stores reviews in data/reviews.txt */
@Repository
public class ReviewRepository extends FileRepository<Review> {

    public ReviewRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "reviews.txt"));
    }

    public Optional<Review> findByBookingId(Long bookingId) {
        return findFirst(r -> bookingId.equals(r.getBookingId()));
    }

    public List<Review> findByVehicleId(Long vehicleId) {
        return findWhere(r -> vehicleId.equals(r.getVehicleId()));
    }

    public List<Review> findByCustomerId(Long customerId) {
        return findWhere(r -> customerId.equals(r.getCustomerId()));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|bookingId|customerId|customerName|vehicleId|vehicleModel|rating|comment|hidden|adminReply|repliedAt";
    }

    @Override
    protected String[] toFields(Review r) {
        return new String[]{text(r.getBookingId()), text(r.getCustomerId()), text(r.getCustomerName()),
                text(r.getVehicleId()), text(r.getVehicleModel()), String.valueOf(r.getRating()),
                text(r.getComment()), String.valueOf(r.isHidden()),
                text(r.getAdminReply()), text(r.getRepliedAt())};
    }

    @Override
    protected Review fromFields(String[] f) {
        Review r = new Review();
        r.setBookingId(lng(f, 0));
        r.setCustomerId(lng(f, 1));
        r.setCustomerName(str(f, 2));
        r.setVehicleId(lng(f, 3));
        r.setVehicleModel(str(f, 4));
        r.setRating(integer(f, 5) == null ? 5 : integer(f, 5));
        r.setComment(strOrNull(f, 6));
        r.setHidden(bool(f, 7));
        r.setAdminReply(strOrNull(f, 8));
        r.setRepliedAt(dateTime(f, 9));
        return r;
    }
}
