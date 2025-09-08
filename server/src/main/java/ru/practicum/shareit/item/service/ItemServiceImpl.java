package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.util.BookingStatus;
import ru.practicum.shareit.common.exceptions.IllegalOperationException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.NewCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoComplete;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private static final Sort SORT_COMMENT_CREATED_ASC = Sort.by(Sort.Direction.ASC, "created");
    private static final Sort SORT_BOOKING_END_DESC = Sort.by(Sort.Direction.DESC, "end");

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;

    @Override
    public Collection<ItemDtoComplete> getAll(long userId, Integer from, Integer size) {
        log.info("Запрос на получение вещей юзера с id {} передан в сервис", userId);
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Юзер с id " + userId + " не найден");
        }
        PageRequest pageRequest = PageRequest.of(from, size);
        Collection<ItemDtoComplete> items = itemRepository.findAllBySharerId(userId, pageRequest).getContent()
                .stream()
                .map(itemMapper::mapToItemDtoComplete)
                .peek(this::setItemBookings)
                .peek(this::setItemComments)
                .peek(this::setItemsToRequest)
                .toList();
        log.info("Список вещей юзера получен из репозитория и передаётся в контроллер...");
        return items;
    }

    @Override
    public ItemDtoComplete getById(long itemId, long ownerId) {
        log.info("Запрос на получение вещи с id {} передан в сервис", itemId);
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new NotFoundException("Юзер с id " + ownerId + " не найден");
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        ItemDtoComplete itemToReturn = itemMapper.mapToItemDtoComplete(item);
        setItemComments(itemToReturn);
        setItemsToRequest(itemToReturn);
        if (item.getSharer().getId().equals(ownerId)) {
            setItemBookings(itemToReturn);
        }
        log.info("Вещь с id {} получена из репозитория и передаётся в контроллер...", itemId);
        return itemToReturn;
    }

    @Override
    public Collection<ItemDto> search(String text, Integer from, Integer size) {
        log.info("Запрос на поиск доступных вещей по подстроке передан в сервис");
        PageRequest pageRequest = PageRequest.of(from, size);
        Collection<ItemDto> items = itemRepository.search(text, pageRequest).getContent()
                .stream()
                .filter(Item::getAvailable)
                .map(itemMapper::mapToItemDto)
                .toList();
        log.info("Доступные вещи, найденные по вхождению подстроки, передаются в контроллер...");
        return items;
    }

    @Override
    @Transactional
    public ItemDto create(long userId, NewItemDto dto) {
        log.info("Запрос на создание вещи юзером с id {} передан в сервис", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер с id " + userId + " не найден"));
        Item newItem = itemMapper.mapToItem(dto);
        newItem.setSharer(user);
        if (dto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с id " + dto.getRequestId() + " не найден"));
            newItem.setRequest(itemRequest);
        } else {
            log.info("Создаваемая вещь никак не связана с запросом");
        }
        Item itemToReturn = itemRepository.save(newItem);
        log.info("Результат создания вещи получен из репозитория и передаётся в контроллер...");
        return itemMapper.mapToItemDto(itemToReturn);
    }

    @Override
    @Transactional
    public ItemDto update(long userId, long itemId, UpdateItemDto dto) {
        log.info("Запрос на апдейт вещи c id {} юзером с id {} передан в сервис", itemId, userId);
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Юзер с id " + userId + " не найден");
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        if (item.getSharer().getId() != userId) {
            throw new IllegalOperationException("Юзер не является владельцем вещи");
        }
        updateFields(item, dto);
        Item updateItem = itemRepository.save(item);
        log.info("Обновлённая вещь с id {} получена из репозитория и передаётся в контроллер", itemId);
        return itemMapper.mapToItemDto(updateItem);
    }

    @Override
    @Transactional
    public void delete(long userId, long itemId) {
        log.info("Запрос на удаление вещи с id {} юзером с id {} передан в сервис", itemId, userId);
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Юзер с id " + userId + " не найден");
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        if (item.getSharer().getId() != userId) {
            throw new IllegalOperationException("Юзер не является владельцем вещи");
        }
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public CommentDto addComment(long authorId, long itemId, NewCommentDto dto) {
        log.info("Запрос на добавление комментария передан в сервис");
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Юзер с id " + authorId + " не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        log.info("Проверяем, бронировал ли юзер ранее вещь, к которой собирается написать отзыв");
        boolean isBooker = bookingRepository.findBondBetweenItemAndUser(itemId, authorId, LocalDateTime.now(),
                BookingStatus.APPROVED);
        if (!isBooker) {
            String errorMessage = String.format("Юзеру с id %d нельзя писать отзыв о вещи с id %d", authorId, itemId);
            throw new IllegalOperationException(errorMessage);
        }
        log.info("Юзер ранее бронировал вещь, к которой собирается оставить отзыв");
        Comment comment = commentMapper.mapToComment(dto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment = commentRepository.save(comment);
        log.info("Сохранили новый отзыв в БД");
        CommentDto toReturn = commentMapper.mapToCommentDto(comment);
        log.info("Возвращаем данные о созданном отзыве в контроллер");
        return toReturn;
    }

    private void updateFields(Item item, UpdateItemDto dto) {
        if (dto.hasName() && !dto.getName().equals(item.getName())) {
            item.setName(dto.getName());
        }
        if (dto.hasDescription() && !dto.getDescription().equals(item.getDescription())) {
            item.setDescription(dto.getDescription());
        }
        if (dto.hasAvailable()) {
            item.setAvailable(dto.getAvailable());
        }
    }

    private void setItemBookings(ItemDtoComplete item) {
        Booking lastBooking = bookingRepository.findFirstBookingByItemIdAndEndIsBeforeAndStatus(item.getId(),
                LocalDateTime.now(), BookingStatus.APPROVED, SORT_BOOKING_END_DESC);
        Booking nextBooking = bookingRepository.findFirstBookingByItemIdAndEndIsAfterAndStatus(item.getId(),
                LocalDateTime.now(), BookingStatus.APPROVED, SORT_BOOKING_END_DESC);
        if (lastBooking != null) {
            item.setLastBooking(bookingMapper.mapToBookingDtoShort(lastBooking));
            item.getLastBooking().setBookerId(lastBooking.getBooker().getId());
        }
        if (nextBooking != null) {
            item.setNextBooking(bookingMapper.mapToBookingDtoShort(nextBooking));
            item.getNextBooking().setBookerId(nextBooking.getBooker().getId());
        }
    }

    private void setItemComments(ItemDtoComplete item) {
        Collection<CommentDto> comments = commentRepository.findAllByItemId(item.getId(), SORT_COMMENT_CREATED_ASC)
                .stream()
                .map(commentMapper::mapToCommentDto)
                .toList();
        item.setComments(comments);
    }

    private void setItemsToRequest(ItemDtoComplete item) {
        if (item.getRequest() != null) {
            Collection<ItemDto> items = itemRepository.findByRequestIdIn(List.of(item.getRequest().getId()))
                    .stream()
                    .map(itemMapper::mapToItemDto)
                    .toList();
            item.getRequest().setItems(items);
        }
    }
}
