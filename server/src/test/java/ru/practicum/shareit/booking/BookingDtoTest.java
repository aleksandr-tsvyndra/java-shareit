package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка преобразования DTO бронирований в JSON-объекты")
@JsonTest
class BookingDtoTest {

    @Autowired
    private JacksonTester<NewBookingDto> newBookingDtoJacksonTester;

    @Autowired
    private JacksonTester<BookingDto> bookingDtoJacksonTester;

    @Autowired
    private JacksonTester<BookingDtoShort> bookingDtoShortJacksonTester;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UserDto ownerDto;
    private UserDto bookerDto;
    private ItemDto itemDto;

    private NewBookingDto newBookingDto;
    private BookingDto bookingDto;
    private BookingDtoShort bookingDtoShort;


    @BeforeEach
    void setUp() {
        Random random = new Random();

        startDate = LocalDateTime.now().plusDays(1);
        endDate = LocalDateTime.now().plusDays(2);

        ownerDto = UserDto.builder()
                .id(Math.abs(random.nextLong()))
                .name("Owner Test")
                .email("owner@system.com")
                .build();

        bookerDto = UserDto.builder()
                .id(Math.abs(random.nextLong()))
                .name("Booker Test")
                .email("booker@system.com")
                .build();

        itemDto = ItemDto.builder()
                .id(Math.abs(random.nextLong()))
                .name("First item")
                .description("First item description")
                .available(true)
                .sharer(ownerDto)
                .build();

        newBookingDto = NewBookingDto.builder()
                .start(startDate)
                .end(endDate)
                .itemId(itemDto.getId())
                .bookerId(bookerDto.getId())
                .build();

        bookingDto = BookingDto.builder()
                .id(Math.abs(random.nextLong()))
                .start(newBookingDto.getStart())
                .end(newBookingDto.getEnd())
                .item(itemDto)
                .booker(bookerDto)
                .status(BookingStatus.WAITING)
                .build();

        bookingDtoShort = BookingDtoShort.builder()
                .id(bookingDto.getId())
                .bookerId(bookerDto.getId())
                .start(newBookingDto.getStart())
                .end(newBookingDto.getEnd())
                .build();
    }

    @AfterEach
    void tearDown() {
        startDate = null;
        endDate = null;

        ownerDto = null;
        bookerDto = null;

        itemDto = null;

        newBookingDto = null;
        bookingDto = null;
        bookingDtoShort = null;
    }

    @DisplayName("Проверка преобразования в JSON-объект для класса NewBookingDto")
    @Test
    void bookingCreateDtoJacksonTesterTest() throws Exception {
        JsonContent<NewBookingDto> jsonContent = newBookingDtoJacksonTester.write(newBookingDto);

        assertThat(jsonContent).hasJsonPath("$.start");
        assertThat(jsonContent).extractingJsonPathStringValue("$.start")
                .isEqualTo(newBookingDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(jsonContent).hasJsonPath("$.end");
        assertThat(jsonContent).extractingJsonPathStringValue("$.end")
                .isEqualTo(newBookingDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(jsonContent).hasJsonPath("$.itemId");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(newBookingDto.getItemId());

        assertThat(jsonContent).hasJsonPath("$.bookerId");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.bookerId")
                .isEqualTo(newBookingDto.getBookerId());
    }

    @DisplayName("Проверка преобразования в JSON-объект для класса BookingDto")
    @Test
    void bookingFullDtoJacksonTesterTest() throws Exception {
        JsonContent<BookingDto> jsonContent = bookingDtoJacksonTester.write(bookingDto);

        assertThat(jsonContent).hasJsonPath("$.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.id")
                .isEqualTo(bookingDto.getId());

        assertThat(jsonContent).hasJsonPath("$.start");
        assertThat(jsonContent).extractingJsonPathStringValue("$.start")
                .isEqualTo(bookingDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(jsonContent).hasJsonPath("$.end");
        assertThat(jsonContent).extractingJsonPathStringValue("$.end")
                .isEqualTo(bookingDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(jsonContent).hasJsonPath("$.status");
        assertThat(jsonContent).extractingJsonPathStringValue("$.status")
                .isEqualTo(bookingDto.getStatus().name());

        assertThat(jsonContent).hasJsonPath("$.item");
        assertThat(jsonContent).hasJsonPath("$.item.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.item.id")
                .isEqualTo(itemDto.getId());

        assertThat(jsonContent).hasJsonPath("$.item.name");
        assertThat(jsonContent).extractingJsonPathStringValue("$.item.name")
                .isEqualTo(itemDto.getName());

        assertThat(jsonContent).hasJsonPath("$.item.description");
        assertThat(jsonContent).extractingJsonPathStringValue("$.item.description")
                .isEqualTo(itemDto.getDescription());

        assertThat(jsonContent).hasJsonPath("$.item.available");
        assertThat(jsonContent).extractingJsonPathBooleanValue("$.item.available")
                .isEqualTo(itemDto.getAvailable());

        assertThat(jsonContent).hasJsonPath("$.item.sharer");
        assertThat(jsonContent).hasJsonPath("$.item.sharer.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.item.sharer.id")
                .isEqualTo(ownerDto.getId());

        assertThat(jsonContent).hasJsonPath("$.item.sharer.name");
        assertThat(jsonContent).extractingJsonPathStringValue("$.item.sharer.name")
                .isEqualTo(ownerDto.getName());

        assertThat(jsonContent).hasJsonPath("$.item.sharer.email");
        assertThat(jsonContent).extractingJsonPathStringValue("$.item.sharer.email")
                .isEqualTo(ownerDto.getEmail());
    }

    @DisplayName("Проверка преобразования в JSON-объект для класса BookingDtoShort")
    @Test
    void bookingShortDtoJacksonTesterTest() throws Exception {
        JsonContent<BookingDtoShort> jsonContent = bookingDtoShortJacksonTester.write(bookingDtoShort);

        assertThat(jsonContent).hasJsonPath("$.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.id")
                .isEqualTo(bookingDtoShort.getId());

        assertThat(jsonContent).hasJsonPath("$.bookerId");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.bookerId")
                .isEqualTo(bookerDto.getId());

        assertThat(jsonContent).hasJsonPath("$.start");
        assertThat(jsonContent).extractingJsonPathStringValue("$.start")
                .isEqualTo(bookingDtoShort.getStart().format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(jsonContent).hasJsonPath("$.end");
        assertThat(jsonContent).extractingJsonPathStringValue("$.end")
                .isEqualTo(bookingDtoShort.getEnd().format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
