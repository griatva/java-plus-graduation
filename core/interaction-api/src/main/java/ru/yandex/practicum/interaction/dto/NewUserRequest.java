package ru.yandex.practicum.interaction.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @Size(min = 2, max = 250, message = "User name must be between 2 and 250 characters long!")
    @NotBlank(message = "User name must not be blank!")
    private String name;

    @Email(message = "Email must be a valid email address!")
    @Size(min = 6, max = 254, message = "Email must be between 6 and 254 characters long!")
    @NotBlank(message = "Email must not be blank!")
    private String email;
}