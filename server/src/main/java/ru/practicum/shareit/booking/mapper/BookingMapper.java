package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    Booking mapToBooking(NewBookingDto newBookingDto);

    BookingDto mapToBookingDto(Booking booking);

    BookingDtoShort mapToBookingDtoShort(Booking booking);

}
