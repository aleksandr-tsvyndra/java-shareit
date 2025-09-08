package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getAll(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Клиент отправил запрос на вывод вещей юзера с id {}", userId);
        return itemClient.getAll(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestParam(name = "text") String text,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Клиент отправил запрос на поиск доступных вещей по подстроке");
        if (text == null || text.trim().isBlank()) {
            log.info("Передано пустое значение подстроки. Возвращаем пустую коллекцию в http-ответе");
            return new ResponseEntity<>(List.of(), HttpStatus.OK);
        }
        return itemClient.search(text, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") @Positive long ownerId,
                                          @PathVariable(name = "id") @Positive long itemId) {
        log.info("Клиент отправил запрос, что юзер с id {} хочет получить данные о вещи с id {}", ownerId, itemId);
        return itemClient.getById(itemId, ownerId);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                         @Valid @RequestBody NewItemDto dto) {
        log.info("Со стороны клиента отправлен запрос на добавление вещи юзером с id {}", userId);
        return itemClient.create(userId, dto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                         @PathVariable(name = "id") @Positive long itemId,
                                         @RequestBody UpdateItemDto dto) {
        log.info("Со стороны клиента отправлен запрос на апдейт вещи с id {} юзером с id {}", itemId, userId);
        return itemClient.update(userId, itemId, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                         @PathVariable(name = "id") @Positive long itemId) {
        log.info("Клиент отправил запрос на удаление вещи с id {} юзером с id {}", itemId, userId);
        return itemClient.delete(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader("X-Sharer-User-Id") @Positive long authorId,
            @PathVariable(name = "itemId") @Positive long itemId,
            @Valid @RequestBody NewCommentDto dto) {
        log.info("Со стороны клиента отправлен запрос на добавление комментария");
        return itemClient.addComment(authorId, itemId, dto);
    }
}
