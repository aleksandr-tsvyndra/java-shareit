package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {

    Collection<UserDto> getAll();

    UserDto getById(long userId);

    UserDto create(NewUserDto dto);

    UserDto update(long userId, UpdateUserDto dto);

    void delete(long userId);

}
