package com.overseas.portal.repository;

import com.overseas.portal.entity.Booking;
import com.overseas.portal.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByStudentId(Long studentId, Pageable pageable);
    @Query("SELECT b FROM Booking b WHERE b.service.provider.user.id = :userId " +
           "AND (:status IS NULL OR b.status = :status)")
    Page<Booking> findByServiceProviderUserIdAndStatus(@Param("userId") Long userId,
                                                        @Param("status") BookingStatus status,
                                                        Pageable pageable);
    Page<Booking> findByStudentIdAndStatus(Long studentId, BookingStatus status, Pageable pageable);
    long countByStudentId(Long studentId);
    long countByStudentIdAndStatusNot(Long studentId, BookingStatus status);
    long countByServiceId(Long serviceId);
}
