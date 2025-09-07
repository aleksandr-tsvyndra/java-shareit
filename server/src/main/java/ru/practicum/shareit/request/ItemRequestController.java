package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoShort;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @GetMapping("/all")
    public ResponseEntity<Collection<ItemRequestDtoShort>> getAll(
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Контроллер получил http-запрос на вывод списка запросов, созданных другими юзерами");
        Collection<ItemRequestDtoShort> requests = itemRequestService.getAll(from, size);
        log.info("Возвращаем список запросов размером {} в http-ответе", requests.size());
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Collection<ItemRequestDto>> getAllByRequestorId(
            @RequestHeader("X-Sharer-User-Id") long requestorId,
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Контроллер получил http-запрос на вывод списка запросов юзера с id {}", requestorId);
        Collection<ItemRequestDto> requests = itemRequestService.getAllByRequestorId(requestorId, from, size);
        log.info("Возвращаем список запросов юзера с id {} размером {} в http-ответе", requestorId, requests.size());
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getById(@PathVariable(name = "requestId") long id) {
        log.info("Контроллер получил http-запрос на вывод данных о запросе с id {}", id);
        ItemRequestDto request = itemRequestService.getById(id);
        log.info("Возвращаем данные о запросе с id {} в http-ответе", id);
        return new ResponseEntity<>(request, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ItemRequestDtoShort> create(@RequestHeader("X-Sharer-User-Id") long requestorId,
                                 @RequestBody NewItemRequestDto dto) {
        log.info("Контроллер получил http-запрос на создание запроса юзером с id {}", requestorId);
        ItemRequestDtoShort newRequest = itemRequestService.create(requestorId, dto);
        log.info("Возвращаем данные о созданном запросе, которому присвоен id {}, в http-ответе", newRequest.getId());
        return new ResponseEntity<>(newRequest, HttpStatus.CREATED);
    }
}
