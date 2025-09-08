package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewUserDto {
    @NotBlank(message = "Имя юзера не должно быть пустым")
    private String name;
    @NotBlank(message = "Имейл юзера должен быть указан")
    @Email(message = "Некорректный имейл")
    private String email;
}
