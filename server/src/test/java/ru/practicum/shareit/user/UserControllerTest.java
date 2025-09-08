package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.exceptions.DuplicateEmailException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Обработка REST-запросов к /users")
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    private UserDto userDto;
    private NewUserDto userCreateDto;
    private UpdateUserDto userUpdateDto;

    @BeforeEach
    void setUp() {
        Random random = new Random();

        userDto = UserDto.builder()
                .id(Math.abs(random.nextLong()))
                .name("First User")
                .email("first@system.com")
                .build();

        userCreateDto = NewUserDto.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();

        userUpdateDto = UpdateUserDto.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    @AfterEach
    void tearDown() {
        userDto = null;
        userCreateDto = null;
    }

    @DisplayName("Получение списка юзеров")
    @Test
    void getAll() throws Exception {
        when(userService.getAll(anyInt(), anyInt()))
                .thenReturn(List.of(userDto));

        mockMvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", "0")
                        .param("size", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())));
    }

    @DisplayName("Вызов исключения RuntimeException при получении списка юзеров")
    @Test
    void getAllWith403Exception() throws Exception {
        when(userService.getAll(anyInt(), anyInt()))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", "0")
                        .param("size", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("Поиск юзера по id")
    @Test
    void getById() throws Exception {
        when(userService.getById(anyLong()))
                .thenReturn(userDto);

        mockMvc.perform(get("/users/" + userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @DisplayName("Вызов исключения NotFoundException при поиске юзера по id")
    @Test
    void getByIdWith404Exception() throws Exception {
        when(userService.getById(anyLong()))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(get("/users/" + userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isNotFound());

    }

    @DisplayName("Создание юзера")
    @Test
    void create() throws Exception {
        when(userService.create(any()))
                .thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.name", is(userDto.getName())));
    }

    @DisplayName("Вызов исключения DuplicateEmailException при создании юзера")
    @Test
    void createWith409Exception() throws Exception {
        when(userService.create(any()))
                .thenThrow(DuplicateEmailException.class);

        mockMvc.perform(post("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isConflict());
    }

    @DisplayName("Вызов исключения RuntimeException при создании юзера")
    @Test
    void createWith500Exception() throws Exception {
        when(userService.getById(anyLong()))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(post("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("Обновление юзера по его id")
    @Test
    void updateUser() throws Exception {
        when(userService.update(anyLong(), any()))
                .thenReturn(userDto);

        mockMvc.perform(patch("/users/" + userDto.getId())
                        .content(objectMapper.writeValueAsString(userUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }


    @DisplayName("Вызов исключения NotFoundException при обновлении юзера по его id")
    @Test
    void updateUserWith404Exception() throws Exception {
        when(userService.update(anyLong(), any()))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(patch("/users/" + userDto.getId())
                        .content(objectMapper.writeValueAsString(userUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isNotFound());
    }


    @DisplayName("Вызов исключения ValueAlreadyUsedException при обновлении юзера по его id")
    @Test
    void updateUserWith409Exception() throws Exception {
        when(userService.update(anyLong(), any()))
                .thenThrow(DuplicateEmailException.class);

        mockMvc.perform(patch("/users/" + userDto.getId())
                        .content(objectMapper.writeValueAsString(userUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isConflict());
    }

    @DisplayName("Удаление юзера по id")
    @Test
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/users/" + userDto.getId()))
                .andExpect(status().isOk());
        verify(userService).delete(userDto.getId());
    }
}
