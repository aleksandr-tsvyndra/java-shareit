package ru.practicum.shareit.request;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoShort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@DisplayName("Обработка данных на уровне сервиса ItemRequestService")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplTest {

    @Autowired
    private final ItemRequestServiceImpl itemRequestService;

    @MockBean
    private final ItemRequestRepository itemRequestRepository;

    @MockBean
    private final UserRepository userRepository;

    @MockBean
    private final ItemRepository itemRepository;

    private User requestor;
    private ItemRequest itemRequest;
    private User owner;
    private Item item;
    private NewItemRequestDto itemRequestCreateDto;

    private static Page<ItemRequest> getPageFromList(List<ItemRequest> list) {
        return new PageImpl<>(list, PageRequest.of(0, 10), list.isEmpty() ? 1 : list.size());
    }

    @BeforeEach
    void setUp() {
        Random random = new Random();

        requestor = User.builder()
                .id(Math.abs(random.nextLong()))
                .name("Requestor Test")
                .email("requestor@system.com")
                .build();

        itemRequest = ItemRequest.builder()
                .id(Math.abs(random.nextLong()))
                .description("Request description")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        owner = User.builder()
                .id(Math.abs(random.nextLong()))
                .name("User Test")
                .email("owner@system.com")
                .build();

        item = Item.builder()
                .id(Math.abs(random.nextLong()))
                .name("Item name")
                .description("Item description")
                .available(true)
                .sharer(owner)
                .request(itemRequest)
                .build();

        itemRequestCreateDto = NewItemRequestDto.builder()
                .description(itemRequest.getDescription())
                .build();
    }

    @AfterEach
    void tearDown() {
        requestor = null;
        itemRequest = null;
        owner = null;
        item = null;
        itemRequestCreateDto = null;
    }

    @DisplayName("Получение списка всех запросов")
    @Test
    void getAll() {
        when(itemRequestRepository.findAll(any(Pageable.class)))
                .thenReturn(getPageFromList(List.of(itemRequest)));

        Collection<ItemRequestDtoShort> requestList = itemRequestService.getAll(0, 10);
        assertNotNull(requestList);
        assertFalse(requestList.isEmpty());
        assertEquals(1, requestList.size());

        Optional<ItemRequestDtoShort> itemRequestShortDtoOpt = requestList.stream().findFirst();
        assertNotNull(itemRequestShortDtoOpt);
        assertTrue(itemRequestShortDtoOpt.isPresent());

        ItemRequestDtoShort itemRequestShortDto = itemRequestShortDtoOpt.get();
        assertNotNull(itemRequestShortDto);
        assertEquals(itemRequest.getId(), itemRequestShortDto.getId());
        assertEquals(requestor.getId(), itemRequestShortDto.getRequestor().getId());
        assertEquals(requestor.getName(), itemRequestShortDto.getRequestor().getName());
        assertEquals(requestor.getEmail(), itemRequestShortDto.getRequestor().getEmail());
        assertEquals(itemRequest.getDescription(), itemRequestShortDto.getDescription());
        assertEquals(itemRequest.getCreated(), itemRequestShortDto.getCreated());
    }

    @DisplayName("Получение пустого списка всех запросов")
    @Test
    void getAllEmptyList() {
        when(itemRequestRepository.findAll(any(Pageable.class)))
                .thenReturn(getPageFromList(new ArrayList<>()));

        Collection<ItemRequestDtoShort> requestList = itemRequestService.getAll(0, 10);
        assertNotNull(requestList);
        assertTrue(requestList.isEmpty());
    }

    @DisplayName("Вызов исключения  при получении списка всех запросов")
    @Test
    void getAllWith500Exception() {
        when(itemRequestRepository.findAll(any(Pageable.class)))
                .thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> itemRequestService.getAll(0, 10));
    }

    @DisplayName("Получение списка бронирований по идентификатору автора")
    @Test
    void getAllByRequestorId() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(requestor));
        when(itemRequestRepository.findByRequestorId(anyLong(), any()))
                .thenReturn(getPageFromList(List.of(itemRequest)));
        when(itemRepository.findByRequestIdIn(anyList()))
                .thenReturn(List.of(item));

        Collection<ItemRequestDto> requestList = itemRequestService.getAllByRequestorId(requestor.getId(), 0,
                10);
        assertNotNull(requestList);
        assertFalse(requestList.isEmpty());
        assertEquals(1, requestList.size());

        Optional<ItemRequestDto> itemRequestFullDtoOpt = requestList.stream().findFirst();
        assertNotNull(itemRequestFullDtoOpt);
        assertTrue(itemRequestFullDtoOpt.isPresent());

        ItemRequestDto itemRequestFullDto = itemRequestFullDtoOpt.get();
        assertNotNull(itemRequestFullDto);
        assertEquals(itemRequest.getId(), itemRequestFullDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestFullDto.getDescription());
        assertEquals(requestor.getId(), itemRequestFullDto.getRequestor().getId());
        assertEquals(requestor.getName(), itemRequestFullDto.getRequestor().getName());
        assertEquals(requestor.getEmail(), itemRequestFullDto.getRequestor().getEmail());
        assertEquals(itemRequest.getCreated(), itemRequestFullDto.getCreated());
        assertNotNull(itemRequestFullDto.getItems());
        assertFalse(itemRequestFullDto.getItems().isEmpty());

        Collection<ItemDto> itemList = itemRequestFullDto.getItems();
        assertNotNull(itemList);
        assertEquals(1, itemList.size());

        Optional<ItemDto> itemShortDtoOpt = itemList.stream().findFirst();
        assertNotNull(itemShortDtoOpt);
        assertTrue(itemShortDtoOpt.isPresent());

        ItemDto itemShortDto = itemShortDtoOpt.get();
        assertNotNull(itemShortDto);
        assertEquals(item.getId(), itemShortDto.getId());
        assertEquals(owner.getId(), itemShortDto.getSharer().getId());
        assertEquals(owner.getName(), itemShortDto.getSharer().getName());
        assertEquals(owner.getEmail(), itemShortDto.getSharer().getEmail());
        assertEquals(item.getName(), itemShortDto.getName());
        assertEquals(item.getDescription(), itemShortDto.getDescription());
        assertEquals(item.getAvailable(), itemShortDto.getAvailable());
        assertEquals(itemRequest.getId(), itemShortDto.getRequest().getId());
        assertEquals(requestor.getId(), itemShortDto.getRequest().getRequestor().getId());
        assertEquals(requestor.getName(), itemShortDto.getRequest().getRequestor().getName());
        assertEquals(requestor.getEmail(), itemShortDto.getRequest().getRequestor().getEmail());
        assertEquals(itemRequest.getDescription(), itemShortDto.getRequest().getDescription());
        assertEquals(itemRequest.getCreated(), itemShortDto.getRequest().getCreated());
    }

    @DisplayName("Вызов исключения NotFoundException при получении списка бронирований по идентификатору автора")
    @Test
    void getAllByRequestorIdWith404Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllByRequestorId(requestor.getId(), 0, 10));
    }

    @DisplayName("Вызов исключения RuntimeException при получении списка бронирований по идентификатору автора")
    @Test
    void getAllByRequestorIdWith500Exception() {
        when(userRepository.findById(anyLong()))
                .thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class,
                () -> itemRequestService.getAllByRequestorId(requestor.getId(), 0, 10));
    }

    @DisplayName("Получение запроса по идентификатору")
    @Test
    void getById() {
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(itemRequest));
        when(itemRepository.findByRequestIdIn(anyList()))
                .thenReturn(List.of(item));

        ItemRequestDto itemRequestFullDto = itemRequestService.getById(itemRequest.getId());
        assertNotNull(itemRequestFullDto);
        assertEquals(itemRequest.getId(), itemRequestFullDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestFullDto.getDescription());
        assertEquals(requestor.getId(), itemRequestFullDto.getRequestor().getId());
        assertEquals(requestor.getName(), itemRequestFullDto.getRequestor().getName());
        assertEquals(requestor.getEmail(), itemRequestFullDto.getRequestor().getEmail());
        assertEquals(itemRequest.getCreated(), itemRequestFullDto.getCreated());
        assertNotNull(itemRequestFullDto.getItems());
        assertFalse(itemRequestFullDto.getItems().isEmpty());

        Collection<ItemDto> itemList = itemRequestFullDto.getItems();
        assertNotNull(itemList);
        assertEquals(1, itemList.size());

        Optional<ItemDto> itemShortDtoOpt = itemList.stream().findFirst();
        assertNotNull(itemShortDtoOpt);
        assertTrue(itemShortDtoOpt.isPresent());

        ItemDto itemShortDto = itemShortDtoOpt.get();
        assertNotNull(itemShortDto);
        assertEquals(item.getId(), itemShortDto.getId());
        assertEquals(owner.getId(), itemShortDto.getSharer().getId());
        assertEquals(owner.getName(), itemShortDto.getSharer().getName());
        assertEquals(owner.getEmail(), itemShortDto.getSharer().getEmail());
        assertEquals(item.getName(), itemShortDto.getName());
        assertEquals(item.getDescription(), itemShortDto.getDescription());
        assertEquals(item.getAvailable(), itemShortDto.getAvailable());
        assertEquals(itemRequest.getId(), itemShortDto.getRequest().getId());
        assertEquals(requestor.getId(), itemShortDto.getRequest().getRequestor().getId());
        assertEquals(requestor.getName(), itemShortDto.getRequest().getRequestor().getName());
        assertEquals(requestor.getEmail(), itemShortDto.getRequest().getRequestor().getEmail());
        assertEquals(itemRequest.getDescription(), itemShortDto.getRequest().getDescription());
        assertEquals(itemRequest.getCreated(), itemShortDto.getRequest().getCreated());
    }

    @DisplayName("Вызов исключения  при получении запроса по идентификатору")
    @Test
    void getByIdWith404Exception() {
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getById(itemRequest.getId()));
    }

    @DisplayName("Создание запроса")
    @Test
    void create() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(requestor));
        when(itemRequestRepository.save(any()))
                .thenReturn(itemRequest);
        when(itemRepository.findByRequestIdIn(anyList()))
                .thenReturn(List.of(item));

        ItemRequestDtoShort itemRequestFullDto = itemRequestService.create(requestor.getId(),
                itemRequestCreateDto);
        assertNotNull(itemRequestFullDto);
        assertEquals(itemRequest.getId(), itemRequestFullDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestFullDto.getDescription());
        assertEquals(requestor.getId(), itemRequestFullDto.getRequestor().getId());
        assertEquals(requestor.getName(), itemRequestFullDto.getRequestor().getName());
        assertEquals(requestor.getEmail(), itemRequestFullDto.getRequestor().getEmail());
        assertEquals(itemRequest.getCreated(), itemRequestFullDto.getCreated());
    }

    @DisplayName("Вызов исключения NotFoundException при создании запроса")
    @Test
    void createWith404Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.create(requestor.getId(), itemRequestCreateDto));
    }

    @DisplayName("Вызов исключения RuntimeException при создании запроса")
    @Test
    void createWith500Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(requestor));
        when(itemRequestRepository.save(any()))
                .thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class,
                () -> itemRequestService.create(requestor.getId(), itemRequestCreateDto));
    }
}
