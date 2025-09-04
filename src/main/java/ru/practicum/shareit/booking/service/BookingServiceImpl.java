package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.util.BookingState;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.common.exceptions.IllegalOperationException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private static final Sort SORT_START_DESC = Sort.by(Direction.DESC, "start");

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final BookingMapper bookingMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    public Collection<BookingDto> findAllByBooker(long bookerId, String state) {
        log.info("Запрос на получение списка всех бронирований юзера с id {} передан в сервис", bookerId);
        if (userRepository.findById(bookerId).isEmpty()) {
            throw new NotFoundException("Юзер с id " + bookerId + " не найден");
        }
        BookingState bookingState = BookingState.checkAndGetState(state);
        log.info("Передано следующее состояние бронирования: {}", bookingState);
        Collection<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findAllByBookerId(bookerId, SORT_START_DESC);
            case PAST -> bookingRepository.findAllPastBookerBookings(bookerId, LocalDateTime.now(),
                    BookingStatus.APPROVED, SORT_START_DESC);
            case CURRENT -> bookingRepository.findAllCurrentBookerBookings(bookerId, LocalDateTime.now(),
                    BookingStatus.APPROVED, SORT_START_DESC);
            case FUTURE -> bookingRepository.findAllFutureBookerBookings(bookerId, LocalDateTime.now(),
                    BookingStatus.APPROVED, SORT_START_DESC);
            case REJECTED -> bookingRepository.findAllBookerBookingsByStatus(bookerId, BookingStatus.REJECTED,
                    SORT_START_DESC);
            case WAITING -> bookingRepository.findAllBookerBookingsByStatus(bookerId, BookingStatus.WAITING,
                    SORT_START_DESC);
        };
        log.info("Получили из БД список всех бронирований юзера с id {}", bookerId);
        Collection<BookingDto> toReturn = bookings.stream().map(bookingMapper::mapToBookingDto).toList();
        completeDtoCollection(toReturn, bookings);
        log.info("Передаём список всех бронирований юзера в контроллер");
        return toReturn;
    }

    @Override
    public Collection<BookingDto> findAllByOwner(long ownerId, String state) {
        log.info("Запрос на список бронирований для всех вещей юзера с id {} передан в сервис", ownerId);
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new NotFoundException("Юзер с id " + ownerId + " не найден");
        }
        BookingState bookingState = BookingState.checkAndGetState(state);
        log.info("Передано следующее состояние бронирования: {}", bookingState);
        Collection<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findAllByItemSharerId(ownerId, SORT_START_DESC);
            case PAST -> bookingRepository.findAllPastOwnerBookings(ownerId, LocalDateTime.now(),
                    BookingStatus.APPROVED, SORT_START_DESC);
            case CURRENT -> bookingRepository.findAllCurrentOwnerBookings(ownerId, LocalDateTime.now(),
                    BookingStatus.APPROVED, SORT_START_DESC);
            case FUTURE -> bookingRepository.findAllFutureOwnerBookings(ownerId, LocalDateTime.now(),
                    BookingStatus.APPROVED, SORT_START_DESC);
            case REJECTED -> bookingRepository.findAllOwnerBookingsByStatus(ownerId, BookingStatus.REJECTED,
                    SORT_START_DESC);
            case WAITING -> bookingRepository.findAllOwnerBookingsByStatus(ownerId, BookingStatus.WAITING,
                    SORT_START_DESC);
        };
        log.info("Получили из БД список бронирования для всех вещей юзера с id {}", ownerId);
        Collection<BookingDto> toReturn = bookings.stream().map(bookingMapper::mapToBookingDto).toList();
        completeDtoCollection(toReturn, bookings);
        log.info("Передаём список бронирования для всех вещей юзера в контроллер");
        return toReturn;
    }

    @Override
    public BookingDto findByBookingId(long userId, long bookingId) {
        log.info("Запрос на получение данных о бронировании с id {} передан в сервис", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));
        log.info("Проверяем, является ли юзер с id {} автором бронирования", userId);
        boolean isBookingBooker = booking.getBooker().getId().equals(userId);
        if (!isBookingBooker) {
            log.info("Юзер не является автором бронирования");
            checkItemOwner(booking.getItem(), userId);
        }
        BookingDto toReturn = bookingMapper.mapToBookingDto(booking);
        toReturn.setItem(itemMapper.mapToItemDto(booking.getItem()));
        toReturn.setBooker(userMapper.mapToUserDto(booking.getBooker()));
        log.info("Передаём данные о найденном бронировании в контроллер");
        return toReturn;
    }

    @Override
    @Transactional
    public BookingDto create(long bookerId, NewBookingDto dto) {
        log.info("Запрос на бронирование вещи юзером с id {} передан в сервис", bookerId);
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Юзер с id " + bookerId + " не найден"));
        checkTimeBooking(dto);
        Item bookingItem = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Бронируемая вещь с id " + dto.getItemId() + " не найдена"));
        if (!bookingItem.getAvailable()) {
            throw new IllegalOperationException("Вещь с id " + bookingItem.getId() + " не доступна для бронирования");
        }
        Booking newBooking = bookingMapper.mapToBooking(dto);
        newBooking.setItem(bookingItem);
        newBooking.setBooker(booker);
        newBooking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(newBooking);
        log.info("Успешно сохранили бронирование в БД");
        BookingDto toReturn = bookingMapper.mapToBookingDto(newBooking);
        log.info("Передаём данные о созданном бронировании в контроллер");
        return toReturn;
    }

    @Override
    @Transactional
    public BookingDto approve(long ownerId, long bookingId, Boolean approved) {
        log.info("Запрос на подтверждение или отклонение бронирования передан в сервис");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));
        checkItemOwner(booking.getItem(), ownerId);
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info("Владелец вещи установил следующий статус бронирования: {}", booking.getStatus());
        bookingRepository.save(booking);
        log.info("Сохранили бронирование с изменённым статусом в БД");
        BookingDto toReturn = bookingMapper.mapToBookingDto(booking);
        toReturn.setItem(itemMapper.mapToItemDto(booking.getItem()));
        toReturn.setBooker(userMapper.mapToUserDto(booking.getBooker()));
        log.info("Передаём бронирование с изменённым статусом в контроллер");
        return toReturn;
    }

    private void checkTimeBooking(NewBookingDto dto) {
        log.info("Проверяем корректность времени бронирования...");
        if (dto.getStart().isAfter(dto.getEnd())) {
            throw new ValidationException("Время начала бронирования не может быть позже его окончания");
        }
        if (dto.getStart().equals(dto.getEnd())) {
            throw new ValidationException("Бронирование не может начинаться и оканчиваться в одно время");
        }
        log.info("Время начала и окончания бронирования корректно");
    }

    private void checkItemOwner(Item item, long userId) {
        log.info("Проверяем, является ли юзер с id {} владельцем вещи, которую бронируют", userId);
        boolean isItemOwner = item.getSharer().getId().equals(userId);
        if (!isItemOwner) {
            String errorMessage = String.format("Юзер с id %d не является владельцем бронируемой вещи", userId);
            throw new IllegalOperationException(errorMessage);
        }
        log.info("Юзер с id {} является владельцем вещи, которую бронируют", userId);
    }

    private void completeDtoCollection(Collection<BookingDto> target, Collection<Booking> from) {
        for (BookingDto dto : target) {
            for (Booking booking : from) {
                if (dto.getId().equals(booking.getId())) {
                    dto.setItem(itemMapper.mapToItemDto(booking.getItem()));
                    dto.setBooker(userMapper.mapToUserDto(booking.getBooker()));
                }
            }
        }
    }
}
