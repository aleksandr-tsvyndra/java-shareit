package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping
    public Collection<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") @Positive long bookerId,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        log.info("Контроллер получил http-запрос на получение списка всех бронирований юзера с id {}", bookerId);
        Collection<BookingDto> userBookings = bookingService.findAllByBooker(bookerId, state);
        log.info("Возвращаем список всех бронирований юзера с id {} в http-ответе", bookerId);
        return userBookings;
    }

    @GetMapping("/owner")
    public Collection<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") @Positive long ownerId,
                                                   @RequestParam(defaultValue = "ALL") String state) {
        log.info("Контроллер получил http-запрос на получение списка бронирований для всех вещей владельца");
        log.info("Id владельца вещей: {}", ownerId);
        Collection<BookingDto> ownerBookings = bookingService.findAllByOwner(ownerId, state);
        log.info("Возвращаем список бронирований для всех вещей владельца с id {} в http-ответе", ownerId);
        return ownerBookings;
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                              @PathVariable(name = "bookingId") @Positive long bookingId) {
        log.info("Контроллер получил http-запрос на получение данных о бронировании с id {}", bookingId);
        BookingDto booking = bookingService.findByBookingId(userId, bookingId);
        log.info("Возвращаем данные о бронировании с id {} в http-ответе", bookingId);
        return booking;
    }

    @PostMapping
    public BookingDto create(@RequestHeader("X-Sharer-User-Id") @Positive long bookerId,
                             @Valid @RequestBody NewBookingDto dto) {
        log.info("Контроллер получил http-запрос на бронирование вещи юзером с id {}", bookerId);
        BookingDto newBooking = bookingService.create(bookerId, dto);
        log.info("Возвращаем результат бронирования в http-ответе");
        return newBooking;
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader("X-Sharer-User-Id") @Positive long ownerId,
                              @PathVariable(name = "bookingId") @Positive long bookingId,
                              @RequestParam(name = "approved") @NotNull Boolean approved) {
        log.info("Контроллер получил http-запрос на подтверждение или отклонение бронирования");
        BookingDto bookingResult = bookingService.approve(ownerId, bookingId, approved);
        log.info("Возвращаем результат подтверждения или отклонения бронирования в http-ответе");
        return bookingResult;
    }
}
