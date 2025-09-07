package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<Collection<BookingDto>> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") long bookerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "100") Integer size) {
        log.info("Контроллер получил http-запрос на получение списка всех бронирований юзера с id {}", bookerId);
        Collection<BookingDto> userBookings = bookingService.findAllByBooker(bookerId, state, from, size);
        log.info("Возвращаем список всех бронирований юзера с id {} в http-ответе", bookerId);
        return new ResponseEntity<>(userBookings, HttpStatus.OK);
    }

    @GetMapping("/owner")
    public ResponseEntity<Collection<BookingDto>> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "100") Integer size) {
        log.info("Контроллер получил http-запрос на получение списка бронирований для всех вещей владельца");
        log.info("Id владельца вещей: {}", ownerId);
        Collection<BookingDto> ownerBookings = bookingService.findAllByOwner(ownerId, state, from, size);
        log.info("Возвращаем список бронирований для всех вещей владельца с id {} в http-ответе", ownerId);
        return new ResponseEntity<>(ownerBookings, HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @PathVariable(name = "bookingId") long bookingId) {
        log.info("Контроллер получил http-запрос на получение данных о бронировании с id {}", bookingId);
        BookingDto booking = bookingService.findByBookingId(userId, bookingId);
        log.info("Возвращаем данные о бронировании с id {} в http-ответе", bookingId);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<BookingDto> create(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                             @RequestBody NewBookingDto dto) {
        log.info("Контроллер получил http-запрос на бронирование вещи юзером с id {}", bookerId);
        BookingDto newBooking = bookingService.create(bookerId, dto);
        log.info("Возвращаем результат бронирования в http-ответе");
        return new ResponseEntity<>(newBooking, HttpStatus.OK);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> approve(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                              @PathVariable(name = "bookingId") long bookingId,
                                              @RequestParam(name = "approved") boolean approved) {
        log.info("Контроллер получил http-запрос на подтверждение или отклонение бронирования");
        BookingDto bookingResult = bookingService.approve(ownerId, bookingId, approved);
        log.info("Возвращаем результат подтверждения или отклонения бронирования в http-ответе");
        return new ResponseEntity<>(bookingResult, HttpStatus.OK);
    }
}
