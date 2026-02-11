package ru.yandex.practicum.interaction.dto.params;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentSearchParamsAdmin {

    private Long authorId;       // filter by author
    private Long eventId;        // filter by event
    private String rangeStart;   // filter by start date
    private String rangeEnd;     // filter by end date

    @PositiveOrZero
    private Integer from = PaginationDefaults.DEFAULT_FROM;

    @Positive
    private Integer size = PaginationDefaults.DEFAULT_SIZE;
}