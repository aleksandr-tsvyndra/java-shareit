package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.practicum.shareit.booking.util.BookingStatus;

import java.time.LocalDateTime;

@Data
public class NewBookingDto {
    @NotNull(message = "Дата начала бронирования не может быть null")
    @FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом")
    private LocalDateTime start;
    @NotNull(message = "Дата окончания бронирования не может быть null")
    @Future(message = "Дата окончания бронирования должна быть в будущем")
    private LocalDateTime end;
    @NotNull(message = "Идентификатор бронируемой вещи не может быть null")
    @Positive(message = "Идентификатор бронируемой вещи должен быть положительным числом")
    private Long itemId;
    private Long bookerId;
    private BookingStatus status;
}
