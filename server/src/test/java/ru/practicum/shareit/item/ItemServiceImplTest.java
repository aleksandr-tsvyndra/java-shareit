package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.IllegalOperationException;
import ru.practicum.shareit.item.comment.NewCommentDto;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.ItemDtoComplete;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Обработка данных на уровне сервиса ItemService")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {

    @Autowired
    private final ItemServiceImpl itemService;

    @MockBean
    private final ItemRepository itemRepository;

    @MockBean
    private final UserRepository userRepository;

    @MockBean
    private final ItemRequestRepository itemRequestRepository;

    @MockBean
    private final BookingRepository bookingRepository;

    @MockBean
    private final CommentRepository commentRepository;

    private User owner;
    private User booker;
    private ItemRequest itemRequest;
    private Item item;
    private Booking lastBooking;
    private Booking nextBooking;
    private Comment comment;
    private NewItemDto itemCreateDto;
    private NewCommentDto commentCreateDto;
    private UpdateItemDto itemUpdateDto;

    private static Page<Item> getPageFromListForItem(List<Item> list) {
        return new PageImpl<>(list, PageRequest.of(0, list.isEmpty() ? 1 : list.size()), list.size());
    }

    @BeforeEach
    void setUp() {
        Random random = new Random();

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

        itemRequest = ItemRequest.builder()
                .id(Math.abs(random.nextLong()))
                .description("Request description")
                .requestor(booker)
                .created(LocalDateTime.now().minusDays(3))
                .build();

        item = Item.builder()
                .id(Math.abs(random.nextLong()))
                .sharer(owner)
                .name("Item test")
                .description("Item description")
                .available(true)
                .request(itemRequest)
                .build();

        lastBooking = Booking.builder()
                .id(Math.abs(random.nextLong()))
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        nextBooking = Booking.builder()
                .id(Math.abs(random.nextLong()))
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        comment = Comment.builder()
                .id(Math.abs(random.nextLong()))
                .text("Comment text")
                .item(item)
                .author(booker)
                .created(lastBooking.getEnd().plusDays(1))
                .build();

        itemCreateDto = NewItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest().getId())
                .build();

        commentCreateDto = NewCommentDto.builder()
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .created(comment.getCreated())
                .build();

        itemUpdateDto = UpdateItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    @AfterEach
    void tearDown() {
        owner = null;
        booker = null;
        itemRequest = null;
        item = null;
        lastBooking = null;
        nextBooking = null;
        comment = null;
        itemCreateDto = null;
        commentCreateDto = null;
    }

    @DisplayName("Получение списка вещей по идентификатору владельца")
    @Test
    void getAll() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findAllBySharerId(anyLong(), any()))
                .thenReturn(getPageFromListForItem(List.of(item)));
        when(itemRepository.findByRequestIdIn(any()))
                .thenReturn(List.of(item));
        when(bookingRepository.findFirstBookingByItemIdAndEndIsBeforeAndStatus(anyLong(), any(), any(),
                any()))
                .thenReturn(lastBooking);
        when(bookingRepository.findFirstBookingByItemIdAndEndIsAfterAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(nextBooking);
        when(commentRepository.findAllByItemId(anyLong(), any()))
                .thenReturn(List.of(comment));

        Collection<ItemDtoComplete> itemList = itemService.getAll(owner.getId(), 0, 10);
        assertNotNull(itemList);
        assertFalse(itemList.isEmpty());
        assertEquals(1, itemList.size());

        Optional<ItemDtoComplete> itemDtoOpt = itemList.stream().findFirst();
        assertTrue(itemDtoOpt.isPresent());

        ItemDtoComplete itemDto = itemDtoOpt.get();
        assertNotNull(itemDto);
        assertNotNull(itemDto.getId());
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
        assertEquals(owner.getId(), itemDto.getSharer().getId());
        assertEquals(owner.getName(), itemDto.getSharer().getName());
        assertEquals(owner.getEmail(), itemDto.getSharer().getEmail());
        assertEquals(lastBooking.getId(), itemDto.getLastBooking().getId());
        assertEquals(lastBooking.getStart(), itemDto.getLastBooking().getStart());
        assertEquals(lastBooking.getEnd(), itemDto.getLastBooking().getEnd());
        assertEquals(lastBooking.getBooker().getId(), itemDto.getLastBooking().getBookerId());
        assertEquals(nextBooking.getId(), itemDto.getNextBooking().getId());
        assertEquals(nextBooking.getStart(), itemDto.getNextBooking().getStart());
        assertEquals(nextBooking.getEnd(), itemDto.getNextBooking().getEnd());
        assertEquals(nextBooking.getBooker().getId(), itemDto.getNextBooking().getBookerId());
        assertEquals(itemRequest.getId(), itemDto.getRequest().getId());
        assertEquals(itemRequest.getDescription(), itemDto.getRequest().getDescription());
        assertEquals(itemRequest.getCreated(), itemDto.getRequest().getCreated());
        assertEquals(booker.getId(), itemDto.getRequest().getRequestor().getId());
        assertEquals(booker.getName(), itemDto.getRequest().getRequestor().getName());
        assertEquals(booker.getEmail(), itemDto.getRequest().getRequestor().getEmail());
        assertNotNull(itemDto.getRequest().getItems());
        assertFalse(itemDto.getRequest().getItems().isEmpty());

        Optional<ItemDto> itemShortDtoOpt = itemDto.getRequest().getItems().stream().findFirst();
        assertNotNull(itemShortDtoOpt);
        ItemDto itemShortDto = itemShortDtoOpt.get();
        assertNotNull(itemShortDto);
        assertEquals(item.getId(), itemShortDto.getId());
        assertEquals(item.getName(), itemShortDto.getName());
        assertEquals(item.getDescription(), itemShortDto.getDescription());
        assertEquals(item.getAvailable(), itemShortDto.getAvailable());
        assertEquals(owner.getId(), itemShortDto.getSharer().getId());
        assertEquals(owner.getName(), itemShortDto.getSharer().getName());
        assertEquals(owner.getEmail(), itemShortDto.getSharer().getEmail());
        assertEquals(itemRequest.getId(), itemShortDto.getRequest().getId());
        assertEquals(itemRequest.getDescription(), itemShortDto.getRequest().getDescription());
        assertEquals(itemRequest.getCreated(), itemShortDto.getRequest().getCreated());
        assertEquals(booker.getId(), itemShortDto.getRequest().getRequestor().getId());
        assertEquals(booker.getName(), itemShortDto.getRequest().getRequestor().getName());
        assertEquals(booker.getEmail(), itemShortDto.getRequest().getRequestor().getEmail());

        assertNotNull(itemDto.getComments());
        assertFalse(itemDto.getComments().isEmpty());
        assertEquals(1, itemDto.getComments().size());
        Optional<CommentDto> commentShortDtoOpt = itemDto.getComments().stream().findFirst();
        assertNotNull(commentShortDtoOpt);
        assertTrue(commentShortDtoOpt.isPresent());
        CommentDto commentShortDto = commentShortDtoOpt.get();
        assertNotNull(commentShortDto);
        assertNotNull(commentShortDto.getId());
        assertEquals(comment.getId(), commentShortDto.getId());
        assertEquals(comment.getText(), commentShortDto.getText());
        assertEquals(comment.getAuthor().getName(), commentShortDto.getAuthorName());
        assertEquals(comment.getCreated(), commentShortDto.getCreated());
    }

    @DisplayName("Получение списка вещей по вхождению подстроки")
    @Test
    void search() {
        when(itemRepository.search(anyString(), any()))
                .thenReturn(getPageFromListForItem(List.of(item)));

        Collection<ItemDto> itemList = itemService.search(item.getName(), 0, 10);
        assertNotNull(itemList);
        assertFalse(itemList.isEmpty());
        assertEquals(1, itemList.size());

        Optional<ItemDto> itemShortDtoOpt = itemList.stream().findFirst();
        assertNotNull(itemShortDtoOpt);
        assertTrue(itemShortDtoOpt.isPresent());
        ItemDto itemShortDto = itemShortDtoOpt.get();
        assertNotNull(itemShortDto);
        assertEquals(item.getId(), itemShortDto.getId());
        assertEquals(item.getName(), itemShortDto.getName());
        assertEquals(item.getDescription(), itemShortDto.getDescription());
        assertEquals(item.getAvailable(), itemShortDto.getAvailable());
        assertEquals(owner.getId(), itemShortDto.getSharer().getId());
        assertEquals(owner.getName(), itemShortDto.getSharer().getName());
        assertEquals(owner.getEmail(), itemShortDto.getSharer().getEmail());
        assertEquals(itemRequest.getId(), itemShortDto.getRequest().getId());
        assertEquals(booker.getId(), itemShortDto.getRequest().getRequestor().getId());
        assertEquals(booker.getName(), itemShortDto.getRequest().getRequestor().getName());
        assertEquals(booker.getEmail(), itemShortDto.getRequest().getRequestor().getEmail());
        assertEquals(itemRequest.getDescription(), itemShortDto.getRequest().getDescription());
        assertEquals(itemRequest.getCreated(), itemShortDto.getRequest().getCreated());
    }

    @DisplayName("Получение пустого списка вещей по вхождению подстроки")
    @Test
    void findByTextEmptyList() {
        when(itemRepository.search(anyString(), any()))
                .thenReturn(getPageFromListForItem(new ArrayList<>()));

        Collection<ItemDto> itemList = itemService.search(item.getName(), 0, 10);
        assertNotNull(itemList);
        assertTrue(itemList.isEmpty());
    }

    @DisplayName("Получение вещи по идентификатору")
    @Test
    void findById() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(owner));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(itemRepository.findByRequestIdIn(any()))
                .thenReturn(List.of(item));
        when(bookingRepository.findFirstBookingByItemIdAndEndIsBeforeAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(lastBooking);
        when(bookingRepository.findFirstBookingByItemIdAndEndIsAfterAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(nextBooking);
        when(commentRepository.findAllByItemId(anyLong(), any()))
                .thenReturn(List.of(comment));

        ItemDtoComplete itemDto = itemService.getById(item.getId(), owner.getId());
        assertNotNull(itemDto);
        assertNotNull(itemDto.getId());
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
        assertEquals(owner.getId(), itemDto.getSharer().getId());
        assertEquals(owner.getName(), itemDto.getSharer().getName());
        assertEquals(owner.getEmail(), itemDto.getSharer().getEmail());
        assertEquals(lastBooking.getId(), itemDto.getLastBooking().getId());
        assertEquals(lastBooking.getStart(), itemDto.getLastBooking().getStart());
        assertEquals(lastBooking.getEnd(), itemDto.getLastBooking().getEnd());
        assertEquals(lastBooking.getBooker().getId(), itemDto.getLastBooking().getBookerId());
        assertEquals(nextBooking.getId(), itemDto.getNextBooking().getId());
        assertEquals(nextBooking.getStart(), itemDto.getNextBooking().getStart());
        assertEquals(nextBooking.getEnd(), itemDto.getNextBooking().getEnd());
        assertEquals(nextBooking.getBooker().getId(), itemDto.getNextBooking().getBookerId());
        assertEquals(itemRequest.getId(), itemDto.getRequest().getId());
        assertEquals(itemRequest.getDescription(), itemDto.getRequest().getDescription());
        assertEquals(itemRequest.getCreated(), itemDto.getRequest().getCreated());
        assertEquals(booker.getId(), itemDto.getRequest().getRequestor().getId());
        assertEquals(booker.getName(), itemDto.getRequest().getRequestor().getName());
        assertEquals(booker.getEmail(), itemDto.getRequest().getRequestor().getEmail());
        assertNotNull(itemDto.getRequest().getItems());
        assertFalse(itemDto.getRequest().getItems().isEmpty());

        Optional<ItemDto> itemShortDtoOpt = itemDto.getRequest().getItems().stream().findFirst();
        assertNotNull(itemShortDtoOpt);
        ItemDto itemShortDto = itemShortDtoOpt.get();
        assertNotNull(itemShortDto);
        assertEquals(item.getId(), itemShortDto.getId());
        assertEquals(item.getName(), itemShortDto.getName());
        assertEquals(item.getDescription(), itemShortDto.getDescription());
        assertEquals(item.getAvailable(), itemShortDto.getAvailable());
        assertEquals(owner.getId(), itemShortDto.getSharer().getId());
        assertEquals(owner.getName(), itemShortDto.getSharer().getName());
        assertEquals(owner.getEmail(), itemShortDto.getSharer().getEmail());
        assertEquals(itemRequest.getId(), itemShortDto.getRequest().getId());
        assertEquals(itemRequest.getDescription(), itemShortDto.getRequest().getDescription());
        assertEquals(itemRequest.getCreated(), itemShortDto.getRequest().getCreated());
        assertEquals(booker.getId(), itemShortDto.getRequest().getRequestor().getId());
        assertEquals(booker.getName(), itemShortDto.getRequest().getRequestor().getName());
        assertEquals(booker.getEmail(), itemShortDto.getRequest().getRequestor().getEmail());

        assertNotNull(itemDto.getComments());
        assertFalse(itemDto.getComments().isEmpty());
        assertEquals(1, itemDto.getComments().size());
        Optional<CommentDto> commentShortDtoOpt = itemDto.getComments().stream().findFirst();
        assertNotNull(commentShortDtoOpt);
        assertTrue(commentShortDtoOpt.isPresent());
        CommentDto commentShortDto = commentShortDtoOpt.get();
        assertNotNull(commentShortDto);
        assertNotNull(commentShortDto.getId());
        assertEquals(comment.getId(), commentShortDto.getId());
        assertEquals(comment.getText(), commentShortDto.getText());
        assertEquals(comment.getAuthor().getName(), commentShortDto.getAuthorName());
        assertEquals(comment.getCreated(), commentShortDto.getCreated());
    }

    @DisplayName("Вызов исключения NotFoundException при получении вещи по идентификатору")
    @Test
    void findByIdWith404Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getById(owner.getId(), item.getId()));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(owner));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getById(owner.getId(), item.getId()));
    }

    @DisplayName("Создание вещи")
    @Test
    void create() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(owner));
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(itemRequest));
        when(itemRepository.save(any()))
                .thenReturn(item);

        ItemDto itemShortDto = itemService.create(owner.getId(), itemCreateDto);
        assertNotNull(itemShortDto);
        assertNotNull(itemShortDto.getId());
        assertEquals(item.getId(), itemShortDto.getId());
        assertEquals(item.getName(), itemShortDto.getName());
        assertEquals(item.getDescription(), itemShortDto.getDescription());
        assertEquals(item.getAvailable(), itemShortDto.getAvailable());
        assertEquals(owner.getId(), itemShortDto.getSharer().getId());
        assertEquals(owner.getName(), itemShortDto.getSharer().getName());
        assertEquals(owner.getEmail(), itemShortDto.getSharer().getEmail());
        assertEquals(itemRequest.getId(), itemShortDto.getRequest().getId());
        assertEquals(itemRequest.getDescription(), itemShortDto.getRequest().getDescription());
        assertEquals(itemRequest.getCreated(), itemShortDto.getRequest().getCreated());
        assertEquals(booker.getId(), itemShortDto.getRequest().getRequestor().getId());
        assertEquals(booker.getName(), itemShortDto.getRequest().getRequestor().getName());
        assertEquals(booker.getEmail(), itemShortDto.getRequest().getRequestor().getEmail());

    }

    @DisplayName("Вызов исключения NotFoundException при создании вещи")
    @Test
    void createWith404Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(owner.getId(), itemCreateDto));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(owner));
        when(itemRequestRepository.findById(anyLong()))
                .thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> itemService.create(owner.getId(), itemCreateDto));
    }


    @DisplayName("Вызов исключения RuntimeException при создании вещи")
    @Test
    void createWith500Exception() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(owner));
        when(itemRepository.save(any()))
                .thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> itemService.create(owner.getId(), itemCreateDto));
    }

    @DisplayName("Создание комментария")
    @Test
    void addComment() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booker));
        when(bookingRepository.findBondBetweenItemAndUser(anyLong(), anyLong(), any(), any()))
                .thenReturn(true);
        when(commentRepository.save(any()))
                .thenReturn(comment);

        CommentDto commentShortDto = itemService.addComment(item.getId(), booker.getId(),
                commentCreateDto);
        assertNotNull(commentShortDto);
        assertNotNull(commentShortDto.getId());
        assertEquals(comment.getId(), commentShortDto.getId());
        assertEquals(comment.getText(), commentShortDto.getText());
        assertEquals(comment.getAuthor().getName(), commentShortDto.getAuthorName());
        assertEquals(comment.getCreated(), commentShortDto.getCreated());
    }

    @DisplayName("Вызов исключения IllegalOperationException при создании комментария")
    @Test
    void createCommentWith400Exception() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booker));
        when(bookingRepository.findBondBetweenItemAndUser(anyLong(), anyLong(), any(), any()))
                .thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> itemService.addComment(item.getId(), booker.getId(), commentCreateDto));
    }


    @DisplayName("Вызов исключения NotFoundException при создании комментария")
    @Test
    void createCommentWith404Exception() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(item.getId(), booker.getId(), commentCreateDto));

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(item.getId(), booker.getId(), commentCreateDto));
    }


    @DisplayName("Вызов исключения RuntimeException при создании комментария")
    @Test
    void createCommentWith500Exception() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(itemRepository.save(any()))
                .thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class,
                () -> itemService.addComment(item.getId(), booker.getId(), commentCreateDto));

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(item.getId(), booker.getId(), commentCreateDto));
    }

    @DisplayName("Обновление вещи по идентификатору")
    @Test
    void update() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(owner));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(itemRepository.save(any()))
                .thenReturn(item);

        ItemDto itemShortDto = itemService.update(owner.getId(), item.getId(), itemUpdateDto);
        assertNotNull(itemShortDto);
        assertNotNull(itemShortDto.getId());
        assertEquals(item.getId(), itemShortDto.getId());
        assertEquals(item.getName(), itemShortDto.getName());
        assertEquals(item.getDescription(), itemShortDto.getDescription());
        assertEquals(item.getAvailable(), itemShortDto.getAvailable());
        assertEquals(owner.getId(), itemShortDto.getSharer().getId());
        assertEquals(owner.getName(), itemShortDto.getSharer().getName());
        assertEquals(owner.getEmail(), itemShortDto.getSharer().getEmail());
        assertEquals(itemRequest.getId(), itemShortDto.getRequest().getId());
        assertEquals(itemRequest.getDescription(), itemShortDto.getRequest().getDescription());
        assertEquals(itemRequest.getCreated(), itemShortDto.getRequest().getCreated());
        assertEquals(booker.getId(), itemShortDto.getRequest().getRequestor().getId());
        assertEquals(booker.getName(), itemShortDto.getRequest().getRequestor().getName());
        assertEquals(booker.getEmail(), itemShortDto.getRequest().getRequestor().getEmail());
    }

    @DisplayName("Вызов исключения NotFoundException при обновлении вещи по идентификатору")
    @Test
    void updateWith403Exception() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));

        assertThrows(NotFoundException.class,
                () -> itemService.update(booker.getId(), item.getId(), itemUpdateDto));
    }

    @DisplayName("Вызов исключения NotFoundException при обновлении вещи по идентификатору")
    @Test
    void updateWith404Exception() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.update(owner.getId(), item.getId(), itemUpdateDto));
    }

    @DisplayName("Удаление вещи по идентификатору")
    @Test
    void delete() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(owner));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));

        itemService.delete(owner.getId(), item.getId());

        verify(itemRepository).deleteById(item.getId());
    }

    @DisplayName("Вызов исключения IllegalOperationException при удалении вещи по идентификатору")
    @Test
    void deleteWith400Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));

        assertThrows(IllegalOperationException.class,
                () -> itemService.delete(booker.getId(), item.getId()));
    }

    @DisplayName("Вызов исключения NotFoundException при удалении вещи по идентификатору")
    @Test
    void deleteWith404Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.delete(owner.getId(), item.getId()));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(owner));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.delete(owner.getId(), item.getId()));
    }
}
