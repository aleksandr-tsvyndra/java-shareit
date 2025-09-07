package ru.practicum.shareit.booking;

import jakarta.validation.Valid;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.common.exceptions.ValidationException;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getUserBookings(
            @Positive @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "100") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Неизвестное значение: " + stateParam));
        log.info("Клиент отправил запрос на вывод списка бронирований юзера с id {}", userId);
        log.info("Параметр state={}, from={}, size={}", state, from, size);
        return bookingClient.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @Positive @RequestHeader("X-Sharer-User-Id") long ownerId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "100") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Неизвестное значение: " + stateParam));
        log.info("Клиент отправил запрос на получение списка бронирований для всех вещей владельца c id {}", ownerId);
        log.info("Параметр state={}, from={}, size={}", state, from, size);
        return bookingClient.getOwnerBookings(ownerId, state, from, size);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                          @Positive @PathVariable(name = "bookingId") long bookingId) {
        log.info("Клиент отправил запрос, что юзер с id {} запрашивает бронирование с id {}", userId, bookingId);
        return bookingClient.getById(userId, bookingId);
    }

    @PostMapping
    public ResponseEntity<Object> createBooking(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestBody @Valid NewBookingDto dto) {
        log.info("Клиент отправил запрос на бронирование вещи юзером с id {}", userId);
        log.info("Проверяем корректность времени бронирования...");
        if (dto.getStart().isAfter(dto.getEnd())) {
            throw new ValidationException("Время начала бронирования не может быть позже его окончания");
        }
        if (dto.getStart().equals(dto.getEnd())) {
            throw new ValidationException("Бронирование не может начинаться и оканчиваться в одно время");
        }
        log.info("Время начала и окончания бронирования корректно");
        return bookingClient.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@Positive @RequestHeader("X-Sharer-User-Id") long ownerId,
                                                 @Positive @PathVariable(name = "bookingId") long bookingId,
                                                 @RequestParam(name = "approved") boolean approved) {
        log.info("Со стороны клиента отправлен запрос на подтверждение или отклонение бронирования");
        log.info("Статус бронирования: {}", approved);
        return bookingClient.approve(ownerId, bookingId, approved);
    }
}
