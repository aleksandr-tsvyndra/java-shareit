package ru.practicum.shareit.user.dto;

import lombok.Data;

@Data
public class UpdateUserDto {
    private String name;
    private String email;

    public boolean hasName() {
        return ! (name == null || name.trim().isBlank());
    }

    public boolean hasEmail() {
        return ! (email == null || email.trim().isBlank());
    }
}
