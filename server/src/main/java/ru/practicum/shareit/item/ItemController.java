package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
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
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.NewCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoComplete;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<Collection<ItemDtoComplete>> getAll(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Контроллер получил http-запрос на вывод вещей юзера с id {}", userId);
        Collection<ItemDtoComplete> userItems = itemService.getAll(userId, from, size);
        log.info("Возвращаем список вещей юзера с id {} в http-ответе", userId);
        return new ResponseEntity<>(userItems, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<ItemDto>> search(
            @RequestParam(name = "text") String text,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Контроллер получил http-запрос на поиск доступных вещей по подстроке");
        log.info("Поисковая подстрока: {}", text);
        Collection<ItemDto> items = itemService.search(text, from, size);
        log.info("Возвращаем доступные вещи, содержащие подстроку {}, в http-ответе", text);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDtoComplete> getById(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                   @PathVariable(name = "id") long itemId) {
        log.info("Контроллер получил http-запрос на вывод вещи с id {}", itemId);
        ItemDtoComplete item = itemService.getById(itemId, ownerId);
        log.info("Возвращаем вещь с id {} в http-ответе", itemId);
        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ItemDto> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @RequestBody NewItemDto dto) {
        log.info("Контроллер получил http-запрос на добавление вещи юзером с id {}", userId);
        ItemDto newItem = itemService.create(userId, dto);
        log.info("Юзер с id {} успешно добавил вещь с id {} в БД", userId, newItem.getId());
        return new ResponseEntity<>(newItem, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> update(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable(name = "id") long itemId,
            @RequestBody UpdateItemDto dto) {
        log.info("Контроллер получил http-запрос на апдейт вещи с id {} юзером с id {}", itemId, userId);
        ItemDto updateItem = itemService.update(userId, itemId, dto);
        log.info("Юзер с id {} успешно обновил вещь с id {} в БД", userId, itemId);
        return new ResponseEntity<>(updateItem, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @PathVariable(name = "id") long itemId) {
        log.info("Контроллер получил http-запрос на удаление вещи с id {} юзером с id {}", itemId, userId);
        itemService.delete(userId, itemId);
        log.info("Юзер с id {} успешно удалил вещь с id {} из БД", userId, itemId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<CommentDto> addComment(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable(name = "id") long itemId,
            @RequestBody NewCommentDto dto) {
        log.info("Контроллер получил http-запрос на добавление комментария");
        log.info("Юзер с id {} хочет оставить комментарий к вещи с id {}", userId, itemId);
        CommentDto newComment = itemService.addComment(userId, itemId, dto);
        log.info("Юзер с id {} успешно оставил комментарий к вещи с id {}", userId, itemId);
        return new ResponseEntity<>(newComment, HttpStatus.OK);
    }
}
