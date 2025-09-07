package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoShort;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private static final Sort SORT_CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    public Collection<ItemRequestDtoShort> getAll(int from, int size) {
        log.info("Запрос на вывод списка запросов, созданных другими юзерами, передан в сервис");
        PageRequest pageRequest = PageRequest.of(from, size, SORT_CREATED_DESC);
        Collection<ItemRequest> result = itemRequestRepository.findAll(pageRequest).getContent();
        log.info("Из БД успешно получен список запросов размером {}", result.size());
        Collection<ItemRequestDtoShort> requests = result
                .stream()
                .map(itemRequestMapper::mapToItemRequestDtoShort)
                .toList();
        log.info("Список запросов преобразован в dto-объекты и передаётся в контроллер...");
        return requests;
    }

    @Override
    public Collection<ItemRequestDto> getAllByRequestorId(long requestorId, int from, int size) {
        log.info("Запрос на вывод списка запросов юзера передан в сервис");
        if (userRepository.findById(requestorId).isEmpty()) {
            throw new NotFoundException("Юзер с id " + requestorId + " не найден");
        }
        PageRequest pageRequest = PageRequest.of(from, size, SORT_CREATED_DESC);
        Collection<ItemRequest> result = itemRequestRepository.findByRequestorId(requestorId, pageRequest).getContent();
        log.info("Из БД успешно получен список запросов юзера с id {} размером {}", requestorId, result.size());
        Collection<ItemRequestDto> requests = result
                .stream()
                .map(itemRequestMapper::mapToItemRequestDto)
                .toList();
        completeWithItems(requests);
        log.info("Список запросов юзера преобразован в dto-объекты и передаётся в контроллер...");
        return requests;
    }

    @Override
    public ItemRequestDto getById(long requestId) {
        log.info("Запрос на получение данных о запросе передан в сервис");
        ItemRequest result = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));
        log.info("Из БД успешно получен запрос с id {}", requestId);
        ItemRequestDto request = itemRequestMapper.mapToItemRequestDto(result);
        completeWithItems(List.of(request));
        log.info("Запрос с id {} преобразован в dto-объект и передаётся в контроллер...", requestId);
        return request;
    }

    @Override
    public ItemRequestDtoShort create(long requestorId, NewItemRequestDto dto) {
        log.info("Запрос на создание запроса передан в сервис");
        User requestor = userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Юзер с id " + requestorId + " не найден"));
        log.info("Юзер с id {} успешно найден в БД", requestorId);
        ItemRequest request = itemRequestMapper.mapToItemRequest(dto);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        request = itemRequestRepository.save(request);
        log.info("Новый запрос был успешно сохранён в БД с id {}", request.getId());
        ItemRequestDtoShort toReturn = itemRequestMapper.mapToItemRequestDtoShort(request);
        log.info("Новый запрос преобразован в dto-объект и передаётся в контроллер...");
        return toReturn;
    }

    private void completeWithItems(Collection<ItemRequestDto> rqs) {
        Collection<Item> items = itemRepository.findByRequestIdIn(rqs.stream().map(ItemRequestDto::getId).toList());
        if (!items.isEmpty()) {
            for (ItemRequestDto dto : rqs) {
                Collection<ItemDto> requestItems = items
                        .stream()
                        .filter(i -> i.getRequest().getId().equals(dto.getId()))
                        .map(itemMapper::mapToItemDto)
                        .toList();
                dto.setItems(requestItems);
            }
        }
    }
}
