package ru.practicum.shareit.request;

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
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoShort;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Обработка REST-запросов к /bookings")
@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String X_SHARER_HEADER = "X-Sharer-User-Id";

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    MockMvc mockMvc;

    private UserDto requestor;
    private UserDto owner;
    private ItemRequestDtoShort itemRequestShortDto;
    private ItemDto itemShortDto;
    private ItemRequestDto itemRequestFullDto;
    private NewItemRequestDto itemRequestCreateDto;

    @BeforeEach
    void setUp() {
        Random random = new Random();

        requestor = UserDto.builder()
                .id(Math.abs(random.nextLong()))
                .name("User Test")
                .email("requestor@system.com")
                .build();

        owner = UserDto.builder()
                .id(Math.abs(random.nextLong()))
                .name("User Test")
                .email("owner@system.com")
                .build();

        itemRequestShortDto = ItemRequestDtoShort.builder()
                .id(Math.abs(random.nextLong()))
                .requestor(requestor)
                .description("Request description")
                .created(LocalDateTime.now().minusDays(1))
                .build();

        itemShortDto = ItemDto.builder()
                .id(Math.abs(random.nextLong()))
                .sharer(owner)
                .name("Item name")
                .description("Item description")
                .available(true)
                .request(itemRequestShortDto)
                .build();

        itemRequestFullDto = ItemRequestDto.builder()
                .id(itemRequestShortDto.getId())
                .description(itemRequestShortDto.getDescription())
                .created(itemRequestShortDto.getCreated())
                .requestor(itemRequestShortDto.getRequestor())
                .items(List.of(itemShortDto))
                .build();

        itemRequestCreateDto = NewItemRequestDto.builder()
                .description(itemRequestShortDto.getDescription())
                .build();
    }

    @AfterEach
    void tearDown() {
        requestor = null;
        owner = null;
        itemRequestShortDto = null;
        itemShortDto = null;
        itemRequestFullDto = null;
        itemRequestCreateDto = null;
    }

    @DisplayName("Получение списка запросов")
    @Test
    void getAll() throws Exception {
        when(itemRequestService.getAll(anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestShortDto));

        mockMvc.perform(get("/requests/all")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestShortDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestShortDto.getDescription())))
                .andExpect(jsonPath("$[0].created",
                        is(itemRequestShortDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].requestor.id", is(itemRequestShortDto.getRequestor().getId()), Long.class))
                .andExpect(jsonPath("$[0].requestor.name", is(itemRequestShortDto.getRequestor().getName())))
                .andExpect(jsonPath("$[0].requestor.email", is(itemRequestShortDto.getRequestor().getEmail())));

    }

    @DisplayName("Вызов исключения RuntimeException при получении списка запросов")
    @Test
    void getAllWith500Exception() throws Exception {
        when(itemRequestService.getAll(anyInt(), anyInt()))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(get("/requests/all")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().is5xxServerError())
                .andExpect(status().isInternalServerError());

    }

    @DisplayName("Получение всех запросов по идентификатору автора")
    @Test
    void getAllByRequestorId() throws Exception {
        when(itemRequestService.getAllByRequestorId(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestFullDto));

        mockMvc.perform(get("/requests")
                        .header(X_SHARER_HEADER, requestor.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestFullDto.getId())))
                .andExpect(jsonPath("$[0].description", is(itemRequestFullDto.getDescription())))
                .andExpect(jsonPath("$[0].created",
                        is(itemRequestFullDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].requestor.id", is(requestor.getId()), Long.class))
                .andExpect(jsonPath("$[0].requestor.name", is(requestor.getName())))
                .andExpect(jsonPath("$[0].requestor.email", is(requestor.getEmail())))
                .andExpect(jsonPath("$[0].items[0].id", is(itemShortDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", is(itemShortDto.getName())))
                .andExpect(jsonPath("$[0].items[0].description", is(itemShortDto.getDescription())))
                .andExpect(jsonPath("$[0].items[0].available", is(itemShortDto.getAvailable())))
                .andExpect(jsonPath("$[0].items[0].sharer.id", is(owner.getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].sharer.name", is(owner.getName())))
                .andExpect(jsonPath("$[0].items[0].sharer.email", is(owner.getEmail())))
                .andExpect(jsonPath("$[0].items[0].request.id", is(itemRequestShortDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].request.description", is(itemRequestShortDto.getDescription())))
                .andExpect(jsonPath("$[0].items[0].request.created",
                        is(itemRequestShortDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].items[0].request.requestor.id", is(requestor.getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].request.requestor.name", is(requestor.getName())))
                .andExpect(jsonPath("$[0].items[0].request.requestor.email", is(requestor.getEmail())));
    }

    @DisplayName("Вызов исключения NotFoundException при поиске по идентификатору автора")
    @Test
    void getAllByRequestorIdWith404Exception() throws Exception {
        when(itemRequestService.getAllByRequestorId(anyLong(), anyInt(), anyInt()))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(get("/requests")
                        .header(X_SHARER_HEADER, requestor.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isNotFound());
    }

    @DisplayName("Получение запроса по идентификатору")
    @Test
    void getById() throws Exception {
        when(itemRequestService.getById(anyLong()))
                .thenReturn(itemRequestFullDto);

        mockMvc.perform(get("/requests/" + itemRequestFullDto.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestFullDto.getId())))
                .andExpect(jsonPath("$.description", is(itemRequestFullDto.getDescription())))
                .andExpect(jsonPath("$.created",
                        is(itemRequestFullDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.requestor.id", is(requestor.getId()), Long.class))
                .andExpect(jsonPath("$.requestor.name", is(requestor.getName())))
                .andExpect(jsonPath("$.requestor.email", is(requestor.getEmail())))
                .andExpect(jsonPath("$.items[0].id", is(itemShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.items[0].name", is(itemShortDto.getName())))
                .andExpect(jsonPath("$.items[0].description", is(itemShortDto.getDescription())))
                .andExpect(jsonPath("$.items[0].available", is(itemShortDto.getAvailable())))
                .andExpect(jsonPath("$.items[0].sharer.id", is(owner.getId()), Long.class))
                .andExpect(jsonPath("$.items[0].sharer.name", is(owner.getName())))
                .andExpect(jsonPath("$.items[0].sharer.email", is(owner.getEmail())))
                .andExpect(jsonPath("$.items[0].request.id", is(itemRequestShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.items[0].request.description", is(itemRequestShortDto.getDescription())))
                .andExpect(jsonPath("$.items[0].request.created",
                        is(itemRequestShortDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.items[0].request.requestor.id", is(requestor.getId()), Long.class))
                .andExpect(jsonPath("$.items[0].request.requestor.name", is(requestor.getName())))
                .andExpect(jsonPath("$.items[0].request.requestor.email", is(requestor.getEmail())));
    }

    @DisplayName("Вызов исключения  при поиске запроса по идентификатору")
    @Test
    void findByIdWith404Exception() throws Exception {
        when(itemRequestService.getById(anyLong()))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(get("/requests/" + itemRequestFullDto.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isNotFound());
    }

    @DisplayName("Создание запроса")
    @Test
    void create() throws Exception {
        when(itemRequestService.create(anyLong(), any()))
                .thenReturn(itemRequestShortDto);

        mockMvc.perform(post("/requests")
                        .header(X_SHARER_HEADER, requestor.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestCreateDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemRequestFullDto.getId())))
                .andExpect(jsonPath("$.description", is(itemRequestFullDto.getDescription())))
                .andExpect(jsonPath("$.created",
                        is(itemRequestFullDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.requestor.id", is(requestor.getId()), Long.class))
                .andExpect(jsonPath("$.requestor.name", is(requestor.getName())))
                .andExpect(jsonPath("$.requestor.email", is(requestor.getEmail())));
    }

    @DisplayName("Вызов исключения  при создании запроса")
    @Test
    void createWith4040Exception() throws Exception {
        when(itemRequestService.create(anyLong(), any()))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(post("/requests")
                        .header(X_SHARER_HEADER, requestor.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestCreateDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isNotFound());
    }
}
