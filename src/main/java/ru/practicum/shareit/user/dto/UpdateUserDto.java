package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateUserDto {
    @NotNull(message = "Id юзера должно быть указано")
    @Positive(message = "Id должно быть положительным числом")
    private Long id;
    private String name;
    private String email;

    public boolean hasName() {
        return ! (name == null || name.trim().isBlank());
    }

    public boolean hasEmail() {
        return ! (email == null || email.trim().isBlank());
    }
}
