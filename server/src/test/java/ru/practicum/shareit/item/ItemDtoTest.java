package ru.practicum.shareit.item;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.NewCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoComplete;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoShort;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка преобразования DTO вещей в JSON-объекты")
@JsonTest
class ItemDtoTest {

    @Autowired
    private JacksonTester<NewCommentDto> commentCreateDtoJacksonTester;

    @Autowired
    private JacksonTester<CommentDto> commentShortDtoJacksonTester;

    @Autowired
    private JacksonTester<NewItemDto> itemCreateDtoJacksonTester;

    @Autowired
    private JacksonTester<ItemDtoComplete> itemFullDtoJacksonTester;

    @Autowired
    private JacksonTester<ItemDto> itemShortDtoJacksonTester;

    @Autowired
    private JacksonTester<UpdateItemDto> itemUpdateDtoJacksonTester;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UserDto owner;
    private UserDto booker;
    private BookingDtoShort lastBookingShortDto;
    private BookingDtoShort nextBookingShortDto;
    private CommentDto commentDto;
    private ItemRequestDtoShort itemRequestDtoShort;
    private ItemDto itemDto;
    private ItemRequestDto itemRequestDto;
    private ItemDtoComplete itemDtoComplete;
    private NewItemDto newItemDto;
    private NewCommentDto newCommentDto;
    private UpdateItemDto updateItemDto;


    @BeforeEach
    void init() {
        Random random = new Random();

        startDate = LocalDateTime.now().plusDays(-2);
        endDate = LocalDateTime.now().plusDays(-1);

        owner = UserDto.builder().id(Math.abs(random.nextLong())).name("User Test").email("owner@system.com").build();

        booker = UserDto.builder().id(Math.abs(random.nextLong())).name("User Test").email("booker@system.com").build();

        lastBookingShortDto = BookingDtoShort.builder().id(Math.abs(random.nextLong())).bookerId(booker.getId())
                .start(startDate).end(endDate).build();

        nextBookingShortDto = BookingDtoShort.builder().id(Math.abs(random.nextLong())).bookerId(booker.getId())
                .start(lastBookingShortDto.getStart().plusDays(3)).end(lastBookingShortDto.getEnd().plusDays(3))
                .build();

        commentDto = CommentDto.builder().id(Math.abs(random.nextLong())).text("Comment text")
                .authorName(booker.getName()).created(lastBookingShortDto.getEnd().plusHours(1)).build();

        itemRequestDtoShort = ItemRequestDtoShort.builder().id(Math.abs(random.nextLong())).requestor(booker)
                .description("Item request description").created(LocalDateTime.now().minusDays(3)).build();

        itemDto = ItemDto.builder().id(Math.abs(random.nextLong())).name("Item name")
                .description("Item description").sharer(owner).available(true).request(itemRequestDtoShort).build();

        itemRequestDto = ItemRequestDto.builder().id(itemRequestDtoShort.getId())
                .description(itemRequestDtoShort.getDescription()).created(itemRequestDtoShort.getCreated())
                .items(List.of(itemDto)).requestor(itemRequestDtoShort.getRequestor()).build();

        itemDtoComplete = ItemDtoComplete.builder().id(itemDto.getId()).sharer(itemDto.getSharer())
                .name(itemDto.getName()).description(itemDto.getDescription())
                .available(itemDto.getAvailable()).lastBooking(lastBookingShortDto)
                .nextBooking(nextBookingShortDto).comments(List.of(commentDto)).request(itemRequestDto)
                .build();

        newItemDto = NewItemDto.builder().name(itemDto.getName()).description(itemDto.getDescription())
                .available(itemDto.getAvailable()).requestId(itemRequestDtoShort.getId()).build();

        newCommentDto = NewCommentDto.builder().text(commentDto.getText()).authorId(booker.getId())
                .created(commentDto.getCreated()).build();

        updateItemDto = UpdateItemDto.builder().itemId(itemDtoComplete.getId()).name(itemDtoComplete.getName())
                .description(itemDtoComplete.getDescription()).available(itemDtoComplete.getAvailable()).build();
    }

    @AfterEach
    void halt() {
        startDate = null;
        endDate = null;

        owner = null;
        booker = null;

        lastBookingShortDto = null;
        nextBookingShortDto = null;

        commentDto = null;

        itemRequestDtoShort = null;
        itemDto = null;

        itemRequestDto = null;

        itemDtoComplete = null;
        newItemDto = null;
        updateItemDto = null;
    }

    @DisplayName("Проверка преобразования в JSON-объект для класса NewCommentDto")
    @Test
    void commentCreateDtoJacksonTesterTest() throws Exception {
        JsonContent<NewCommentDto> jsonContent = commentCreateDtoJacksonTester.write(newCommentDto);

        assertThat(jsonContent).hasJsonPath("$.text");
        assertThat(jsonContent).extractingJsonPathStringValue("$.text").isEqualTo(newCommentDto.getText());

        assertThat(jsonContent).hasJsonPath("$.authorId");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.authorId").isEqualTo(newCommentDto.getAuthorId());

        assertThat(jsonContent).hasJsonPath("$.created");
        assertThat(jsonContent).extractingJsonPathStringValue("$.created")
                .isEqualTo(newCommentDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME));
    }

    @DisplayName("Проверка преобразования в JSON-объект для класса CommentDto")
    @Test
    void commentShortDtoJacksonTesterTest() throws Exception {
        JsonContent<CommentDto> jsonContent = commentShortDtoJacksonTester.write(commentDto);

        assertThat(jsonContent).hasJsonPath("$.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(commentDto.getId());

        assertThat(jsonContent).hasJsonPath("$.text");
        assertThat(jsonContent).extractingJsonPathStringValue("$.text").isEqualTo(commentDto.getText());

        assertThat(jsonContent).hasJsonPath("$.authorName");
        assertThat(jsonContent).extractingJsonPathStringValue("$.authorName")
                .isEqualTo(commentDto.getAuthorName());

        assertThat(jsonContent).hasJsonPath("$.created");
        assertThat(jsonContent).extractingJsonPathStringValue("$.created")
                .isEqualTo(commentDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME));
    }

    @DisplayName("Проверка преобразования в JSON-объект для класса NewItemDto")
    @Test
    void itemCreateDtoJacksonTesterTest() throws Exception {
        JsonContent<NewItemDto> jsonContent = itemCreateDtoJacksonTester.write(newItemDto);

        assertThat(jsonContent).hasJsonPath("$.name");
        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo(newItemDto.getName());

        assertThat(jsonContent).hasJsonPath("$.description");
        assertThat(jsonContent).extractingJsonPathStringValue("$.description")
                .isEqualTo(newItemDto.getDescription());

        assertThat(jsonContent).hasJsonPath("$.available");
        assertThat(jsonContent).extractingJsonPathBooleanValue("$.available").isEqualTo(newItemDto.getAvailable());

        assertThat(jsonContent).hasJsonPath("$.requestId");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.requestId").isEqualTo(newItemDto.getRequestId());
    }

    @DisplayName("Проверка преобразования в JSON-объект для класса ItemDtoComplete")
    @Test
    void itemFullDtoJacksonTesterTest() throws Exception {
        JsonContent<ItemDtoComplete> jsonContent = itemFullDtoJacksonTester.write(itemDtoComplete);

        assertThat(jsonContent).hasJsonPath("$.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(itemDtoComplete.getId());

        assertThat(jsonContent).hasJsonPath("$.name");
        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo(itemDtoComplete.getName());

        assertThat(jsonContent).hasJsonPath("$.description");
        assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo(itemDtoComplete.getDescription());

        assertThat(jsonContent).hasJsonPath("$.available");
        assertThat(jsonContent).extractingJsonPathBooleanValue("$.available").isEqualTo(itemDtoComplete.getAvailable());

        assertThat(jsonContent).hasJsonPath("$.sharer");
        assertThat(jsonContent).hasJsonPath("$.sharer.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.sharer.id")
                .isEqualTo(owner.getId());

        assertThat(jsonContent).hasJsonPath("$.sharer.name");
        assertThat(jsonContent).extractingJsonPathStringValue("$.sharer.name")
                .isEqualTo(owner.getName());

        assertThat(jsonContent).hasJsonPath("$.sharer.email");
        assertThat(jsonContent).extractingJsonPathStringValue("$.sharer.email")
                .isEqualTo(owner.getEmail());

        assertThat(jsonContent).hasJsonPath("$.lastBooking");
        assertThat(jsonContent).hasJsonPath("$.lastBooking.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.lastBooking.id")
                .isEqualTo(lastBookingShortDto.getId());

        assertThat(jsonContent).hasJsonPath("$.lastBooking.bookerId");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.lastBooking.bookerId")
                .isEqualTo(lastBookingShortDto.getBookerId());

        assertThat(jsonContent).hasJsonPath("$.lastBooking.start");
        assertThat(jsonContent).extractingJsonPathStringValue("$.lastBooking.start")
                .isEqualTo(lastBookingShortDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(jsonContent).hasJsonPath("$.lastBooking.end");
        assertThat(jsonContent).extractingJsonPathStringValue("$.lastBooking.end")
                .isEqualTo(lastBookingShortDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(jsonContent).hasJsonPath("$.nextBooking");
        assertThat(jsonContent).hasJsonPath("$.lastBooking.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.nextBooking.id")
                .isEqualTo(nextBookingShortDto.getId());

        assertThat(jsonContent).hasJsonPath("$.nextBooking.bookerId");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.nextBooking.bookerId")
                .isEqualTo(nextBookingShortDto.getBookerId());

        assertThat(jsonContent).hasJsonPath("$.nextBooking.start");
        assertThat(jsonContent).extractingJsonPathStringValue("$.nextBooking.start")
                .isEqualTo(nextBookingShortDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(jsonContent).hasJsonPath("$.nextBooking.end");
        assertThat(jsonContent).extractingJsonPathStringValue("$.nextBooking.end")
                .isEqualTo(nextBookingShortDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(jsonContent).hasJsonPath("$.request");
        assertThat(jsonContent).hasJsonPath("$.request.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.request.id")
                .isEqualTo(itemRequestDto.getId());

        assertThat(jsonContent).hasJsonPath("$.request.description");
        assertThat(jsonContent).extractingJsonPathStringValue("$.request.description")
                .isEqualTo(itemRequestDto.getDescription());

        assertThat(jsonContent).hasJsonPath("$.request.created");
        assertThat(jsonContent).extractingJsonPathStringValue("$.request.created")
                .isEqualTo(itemRequestDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(jsonContent).hasJsonPath("$.request.requestor");
        assertThat(jsonContent).hasJsonPath("$.request.requestor.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.request.requestor.id")
                .isEqualTo(booker.getId());

        assertThat(jsonContent).hasJsonPath("$.request.requestor.name");
        assertThat(jsonContent).extractingJsonPathStringValue("$.request.requestor.name")
                .isEqualTo(booker.getName());

        assertThat(jsonContent).hasJsonPath("$.request.requestor.email");
        assertThat(jsonContent).extractingJsonPathStringValue("$.request.requestor.email")
                .isEqualTo(booker.getEmail());
    }

    @DisplayName("Проверка преобразования в JSON-объект для класса ItemDto")
    @Test
    void itemShortDtoJacksonTesterTest() throws Exception {
        JsonContent<ItemDto> jsonContent = itemShortDtoJacksonTester.write(itemDto);

        assertThat(jsonContent).hasJsonPath("$.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(itemDto.getId());

        assertThat(jsonContent).hasJsonPath("$.name");
        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo(itemDto.getName());

        assertThat(jsonContent).hasJsonPath("$.description");
        assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo(itemDto.getDescription());

        assertThat(jsonContent).hasJsonPath("$.available");
        assertThat(jsonContent).extractingJsonPathBooleanValue("$.available").isEqualTo(itemDto.getAvailable());

        assertThat(jsonContent).hasJsonPath("$.sharer");
        assertThat(jsonContent).hasJsonPath("$.sharer.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.sharer.id")
                .isEqualTo(owner.getId());

        assertThat(jsonContent).hasJsonPath("$.sharer.name");
        assertThat(jsonContent).extractingJsonPathStringValue("$.sharer.name")
                .isEqualTo(owner.getName());

        assertThat(jsonContent).hasJsonPath("$.sharer.email");
        assertThat(jsonContent).extractingJsonPathStringValue("$.sharer.email")
                .isEqualTo(owner.getEmail());

        assertThat(jsonContent).hasJsonPath("$.request");
        assertThat(jsonContent).hasJsonPath("$.request.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.request.id")
                .isEqualTo(itemRequestDtoShort.getId());

        assertThat(jsonContent).hasJsonPath("$.request.description");
        assertThat(jsonContent).extractingJsonPathStringValue("$.request.description")
                .isEqualTo(itemRequestDtoShort.getDescription());

        assertThat(jsonContent).hasJsonPath("$.request.created");
        assertThat(jsonContent).extractingJsonPathStringValue("$.request.created")
                .isEqualTo(itemRequestDtoShort.getCreated().format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(jsonContent).hasJsonPath("$.request.requestor");
        assertThat(jsonContent).hasJsonPath("$.request.requestor.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.request.requestor.id")
                .isEqualTo(booker.getId());

        assertThat(jsonContent).hasJsonPath("$.request.requestor.name");
        assertThat(jsonContent).extractingJsonPathStringValue("$.request.requestor.name")
                .isEqualTo(booker.getName());

        assertThat(jsonContent).hasJsonPath("$.request.requestor.email");
        assertThat(jsonContent).extractingJsonPathStringValue("$.request.requestor.email")
                .isEqualTo(booker.getEmail());
    }

    @DisplayName("Проверка преобразования в JSON-объект для класса UpdateItemDto")
    @Test
    void itemUpdateDtoJacksonTesterTest() throws Exception {
        JsonContent<UpdateItemDto> jsonContent = itemUpdateDtoJacksonTester.write(updateItemDto);

        assertThat(jsonContent).hasJsonPath("$.itemId");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(updateItemDto.getItemId());

        assertThat(jsonContent).hasJsonPath("$.name");
        assertThat(jsonContent).extractingJsonPathStringValue("$.name")
                .isEqualTo(updateItemDto.getName());

        assertThat(jsonContent).hasJsonPath("$.description");
        assertThat(jsonContent).extractingJsonPathStringValue("$.description")
                .isEqualTo(updateItemDto.getDescription());

        assertThat(jsonContent).hasJsonPath("$.available");
        assertThat(jsonContent).extractingJsonPathBooleanValue("$.available")
                .isEqualTo(updateItemDto.getAvailable());
    }
}
