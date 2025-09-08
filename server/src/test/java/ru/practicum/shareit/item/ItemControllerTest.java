package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.IllegalOperationException;
import ru.practicum.shareit.item.comment.NewCommentDto;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.ItemDtoComplete;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoShort;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Обработка REST-запросов к /items")
@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    private static final String X_SHARER_HEADER = "X-Sharer-User-Id";

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemService itemService;

    @Autowired
    MockMvc mockMvc;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UserDto owner;
    private UserDto booker;
    private BookingDtoShort lastBookingShortDto;
    private BookingDtoShort nextBookingShortDto;
    private CommentDto commentShortDto;
    private ItemRequestDtoShort itemRequestShortDto;
    private ItemDto itemShortDto;
    private ItemRequestDto itemRequestFullDto;
    private ItemDtoComplete itemFullDto;
    private NewItemDto itemCreateDto;
    private NewCommentDto commentCreateDto;
    private UpdateItemDto itemUpdateDto;

    @BeforeEach
    void init() {
        Random random = new Random();

        startDate = LocalDateTime.now().plusDays(-2);
        endDate = LocalDateTime.now().plusDays(-1);

        owner = UserDto.builder()
                .id(Math.abs(random.nextLong()))
                .name("User Test")
                .email("owner@system.com")
                .build();

        booker = UserDto.builder()
                .id(Math.abs(random.nextLong()))
                .name("User Test")
                .email("booker@system.com")
                .build();

        lastBookingShortDto = BookingDtoShort.builder()
                .id(Math.abs(random.nextLong()))
                .bookerId(booker.getId())
                .start(startDate)
                .end(endDate)
                .build();

        nextBookingShortDto = BookingDtoShort.builder()
                .id(Math.abs(random.nextLong()))
                .bookerId(booker.getId())
                .start(lastBookingShortDto.getStart().plusDays(3))
                .end(lastBookingShortDto.getEnd().plusDays(3))
                .build();

        commentShortDto = CommentDto.builder()
                .id(Math.abs(random.nextLong()))
                .text("Comment text")
                .authorName(booker.getName())
                .created(lastBookingShortDto.getEnd().plusHours(1))
                .build();

        itemRequestShortDto = ItemRequestDtoShort.builder()
                .id(Math.abs(random.nextLong()))
                .requestor(booker)
                .description("Item request description")
                .created(LocalDateTime.now().minusDays(3))
                .build();

        itemShortDto = ItemDto.builder()
                .id(Math.abs(random.nextLong()))
                .name("Item name")
                .description("Item description")
                .sharer(owner)
                .available(true)
                .request(itemRequestShortDto)
                .build();

        itemRequestFullDto = ItemRequestDto.builder()
                .id(itemRequestShortDto.getId())
                .description(itemRequestShortDto.getDescription())
                .created(itemRequestShortDto.getCreated())
                .items(List.of(itemShortDto))
                .requestor(itemRequestShortDto.getRequestor())
                .build();

        itemFullDto = ItemDtoComplete.builder()
                .id(itemShortDto.getId())
                .sharer(itemShortDto.getSharer())
                .name(itemShortDto.getName())
                .description(itemShortDto.getDescription())
                .available(itemShortDto.getAvailable())
                .lastBooking(lastBookingShortDto)
                .nextBooking(nextBookingShortDto)
                .comments(List.of(commentShortDto))
                .request(itemRequestFullDto)
                .build();

        itemCreateDto = NewItemDto.builder()
                .name(itemShortDto.getName())
                .description(itemShortDto.getDescription())
                .available(itemShortDto.getAvailable())
                .requestId(itemRequestShortDto.getId())
                .build();

        commentCreateDto = NewCommentDto.builder()
                .text(commentShortDto.getText())
                .authorId(booker.getId())
                .created(commentShortDto.getCreated())
                .build();

        itemUpdateDto = UpdateItemDto.builder()
                .itemId(itemFullDto.getId())
                .name(itemFullDto.getName())
                .description(itemFullDto.getDescription())
                .available(itemFullDto.getAvailable())
                .build();
    }

    @AfterEach
    void halt() {
        startDate = null;
        endDate = null;

        owner = null;
        booker = null;

        lastBookingShortDto = null;
        nextBookingShortDto = null;

        commentShortDto = null;

        itemRequestShortDto = null;
        itemShortDto = null;

        itemRequestFullDto = null;

        itemFullDto = null;
        itemCreateDto = null;
        itemUpdateDto = null;
    }

    @DisplayName("Получение списка вещей")
    @Test
    void findAll() throws Exception {
        when(itemService.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemFullDto));

        mockMvc.perform(get("/items")
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemFullDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemFullDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemFullDto.getDescription())))
                .andExpect(jsonPath("$[0].sharer.id", is(owner.getId()), Long.class))
                .andExpect(jsonPath("$[0].sharer.name", is(owner.getName())))
                .andExpect(jsonPath("$[0].sharer.email", is(owner.getEmail())))
                .andExpect(jsonPath("$[0].lastBooking.id", is(lastBookingShortDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.bookerId", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.start",
                        is(lastBookingShortDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].lastBooking.end",
                        is(lastBookingShortDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].nextBooking.id", is(nextBookingShortDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.bookerId", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.start",
                        is(nextBookingShortDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].nextBooking.end",
                        is(nextBookingShortDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].comments[0].id", is(commentShortDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].text", is(commentShortDto.getText())))
                .andExpect(jsonPath("$[0].comments[0].authorName", is(booker.getName())))
                .andExpect(jsonPath("$[0].comments[0].created",
                        is(commentShortDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].request.id", is(itemRequestFullDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].request.description", is(itemRequestFullDto.getDescription())))
                .andExpect(jsonPath("$[0].request.created",
                        is(itemRequestFullDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].request.requestor.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$[0].request.requestor.name", is(booker.getName())))
                .andExpect(jsonPath("$[0].request.requestor.email", is(booker.getEmail())))
                .andExpect(jsonPath("$[0].request.items[0].id", is(itemShortDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].request.items[0].name", is(itemShortDto.getName())))
                .andExpect(jsonPath("$[0].request.items[0].description", is(itemShortDto.getDescription())))
                .andExpect(jsonPath("$[0].request.items[0].available", is(itemShortDto.getAvailable())))
                .andExpect(jsonPath("$[0].request.items[0].sharer.id", is(owner.getId()), Long.class))
                .andExpect(jsonPath("$[0].request.items[0].sharer.name", is(owner.getName())))
                .andExpect(jsonPath("$[0].request.items[0].sharer.email", is(owner.getEmail())))
                .andExpect(jsonPath("$[0].request.items[0].request.id", is(itemRequestShortDto.getId()), Long.class))
                .andExpect(
                        jsonPath("$[0].request.items[0].request.description", is(itemRequestShortDto.getDescription())))
                .andExpect(jsonPath("$[0].request.items[0].request.created",
                        is(itemRequestShortDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].request.items[0].request.requestor.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$[0].request.items[0].request.requestor.name", is(booker.getName())))
                .andExpect(jsonPath("$[0].request.items[0].request.requestor.email", is(booker.getEmail())));

    }

    @DisplayName("Вызов исключения IllegalOperationException при получении списка вещей")
    @Test
    void findAllWith403Exception() throws Exception {
        when(itemService.getAll(anyLong(), anyInt(), anyInt()))
                .thenThrow(IllegalOperationException.class);

        mockMvc.perform(get("/items")
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Вызов исключения NotFoundException при получении списка вещей")
    @Test
    void findAllWith404Exception() throws Exception {
        when(itemService.getAll(anyLong(), anyInt(), anyInt()))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items")
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isNotFound());
    }

    @DisplayName("Вызов исключения RuntimeException при получении списка вещей")
    @Test
    void findAllWith500Exception() throws Exception {
        when(itemService.getAll(anyLong(), anyInt(), anyInt()))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(get("/items")
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().is5xxServerError())
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("Поиск вещей по поисковой строке")
    @Test
    void findByText() throws Exception {
        when(itemService.search(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(itemShortDto));

        mockMvc.perform(get("/items/search?text=" + itemShortDto.getName())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("text", itemShortDto.getName())
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemShortDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemShortDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemShortDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemShortDto.getAvailable())))
                .andExpect(jsonPath("$[0].sharer.id", is(owner.getId())))
                .andExpect(jsonPath("$[0].sharer.name", is(owner.getName())))
                .andExpect(jsonPath("$[0].sharer.email", is(owner.getEmail())))
                .andExpect(jsonPath("$[0].request.id", is(itemRequestShortDto.getId())))
                .andExpect(jsonPath("$[0].request.description", is(itemRequestShortDto.getDescription())))
                .andExpect(jsonPath("$[0].request.created",
                        is(itemRequestShortDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].request.requestor.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$[0].request.requestor.name", is(booker.getName())))
                .andExpect(jsonPath("$[0].request.requestor.email", is(booker.getEmail())));
    }

    @DisplayName("Поиск вещей по пустой поисковой строке")
    @Test
    void findByTextWithEmptyCollection() throws Exception {
        mockMvc.perform(get("/items/search?text=")
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @DisplayName("Вызов исключения ValidationException при поиске вещей по поисковой строке")
    @Test
    void findByTextWith400Exception() throws Exception {
        when(itemService.search(anyString(), anyInt(), anyInt()))
                .thenThrow(ValidationException.class);

        mockMvc.perform(get("/items/search?text=" + itemShortDto.getName())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("text", itemShortDto.getName())
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Вызов исключения RuntimeException при поиске вещей по поисковой строке")
    @Test
    void findByTextWith500Exception() throws Exception {
        when(itemService.search(anyString(), anyInt(), anyInt()))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(get("/items/search?text=" + itemShortDto.getName())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("text", itemShortDto.getName())
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().is5xxServerError())
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("Поиск вещи по идентификатору")
    @Test
    void findById() throws Exception {
        when(itemService.getById(anyLong(), anyLong()))
                .thenReturn(itemFullDto);

        mockMvc.perform(get("/items/" + itemFullDto.getId())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemFullDto.getName())))
                .andExpect(jsonPath("$.description", is(itemFullDto.getDescription())))
                .andExpect(jsonPath("$.sharer.id", is(owner.getId()), Long.class))
                .andExpect(jsonPath("$.sharer.name", is(owner.getName())))
                .andExpect(jsonPath("$.sharer.email", is(owner.getEmail())))
                .andExpect(jsonPath("$.lastBooking.id", is(lastBookingShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.start",
                        is(lastBookingShortDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.lastBooking.end",
                        is(lastBookingShortDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.nextBooking.id", is(nextBookingShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.start",
                        is(nextBookingShortDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.nextBooking.end",
                        is(nextBookingShortDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.comments[0].id", is(commentShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(commentShortDto.getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(booker.getName())))
                .andExpect(jsonPath("$.comments[0].created",
                        is(commentShortDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.request.id", is(itemRequestFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.request.description", is(itemRequestFullDto.getDescription())))
                .andExpect(jsonPath("$.request.created",
                        is(itemRequestFullDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.request.requestor.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.request.requestor.name", is(booker.getName())))
                .andExpect(jsonPath("$.request.requestor.email", is(booker.getEmail())))
                .andExpect(jsonPath("$.request.items[0].id", is(itemShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.request.items[0].name", is(itemShortDto.getName())))
                .andExpect(jsonPath("$.request.items[0].description", is(itemShortDto.getDescription())))
                .andExpect(jsonPath("$.request.items[0].available", is(itemShortDto.getAvailable())))
                .andExpect(jsonPath("$.request.items[0].sharer.id", is(owner.getId()), Long.class))
                .andExpect(jsonPath("$.request.items[0].sharer.name", is(owner.getName())))
                .andExpect(jsonPath("$.request.items[0].sharer.email", is(owner.getEmail())))
                .andExpect(jsonPath("$.request.items[0].request.id", is(itemRequestShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.request.items[0].request.description", is(itemRequestShortDto.getDescription())))
                .andExpect(jsonPath("$.request.items[0].request.created",
                        is(itemRequestShortDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.request.items[0].request.requestor.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.request.items[0].request.requestor.name", is(booker.getName())))
                .andExpect(jsonPath("$.request.items[0].request.requestor.email", is(booker.getEmail())));
    }

    @DisplayName("Вызов исключения ValidationException при поиске вещи по идентификатору")
    @Test
    void findByIdWith400Exception() throws Exception {
        when(itemService.getById(anyLong(), anyLong()))
                .thenThrow(ValidationException.class);

        mockMvc.perform(get("/items/" + itemFullDto.getId())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Вызов исключения IllegalOperationException при поиске вещи по идентификатору")
    @Test
    void findByIdWith403Exception() throws Exception {
        when(itemService.getById(anyLong(), anyLong()))
                .thenThrow(IllegalOperationException.class);

        mockMvc.perform(get("/items/" + itemFullDto.getId())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Вызов исключения NotFoundException при поиске вещи по идентификатору")
    @Test
    void findByIdWith404Exception() throws Exception {
        when(itemService.getById(anyLong(), anyLong()))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items/" + itemFullDto.getId())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isNotFound());
    }

    @DisplayName("Вызов исключения RuntimeException при поиске вещи по идентификатору")
    @Test
    void findByIdWith500Exception() throws Exception {
        when(itemService.getById(anyLong(), anyLong()))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(get("/items/" + itemFullDto.getId())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().is5xxServerError())
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("Создание вещи по запросу")
    @Test
    void createItem() throws Exception {
        when(itemService.create(anyLong(), any()))
                .thenReturn(itemShortDto);

        mockMvc.perform(post("/items")
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(itemCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemShortDto.getName())))
                .andExpect(jsonPath("$.description", is(itemShortDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemShortDto.getAvailable())))
                .andExpect(jsonPath("$.sharer.id", is(owner.getId())))
                .andExpect(jsonPath("$.sharer.name", is(owner.getName())))
                .andExpect(jsonPath("$.sharer.email", is(owner.getEmail())))
                .andExpect(jsonPath("$.request.id", is(itemRequestShortDto.getId())))
                .andExpect(jsonPath("$.request.description", is(itemRequestShortDto.getDescription())))
                .andExpect(jsonPath("$.request.created",
                        is(itemRequestShortDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.request.requestor.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.request.requestor.name", is(booker.getName())))
                .andExpect(jsonPath("$.request.requestor.email", is(booker.getEmail())));
    }

    @DisplayName("Создание вещи без запроса")
    @Test
    void createItemWithoutRequest() throws Exception {
        itemCreateDto.setRequestId(null);
        itemShortDto.setRequest(null);

        when(itemService.create(anyLong(), any()))
                .thenReturn(itemShortDto);

        mockMvc.perform(post("/items")
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(itemCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemShortDto.getName())))
                .andExpect(jsonPath("$.description", is(itemShortDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemShortDto.getAvailable())))
                .andExpect(jsonPath("$.sharer.id", is(owner.getId())))
                .andExpect(jsonPath("$.sharer.name", is(owner.getName())))
                .andExpect(jsonPath("$.sharer.email", is(owner.getEmail())))
                .andExpect(jsonPath("$.request").isEmpty());
    }

    @DisplayName("Вызов исключения ValidationException при создании вещи по запросу")
    @Test
    void createItemWith400Exception() throws Exception {
        when(itemService.create(anyLong(), any()))
                .thenThrow(ValidationException.class);

        mockMvc.perform(post("/items")
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(itemCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Вызов исключения NotFoundException при создании вещи по запросу")
    @Test
    void createItemWith404Exception() throws Exception {
        when(itemService.create(anyLong(), any()))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(post("/items")
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(itemCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isNotFound());
    }

    @DisplayName("Вызов исключения RuntimeException при создании вещи по запросу")
    @Test
    void createItemWith500Exception() throws Exception {
        when(itemService.create(anyLong(), any()))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(post("/items")
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(itemCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("Создание комментария на завершённую аренду")
    @Test
    void createComment() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenReturn(commentShortDto);

        mockMvc.perform(post("/items/" + itemShortDto.getId() + "/comment")
                        .header(X_SHARER_HEADER, booker.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(commentCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentShortDto.getText())))
                .andExpect(jsonPath("$.authorName", is(booker.getName())))
                .andExpect(jsonPath("$.created",
                        is(commentShortDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))));
    }

    @DisplayName("Вызов исключения ValidationException при добавлении комментария")
    @Test
    void createCommentWith400Exception() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenThrow(ValidationException.class);

        mockMvc.perform(post("/items/" + itemShortDto.getId() + "/comment")
                        .header(X_SHARER_HEADER, booker.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(commentCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Вызов исключения IllegalOperationException при добавлении комментария")
    @Test
    void createCommentWith403Exception() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenThrow(IllegalOperationException.class);

        mockMvc.perform(post("/items/" + itemShortDto.getId() + "/comment")
                        .header(X_SHARER_HEADER, booker.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(commentCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Вызов исключения NotFoundException при добавлении комментария")
    @Test
    void createCommentWith404Exception() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(post("/items/" + itemShortDto.getId() + "/comment")
                        .header(X_SHARER_HEADER, booker.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(commentCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isNotFound());
    }

    @DisplayName("Вызов исключения RuntimeException при добавлении комментария")
    @Test
    void createCommentWith500Exception() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(post("/items/" + itemShortDto.getId() + "/comment")
                        .header(X_SHARER_HEADER, booker.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(commentCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("Обновление вещи по идентификатору")
    @Test
    void updateItem() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any()))
                .thenReturn(itemShortDto);

        mockMvc.perform(patch("/items/" + itemUpdateDto.getItemId())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(itemUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemShortDto.getName())))
                .andExpect(jsonPath("$.description", is(itemShortDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemShortDto.getAvailable())))
                .andExpect(jsonPath("$.sharer.id", is(owner.getId())))
                .andExpect(jsonPath("$.sharer.name", is(owner.getName())))
                .andExpect(jsonPath("$.sharer.email", is(owner.getEmail())))
                .andExpect(jsonPath("$.request.id", is(itemRequestShortDto.getId())))
                .andExpect(jsonPath("$.request.description", is(itemRequestShortDto.getDescription())))
                .andExpect(jsonPath("$.request.created", is(itemRequestShortDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.request.requestor.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.request.requestor.name", is(booker.getName())))
                .andExpect(jsonPath("$.request.requestor.email", is(booker.getEmail())));
    }

    @DisplayName("Вызов исключения ValidationException при обновлении по идентификатору вещи")
    @Test
    void updateItemWith400Exception() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any()))
                .thenThrow(ValidationException.class);

        mockMvc.perform(patch("/items/" + itemUpdateDto.getItemId())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(itemUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Вызов исключения IllegalOperationException при обновлении по идентификатору вещи")
    @Test
    void updateItemWith403Exception() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any()))
                .thenThrow(IllegalOperationException.class);

        mockMvc.perform(patch("/items/" + itemUpdateDto.getItemId())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(itemUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Вызов исключения NotFoundException при обновлении по идентификатору вещи")
    @Test
    void updateItemWith404Exception() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any()))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(patch("/items/" + itemUpdateDto.getItemId())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(itemUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isNotFound());
    }

    @DisplayName("Вызов исключения RuntimeException при обновлении по идентификатору вещи")
    @Test
    void updateItemWith500Exception() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any()))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(patch("/items/" + itemUpdateDto.getItemId())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(itemUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("Удаление вещи по идентификатору")
    @Test
    void deleteItem() throws Exception {
        mockMvc.perform(delete("/items/" + itemFullDto.getId())
                        .header(X_SHARER_HEADER, owner.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(itemService).delete(owner.getId(), itemFullDto.getId());
    }
}
