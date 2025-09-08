package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.IllegalOperationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@DisplayName("Обработка данных на уровне сервиса BookingService")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {

    @Autowired
    private final BookingServiceImpl bookingService;

    @MockBean
    private final UserRepository userRepository;

    @MockBean
    private final ItemRepository itemRepository;

    @MockBean
    private final BookingRepository bookingRepository;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private NewBookingDto bookingCreateDto;

    private static Page<Booking> getPageFromList(List<Booking> list) {
        return new PageImpl<>(list, PageRequest.of(0, 10), list.size());
    }

    @BeforeEach
    void setUp() {
        Random random = new Random();

        startDate = LocalDateTime.now().plusDays(1);
        endDate = LocalDateTime.now().plusDays(2);

        owner = User.builder()
                .id(Math.abs(random.nextLong()))
                .name("Owner Test")
                .email("owner@system.com")
                .build();
        booker = User.builder()
                .id(Math.abs(random.nextLong()))
                .name("Booker Test")
                .email("booker@system.com")
                .build();

        item = Item.builder()
                .id(Math.abs(random.nextLong()))
                .name("First item")
                .description("First item description")
                .sharer(owner)
                .available(true)
                .build();

        booking = Booking.builder()
                .id(Math.abs(random.nextLong()))
                .start(startDate)
                .end(endDate)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        bookingCreateDto = NewBookingDto.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .bookerId(booker.getId())
                .itemId(item.getId())
                .build();
    }

    @AfterEach
    void tearDown() {
        startDate = null;
        endDate = null;

        owner = null;
        booker = null;

        item = null;

        booking = null;
        bookingCreateDto = null;
    }

    @DisplayName("Получение списка бронирований по идентификатору бронирующего")
    @Test
    void getAllBookingsByBooker() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booker));

        when(bookingRepository.findAllByBookerId(anyLong(), any()))
                .thenReturn(getPageFromList(List.of(booking)));

        Collection<BookingDto> bookingList = bookingService.findAllByBooker(booker.getId(), "ALL", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());

        Optional<BookingDto> optDto = bookingList.stream().findFirst();
        assertTrue(optDto.isPresent());

        BookingDto dto = optDto.get();
        assertNotNull(dto);
        assertNotNull(dto.getId());
        assertEquals(booking.getId(), dto.getId());
        assertEquals(booking.getStart(), dto.getStart());
        assertEquals(booking.getEnd(), dto.getEnd());
        assertEquals(booking.getStatus(), dto.getStatus());
        assertEquals(booker.getId(), dto.getBooker().getId());
        assertEquals(booker.getName(), dto.getBooker().getName());
        assertEquals(booker.getEmail(), dto.getBooker().getEmail());
        assertEquals(item.getId(), dto.getItem().getId());
        assertEquals(item.getName(), dto.getItem().getName());
        assertEquals(item.getDescription(), dto.getItem().getDescription());
        assertEquals(item.getAvailable(), dto.getItem().getAvailable());
        assertEquals(owner.getId(), dto.getItem().getSharer().getId());
        assertEquals(owner.getName(), dto.getItem().getSharer().getName());
        assertEquals(owner.getEmail(), dto.getItem().getSharer().getEmail());

        when(bookingRepository.findAllCurrentBookerBookings(anyLong(), any(), any(), any()))
                .thenReturn(getPageFromList(List.of(booking)));

        bookingList = bookingService.findAllByBooker(booker.getId(), "CURRENT", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());

        when(bookingRepository.findAllFutureBookerBookings(anyLong(), any(), any(), any()))
                .thenReturn(getPageFromList(List.of(booking)));

        bookingList = bookingService.findAllByBooker(booker.getId(), "FUTURE", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());

        when(bookingRepository.findAllPastBookerBookings(anyLong(), any(), any(), any()))
                .thenReturn(getPageFromList(List.of(booking)));

        bookingList = bookingService.findAllByBooker(booker.getId(), "PAST", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());

        when(bookingRepository.findAllBookerBookingsByStatus(anyLong(), any(), any()))
                .thenReturn(getPageFromList(List.of(booking)));

        bookingList = bookingService.findAllByBooker(booker.getId(), "REJECTED", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());

        bookingList = bookingService.findAllByBooker(booker.getId(), "WAITING", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());
    }

    @DisplayName("Вызов исключения NotFoundException при получении списка бронирований по идентификатору бронирующего")
    @Test
    void getAllBookingsByBookerWith404Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.findAllByBooker(booker.getId(), "ALL", 0, 10));
    }

    @DisplayName("Получение списка бронирований по идентификатору владельца вещи")
    @Test
    void getAllBookingsByOwner() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(owner));

        when(bookingRepository.findAllByItemSharerId(anyLong(), any()))
                .thenReturn(getPageFromList(List.of(booking)));

        Collection<BookingDto> bookingList = bookingService.findAllByOwner(booker.getId(), "ALL", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());

        Optional<BookingDto> optDto = bookingList.stream().findFirst();
        assertTrue(optDto.isPresent());

        BookingDto dto = optDto.get();
        assertNotNull(dto);
        assertNotNull(dto.getId());
        assertEquals(booking.getId(), dto.getId());
        assertEquals(booking.getStart(), dto.getStart());
        assertEquals(booking.getEnd(), dto.getEnd());
        assertEquals(booking.getStatus(), dto.getStatus());
        assertEquals(booker.getId(), dto.getBooker().getId());
        assertEquals(booker.getName(), dto.getBooker().getName());
        assertEquals(booker.getEmail(), dto.getBooker().getEmail());
        assertEquals(item.getId(), dto.getItem().getId());
        assertEquals(item.getName(), dto.getItem().getName());
        assertEquals(item.getDescription(), dto.getItem().getDescription());
        assertEquals(item.getAvailable(), dto.getItem().getAvailable());
        assertEquals(owner.getId(), dto.getItem().getSharer().getId());
        assertEquals(owner.getName(), dto.getItem().getSharer().getName());
        assertEquals(owner.getEmail(), dto.getItem().getSharer().getEmail());

        when(bookingRepository.findAllCurrentOwnerBookings(anyLong(), any(), any(), any()))
                .thenReturn(getPageFromList(List.of(booking)));

        bookingList = bookingService.findAllByOwner(booker.getId(), "CURRENT", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());

        when(bookingRepository.findAllFutureOwnerBookings(anyLong(), any(), any(), any()))
                .thenReturn(getPageFromList(List.of(booking)));

        bookingList = bookingService.findAllByOwner(booker.getId(), "FUTURE", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());

        when(bookingRepository.findAllPastOwnerBookings(anyLong(), any(), any(), any()))
                .thenReturn(getPageFromList(List.of(booking)));

        bookingList = bookingService.findAllByOwner(booker.getId(), "PAST", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());

        when(bookingRepository.findAllOwnerBookingsByStatus(anyLong(), any(), any()))
                .thenReturn(getPageFromList(List.of(booking)));

        bookingList = bookingService.findAllByOwner(booker.getId(), "REJECTED", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());

        bookingList = bookingService.findAllByOwner(booker.getId(), "WAITING", 0,
                10);
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
        assertEquals(1, bookingList.size());
    }

    @DisplayName("Вызов исключения NotFoundException при получении списка бронирований по идентификатору владельца вещи")
    @Test
    void getAllBookingsByOwnerWith404Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.findAllByBooker(owner.getId(), "ALL", 0, 10));
    }

    @DisplayName("Получение бронирования по идентификатору бронирования и бронирующего")
    @Test
    void getBookingByIdAndBooker() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booker));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking));

        BookingDto dto = bookingService.findByBookingId(booker.getId(), booking.getId());
        assertNotNull(dto);
        assertNotNull(dto.getId());
        assertEquals(booking.getId(), dto.getId());
        assertEquals(booking.getStart(), dto.getStart());
        assertEquals(booking.getEnd(), dto.getEnd());
        assertEquals(booking.getStatus(), dto.getStatus());
        assertEquals(booker.getId(), dto.getBooker().getId());
        assertEquals(booker.getName(), dto.getBooker().getName());
        assertEquals(booker.getEmail(), dto.getBooker().getEmail());
        assertEquals(item.getId(), dto.getItem().getId());
        assertEquals(item.getName(), dto.getItem().getName());
        assertEquals(item.getDescription(), dto.getItem().getDescription());
        assertEquals(item.getAvailable(), dto.getItem().getAvailable());
        assertEquals(owner.getId(), dto.getItem().getSharer().getId());
        assertEquals(owner.getName(), dto.getItem().getSharer().getName());
        assertEquals(owner.getEmail(), dto.getItem().getSharer().getEmail());

        booking.setBooker(owner);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(owner));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking));

        dto = bookingService.findByBookingId(owner.getId(), booking.getId());
        assertNotNull(dto);
        assertNotNull(dto.getId());
        assertEquals(booking.getId(), dto.getId());
        assertEquals(booking.getStart(), dto.getStart());
        assertEquals(booking.getEnd(), dto.getEnd());
        assertEquals(booking.getStatus(), dto.getStatus());
        assertEquals(owner.getId(), dto.getBooker().getId());
        assertEquals(owner.getName(), dto.getBooker().getName());
        assertEquals(owner.getEmail(), dto.getBooker().getEmail());
        assertEquals(item.getId(), dto.getItem().getId());
        assertEquals(item.getName(), dto.getItem().getName());
        assertEquals(item.getDescription(), dto.getItem().getDescription());
        assertEquals(item.getAvailable(), dto.getItem().getAvailable());
        assertEquals(owner.getId(), dto.getItem().getSharer().getId());
        assertEquals(owner.getName(), dto.getItem().getSharer().getName());
        assertEquals(owner.getEmail(), dto.getItem().getSharer().getEmail());
    }

    @DisplayName("Вызов исключения IllegalOperationException при получении бронирования по идентификатору бронирования и пользователя")
    @Test
    void getBookingByIdAndBookerWith403Exception() {
        booking.setBooker(owner);

        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.ofNullable(booker));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.ofNullable(booking));

        assertThrows(IllegalOperationException.class,
                () -> bookingService.findByBookingId(booker.getId(), booking.getId()));
    }


    @DisplayName("Вызов исключения NotFoundException при получении бронирования по идентификатору бронирования и пользователя")
    @Test
    void getBookingByIdAndBookerWith404Exception() {
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.findByBookingId(booker.getId(), booking.getId()));

        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.ofNullable(booker));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> bookingService.findByBookingId(booker.getId(), booking.getId()));
    }

    @DisplayName("Создание бронирования")
    @Test
    void createBooking() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booker));

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));

        when(bookingRepository.save(any()))
                .thenReturn(booking);

        BookingDto dto = bookingService.create(booker.getId(), bookingCreateDto);
        assertNotNull(dto);
        assertEquals(booking.getStart(), dto.getStart());
        assertEquals(booking.getEnd(), dto.getEnd());
        assertEquals(booking.getStatus(), dto.getStatus());
        assertEquals(booker.getId(), dto.getBooker().getId());
        assertEquals(booker.getName(), dto.getBooker().getName());
        assertEquals(booker.getEmail(), dto.getBooker().getEmail());
        assertEquals(item.getId(), dto.getItem().getId());
        assertEquals(item.getName(), dto.getItem().getName());
        assertEquals(item.getDescription(), dto.getItem().getDescription());
        assertEquals(item.getAvailable(), dto.getItem().getAvailable());
        assertEquals(owner.getId(), dto.getItem().getSharer().getId());
        assertEquals(owner.getName(), dto.getItem().getSharer().getName());
        assertEquals(owner.getEmail(), dto.getItem().getSharer().getEmail());
    }

    @DisplayName("Вызов исключения IllegalOperationException при создании бронирования")
    @Test
    void createBookingWith400Exception() {
        item.setAvailable(false);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        assertThrows(IllegalOperationException.class, () -> bookingService.create(booker.getId(), bookingCreateDto));
    }

    @DisplayName("Вызов исключения NotFoundException при создании бронирования")
    @Test
    void createBookingWith404Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(), bookingCreateDto));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(), bookingCreateDto));
    }

    @DisplayName("Изменение статуса бронирования")
    @Test
    void approveBooking() {
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking));

        when(bookingRepository.save(any()))
                .thenReturn(booking);

        BookingDto dto = bookingService.approve(owner.getId(), booking.getId(), true);
        assertNotNull(dto);
        assertNotNull(dto.getId());
        assertEquals(booking.getId(), dto.getId());
        assertEquals(booking.getStart(), dto.getStart());
        assertEquals(booking.getEnd(), dto.getEnd());
        assertEquals(booking.getStatus(), dto.getStatus());
        assertEquals(booker.getId(), dto.getBooker().getId());
        assertEquals(booker.getName(), dto.getBooker().getName());
        assertEquals(booker.getEmail(), dto.getBooker().getEmail());
        assertEquals(item.getId(), dto.getItem().getId());
        assertEquals(item.getName(), dto.getItem().getName());
        assertEquals(item.getDescription(), dto.getItem().getDescription());
        assertEquals(item.getAvailable(), dto.getItem().getAvailable());
        assertEquals(owner.getId(), dto.getItem().getSharer().getId());
        assertEquals(owner.getName(), dto.getItem().getSharer().getName());
        assertEquals(owner.getEmail(), dto.getItem().getSharer().getEmail());
    }

    @DisplayName("Вызов исключения IllegalOperationException при изменении статуса бронирования")
    @Test
    void approveBookingWith400Exception() {
        booking.getItem().setSharer(booker);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booking));

        assertThrows(IllegalOperationException.class, () -> bookingService.approve(owner.getId(), booking.getId(), true));
    }

    @DisplayName("Вызов исключения NotFoundException при изменении статуса бронирования")
    @Test
    void approveBookingWith404Exception() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.approve(owner.getId(), booking.getId(), true));
    }
}
