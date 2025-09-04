package ru.practicum.shareit.booking.util;

import ru.practicum.shareit.common.exceptions.ValidationException;

public enum BookingState {
    ALL,
    CURRENT,
    FUTURE,
    PAST,
    REJECTED,
    WAITING;

    public static BookingState checkAndGetState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Невалидное значение параметра state: " + state);
        }
    }
}
