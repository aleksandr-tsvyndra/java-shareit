package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exceptions.IllegalOperationException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public Collection<ItemDto> getAll(long userId) {
        log.info("Запрос на получение вещей юзера с id {} передан в сервис", userId);
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Юзер с id " + userId + " не найден");
        }
        Collection<ItemDto> items = itemRepository.findAll()
                .stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(itemMapper::mapToItemDto)
                .toList();
        log.info("Список вещей юзера получен из репозитория и передаётся в контроллер...");
        return items;
    }

    @Override
    public ItemDto getById(long itemId) {
        log.info("Запрос на получение вещи с id {} передан в сервис", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        ItemDto itemToReturn = itemMapper.mapToItemDto(item);
        log.info("Вещь с id {} получена из репозитория и передаётся в контроллер...", itemId);
        return itemToReturn;
    }

    @Override
    public Collection<ItemDto> search(String text) {
        log.info("Запрос на поиск доступных вещей по подстроке передан в сервис");
        if (text == null || text.trim().isBlank()) {
            log.info("Передано пустое значение подстроки. Возвращаем пустую коллекцию на уровень контроллера");
            return List.of();
        }
        Collection<ItemDto> items = itemRepository.findAll()
                .stream()
                .filter(Item::getAvailable)
                .filter(i -> i.getName().toLowerCase().contains(text.toLowerCase())
                        || i.getDescription().toLowerCase().contains(text.toLowerCase()))
                .map(itemMapper::mapToItemDto)
                .toList();
        log.info("Доступные вещи, найденные по вхождению подстроки, передаются в контроллер...");
        return items;
    }

    @Override
    public ItemDto create(long userId, NewItemDto dto) {
        log.info("Запрос на создание вещи юзером с id {} передан в сервис", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер с id " + userId + " не найден"));
        Item newItem = itemMapper.mapToItem(dto);
        newItem.setOwner(user);
        Item itemToReturn = itemRepository.create(newItem);
        log.info("Результат создания вещи получен из репозитория и передаётся в контроллер...");
        return itemMapper.mapToItemDto(itemToReturn);
    }

    @Override
    public ItemDto update(long userId, long itemId, UpdateItemDto dto) {
        log.info("Запрос на апдейт вещи c id {} юзером с id {} передан в сервис", itemId, userId);
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Юзер с id " + userId + " не найден");
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        if (item.getOwner().getId() != userId) {
            throw new IllegalOperationException("Юзер не является владельцем вещи");
        }
        updateFields(item, dto);
        Item updateItem = itemRepository.update(item);
        log.info("Обновлённая вещь с id {} получена из репозитория и передаётся в контроллер", itemId);
        return itemMapper.mapToItemDto(updateItem);
    }

    @Override
    public void delete(long userId, long itemId) {
        log.info("Запрос на удаление вещи с id {} юзером с id {} передан в сервис", itemId, userId);
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Юзер с id " + userId + " не найден");
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        if (item.getOwner().getId() != userId) {
            throw new IllegalOperationException("Юзер не является владельцем вещи");
        }
        itemRepository.delete(itemId);
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
}
