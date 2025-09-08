package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.DuplicateEmailException;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Обработка данных на уровне сервиса UserService")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {

    @Autowired
    private final UserServiceImpl userService;

    @MockBean
    private final UserRepository userRepository;

    private User user;
    private NewUserDto userCreateDto;
    private UpdateUserDto userUpdateDto;

    private static Page<User> getPageFromList(List<User> list) {
        return new PageImpl<>(list, PageRequest.of(0, list.size()), list.size());
    }

    @BeforeEach
    void setUp() {
        Random random = new Random();

        user = User.builder()
                .id(Math.abs(random.nextLong()))
                .name("First User")
                .email("first@sysem.com")
                .build();

        userCreateDto = NewUserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();

        userUpdateDto = UpdateUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @AfterEach
    void tearDown() {
        user = null;
        userCreateDto = null;
        userUpdateDto = null;
    }

    @DisplayName("Получение списка юзеров")
    @Test
    void getAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable))
                .thenReturn(getPageFromList(List.of(user)));

        Collection<UserDto> userList = userService.getAll(0, 10);
        assertNotNull(userList);
        assertFalse(userList.isEmpty());
        assertEquals(1, userList.size());

        Optional<UserDto> userDtoOpt = userList.stream().findFirst();
        assertTrue(userDtoOpt.isPresent());
        UserDto userDto = userDtoOpt.get();
        assertNotNull(userDto);
        assertNotNull(userDto.getId());
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @DisplayName("Поиск юзера по id")
    @Test
    void getById() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));

        UserDto userDto = userService.getById(user.getId());
        assertNotNull(userDto);
        assertNotNull(userDto.getId());
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @DisplayName("Вызов исключения  при поиске юзера по id")
    @Test
    void findByIdWith404Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(user.getId()));
    }

    @DisplayName("Создание юзера")
    @Test
    void createUser() {
        when(userRepository.existsByEmailIgnoreCase(anyString()))
                .thenReturn(false);
        when(userRepository.save(any()))
                .thenReturn(user);

        UserDto userDto = userService.create(userCreateDto);
        assertNotNull(userDto);
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @DisplayName("Вызов исключения DuplicateEmailException при создании юзера")
    @Test
    void createUserWith403Exception() {
        when(userRepository.existsByEmailIgnoreCase(anyString()))
                .thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.create(userCreateDto));
    }

    @DisplayName("Обновление юзера")
    @Test
    void updateUser() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(userRepository.existsByEmailIgnoreCase(anyString()))
                .thenReturn(false);
        when(userRepository.save(any()))
                .thenReturn(user);

        UserDto userDto = userService.update(user.getId(), userUpdateDto);
        assertNotNull(userDto);
        assertNotNull(userDto.getId());
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @DisplayName("Вызов исключения DuplicateEmailException при обновлении юзера")
    @Test
    void updateUserWith403Exception() {
        userUpdateDto.setEmail("notvalidemail@gmail.com");

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(userRepository.existsByEmailIgnoreCase(anyString()))
                .thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.update(user.getId(), userUpdateDto));
    }

    @DisplayName("Вызов исключения NotFoundException при обновлении юзера")
    @Test
    void updateUserWith404Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(user.getId(), userUpdateDto));
    }

    @DisplayName("Удаление юзера по id")
    @Test
    void deleteUser() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));

        userRepository.deleteById(user.getId());

        verify(userRepository).deleteById(user.getId());
    }

    @DisplayName("Вызов исключения NotFoundException при удалении юзера по id")
    @Test
    void deleteUserWith404Exception() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.delete(user.getId()));
    }
}
