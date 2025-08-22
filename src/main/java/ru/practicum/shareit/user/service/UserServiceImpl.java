package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exceptions.DuplicateEmailException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserMapper userMapper;

    @Override
    public Collection<UserDto> getAll() {
        log.info("Запрос на получение всех юзеров передан в сервис");
        Collection<UserDto> users = userRepository.findAll()
                .stream()
                .map(userMapper::mapToUserDto)
                .toList();
        log.info("Список юзеров получен из репозитория и передаётся в контроллер");
        return users;
    }

    @Override
    public UserDto getById(long userId) {
        log.info("Запрос на получение юзера с id {} передан в сервис", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер с id " + userId + " не найден"));
        UserDto userToReturn = userMapper.mapToUserDto(user);
        log.info("Юзер с id {} получен из репозитория и передаётся в контроллер", userId);
        return userToReturn;
    }

    @Override
    public UserDto create(NewUserDto dto) {
        log.info("Запрос на создание нового юзера передан в сервис");
        checkEmail(dto.getEmail());
        log.info("Имейл нового юзера уникален");
        User user = userRepository.create(userMapper.mapToUser(dto));
        log.info("Созданный юзер с id {} получен из репозитория и передаётся в контроллер", user.getId());
        return userMapper.mapToUserDto(user);
    }

    @Override
    public UserDto update(long userId, UpdateUserDto dto) {
        log.info("Запрос на апдейт юзера с id {} передан в сервис", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер с id " + userId + " не найден"));
        updateFields(user, dto);
        User updateUser = userRepository.update(user);
        log.info("Обновлённый юзер с id {} получен из репозитория и передаётся в контроллер", userId);
        return userMapper.mapToUserDto(updateUser);
    }

    @Override
    public void delete(long userId) {
        log.info("Запрос на удаление юзера с id {} передан в сервис", userId);
        getById(userId);
        log.info("Перед удалением юзера получаем и удаляем все его вещи из БД");
        itemRepository.findAll()
                .stream()
                .filter(i -> i.getOwner().getId().equals(userId))
                .forEach(i -> itemRepository.delete(i.getId()));
        userRepository.delete(userId);
    }

    private void checkEmail(String email) {
        log.info("Проверяем имейл при создании юзера на дубликат");
        boolean hasDuplicateEmail = getAll()
                .stream()
                .map(UserDto::getEmail)
                .anyMatch(email::equals);
        if (hasDuplicateEmail) {
            var message = String.format("Имейл %s уже занят другим юзером", email);
            log.warn(message);
            throw new DuplicateEmailException(message);
        }
    }

    private void updateFields(User user, UpdateUserDto dto) {
        if (dto.hasEmail() && dto.getEmail().matches(".*")) {
            if (!dto.getEmail().equals(user.getEmail())) {
                checkEmail(dto.getEmail());
                user.setEmail(dto.getEmail());
                log.info("Имейл юзера с id {} был успешно обновлен", user.getId());
            }
        }
        if (dto.hasName() && !dto.getName().equals(user.getName())) {
            user.setName(dto.getName());
            log.info("Имя юзера с id {} было успешно обновлено", user.getId());
        }
    }
}
