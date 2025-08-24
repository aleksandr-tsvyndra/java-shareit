package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> getAll() {
        log.info("Контроллер получил http-запрос на вывод всех юзеров");
        Collection<UserDto> users = userService.getAll();
        log.info("Возвращаем список юзеров в http-ответе");
        return users;
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable(name = "id") @Positive long userId) {
        log.info("Контроллер получил http-запрос на вывод юзера с id {}", userId);
        UserDto user = userService.getById(userId);
        log.info("Возвращаем юзера с id {} в http-ответе", userId);
        return user;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody NewUserDto dto) {
        log.info("Контроллер получил http-запрос на создание юзера");
        UserDto newUser = userService.create(dto);
        log.info("Новый юзер с id {} был добавлен в БД", newUser.getId());
        return newUser;
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable(name = "id") @Positive long userId,
                          @RequestBody UpdateUserDto dto) {
        log.info("Контроллер получил http-запрос на апдейт юзера с id {}", userId);
        UserDto updateUser = userService.update(userId, dto);
        log.info("Юзер с id {} был успешно обновлён", userId);
        return updateUser;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable(name = "id") @Positive long userId) {
        log.info("Контроллер получил http-запрос на удаление юзера с id {}", userId);
        userService.delete(userId);
        log.info("Юзер с id {} был успешно удалён из БД", userId);
    }
}
