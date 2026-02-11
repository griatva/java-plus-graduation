package ru.yandex.practicum.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {

    @Size(min = 1, max = 50, message = "Category name must be between 1 and 50 characters long!")
    @NotBlank(message = "Category name must not be blank!")
    private String name;
}