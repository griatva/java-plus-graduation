package ru.yandex.practicum.interaction.dto.params;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventParamsPublic {

    private String text; // text to search in the annotation content and detailed event description

    private List<Long> categories; // list of category IDs in which the search is performed

    private Boolean paid; // search only paid/free events

    private String rangeStart; // date and time not earlier than which the event should occur

    private String rangeEnd; // date and time not later than which the event should occur

    private boolean onlyAvailable = false; // only events for which the participation request limit has not been reached

    private String sort; // sorting option: by event date or by number of views

    @PositiveOrZero
    private Integer from = PaginationDefaults.DEFAULT_FROM; // number of events to skip // default value: 0

    @Positive
    private Integer size = PaginationDefaults.DEFAULT_SIZE; // number of events in the result set // default value: 10
}