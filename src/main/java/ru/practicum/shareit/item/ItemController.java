package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public Collection<ItemDto> getAll(@RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        log.info("Контроллер получил http-запрос на вывод вещей юзера с id {}", userId);
        Collection<ItemDto> userItems = itemService.getAll(userId);
        log.info("Возвращаем список вещей юзера с id {} в http-ответе", userId);
        return userItems;
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam(name = "text") String text) {
        log.info("Контроллер получил http-запрос на поиск доступных вещей по подстроке");
        log.info("Поисковая подстрока: {}", text);
        Collection<ItemDto> items = itemService.search(text);
        log.info("Возвращаем доступные вещи, содержащие подстроку {}, в http-ответе", text);
        return items;
    }

    @GetMapping("/{id}")
    public ItemDto getById(@PathVariable(name = "id") @Positive long itemId) {
        log.info("Контроллер получил http-запрос на вывод вещи с id {}", itemId);
        ItemDto item = itemService.getById(itemId);
        log.info("Возвращаем вещь с id {} в http-ответе", itemId);
        return item;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                          @Valid @RequestBody NewItemDto dto) {
        log.info("Контроллер получил http-запрос на добавление вещи юзером с id {}", userId);
        ItemDto newItem = itemService.create(userId, dto);
        log.info("Юзер с id {} успешно добавил вещь с id {} в БД", userId, newItem.getId());
        return newItem;
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                          @PathVariable(name = "id") @Positive long itemId,
                          @RequestBody UpdateItemDto dto) {
        log.info("Контроллер получил http-запрос на апдейт вещи с id {} юзером с id {}", itemId, userId);
        ItemDto updateItem = itemService.update(userId, itemId, dto);
        log.info("Юзер с id {} успешно обновил вещь с id {} в БД", userId, itemId);
        return updateItem;
    }

    @DeleteMapping("/{id}")
    public void delete(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                       @PathVariable(name = "id") @Positive long itemId) {
        log.info("Контроллер получил http-запрос на удаление вещи с id {} юзером с id {}", itemId, userId);
        itemService.delete(userId, itemId);
        log.info("Юзер с id {} успешно удалил вещь с id {} из БД", userId, itemId);
    }
}
