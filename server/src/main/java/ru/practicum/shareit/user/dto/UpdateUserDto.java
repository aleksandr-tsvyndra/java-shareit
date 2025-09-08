package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {
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
