package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Collection<UserDto>> getAll(
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Контроллер получил http-запрос на вывод всех юзеров");
        Collection<UserDto> users = userService.getAll(from, size);
        log.info("Возвращаем список юзеров в http-ответе");
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable(name = "id") long userId) {
        log.info("Контроллер получил http-запрос на вывод юзера с id {}", userId);
        UserDto user = userService.getById(userId);
        log.info("Возвращаем юзера с id {} в http-ответе", userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody NewUserDto dto) {
        log.info("Контроллер получил http-запрос на создание юзера");
        UserDto newUser = userService.create(dto);
        log.info("Новый юзер с id {} был добавлен в БД", newUser.getId());
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> update(
            @PathVariable(name = "id") long userId,
            @RequestBody UpdateUserDto dto) {
        log.info("Контроллер получил http-запрос на апдейт юзера с id {}", userId);
        UserDto updateUser = userService.update(userId, dto);
        log.info("Юзер с id {} был успешно обновлён", userId);
        return new ResponseEntity<>(updateUser, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") long userId) {
        log.info("Контроллер получил http-запрос на удаление юзера с id {}", userId);
        userService.delete(userId);
        log.info("Юзер с id {} был успешно удалён из БД", userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
