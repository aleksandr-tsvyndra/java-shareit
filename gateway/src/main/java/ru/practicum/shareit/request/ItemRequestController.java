package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Клиент отправил запрос на получение списка запросов, созданных другими юзерами");
        return itemRequestClient.getAll(from, size);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByRequestorId(
            @Positive @RequestHeader("X-Sharer-User-Id") long requestorId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
            @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Клиент отправил запрос на получение списка запросов юзера с id {}", requestorId);
        return itemRequestClient.getAllByRequestorId(requestorId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@Positive @PathVariable(name = "requestId") long requestId) {
        log.info("Клиент отправил запрос на получение данных о запросе с id {}", requestId);
        return itemRequestClient.getById(requestId);
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @Positive @RequestHeader("X-Sharer-User-Id") long requestorId,
            @Valid @RequestBody NewItemRequestDto dto) {
        log.info("Клиент отправил запрос на создание запроса юзером с id {}", requestorId);
        return itemRequestClient.create(requestorId, dto);
    }
}
