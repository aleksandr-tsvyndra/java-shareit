package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.util.Collection;

public interface BookingService {

    Collection<BookingDto> findAllByBooker(long bookerId, String state);

    Collection<BookingDto> findAllByOwner(long ownerId, String state);

    BookingDto findByBookingId(long userId, long bookingId);

    BookingDto create(long bookerId, NewBookingDto dto);

    BookingDto approve(long ownerId, long bookingId, Boolean approved);

}
