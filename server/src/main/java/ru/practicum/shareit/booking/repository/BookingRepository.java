package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.util.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findAllByItemSharerId(long ownerId, Pageable pageable);

    @Query("SELECT b "
            + "FROM Booking AS b "
            + "WHERE b.item.sharer.id = :owner_id "
            + "AND b.start < :time "
            + "AND b.end < :time "
            + "AND b.status = :status")
    Page<Booking> findAllPastOwnerBookings(@Param("owner_id") long ownerId,
                                                 @Param("time") LocalDateTime time,
                                                 @Param("status") BookingStatus status,
                                                 Pageable pageable);

    @Query("SELECT b "
            + "FROM Booking AS b "
            + "WHERE b.item.sharer.id = :owner_id "
            + "AND b.start < :time "
            + "AND b.end > :time "
            + "AND b.status = :status")
    Page<Booking> findAllCurrentOwnerBookings(@Param("owner_id") long ownerId,
                                                    @Param("time") LocalDateTime time,
                                                    @Param("status") BookingStatus status,
                                                    Pageable pageable);

    @Query("SELECT b "
            + "FROM Booking AS b "
            + "WHERE b.item.sharer.id = :owner_id "
            + "AND b.start > :time "
            + "AND b.end > :time "
            + "AND b.status = :status")
    Page<Booking> findAllFutureOwnerBookings(@Param("owner_id") long ownerId,
                                                   @Param("time") LocalDateTime time,
                                                   @Param("status") BookingStatus status,
                                                   Pageable pageable);

    @Query("SELECT b "
            + "FROM Booking AS b "
            + "WHERE b.item.sharer.id = :owner_id "
            + "AND b.status = :status")
    Page<Booking> findAllOwnerBookingsByStatus(@Param("owner_id") long ownerId,
                                                     @Param("status") BookingStatus status,
                                                     Pageable pageable);

    Page<Booking> findAllByBookerId(long bookerId, Pageable pageable);

    @Query("SELECT b "
            + "FROM Booking AS b "
            + "WHERE b.booker.id = :booker_id "
            + "AND b.start < :time "
            + "AND b.end < :time "
            + "AND b.status = :status")
    Page<Booking> findAllPastBookerBookings(@Param("booker_id") long bookerId,
                                                  @Param("time") LocalDateTime time,
                                                  @Param("status") BookingStatus status,
                                                  Pageable pageable);

    @Query("SELECT b "
            + "FROM Booking AS b "
            + "WHERE b.booker.id = :booker_id "
            + "AND b.start < :time "
            + "AND b.end > :time "
            + "AND b.status = :status")
    Page<Booking> findAllCurrentBookerBookings(@Param("booker_id") long bookerId,
                                                     @Param("time") LocalDateTime time,
                                                     @Param("status") BookingStatus status,
                                                     Pageable pageable);

    @Query("SELECT b "
            + "FROM Booking AS b "
            + "WHERE b.booker.id = :booker_id "
            + "AND b.start > :time "
            + "AND b.end > :time "
            + "AND b.status = :status")
    Page<Booking> findAllFutureBookerBookings(@Param("booker_id") long bookerId,
                                                    @Param("time") LocalDateTime time,
                                                    @Param("status") BookingStatus status,
                                                    Pageable pageable);

    @Query("SELECT b "
            + "FROM Booking AS b "
            + "WHERE b.booker.id = :booker_id "
            + "AND b.status = :status")
    Page<Booking> findAllBookerBookingsByStatus(@Param("booker_id") long bookerId,
                                                      @Param("status") BookingStatus status,
                                                      Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END "
            + "FROM Booking b "
            + "WHERE b.end < :time "
            + "AND b.status = :status "
            + "AND b.item.id = :item_id "
            + "AND b.booker.id = :user_id")
    boolean findBondBetweenItemAndUser(@Param("item_id") long itemId,
                                       @Param("user_id") long userId,
                                       @Param("time") LocalDateTime time,
                                       @Param("status") BookingStatus status);

    Booking findFirstBookingByItemIdAndEndIsBeforeAndStatus(long itemId,
                                                            LocalDateTime date,
                                                            BookingStatus status,
                                                            Sort sort);

    Booking findFirstBookingByItemIdAndEndIsAfterAndStatus(long itemId,
                                                           LocalDateTime date,
                                                           BookingStatus status,
                                                           Sort sort);

}
