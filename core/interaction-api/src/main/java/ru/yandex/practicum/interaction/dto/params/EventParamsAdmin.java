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
public class EventParamsAdmin {

    private List<Long> users; // list of user IDs whose events should be found

    private List<String> states; // list of states in which the searched events are

    private List<Long> categories; // list of category IDs in which the search is performed

    private String rangeStart; // date and time not earlier than which the event should occur

    private String rangeEnd; // date and time not later than which the event should occur

    @PositiveOrZero
    private Integer from = PaginationDefaults.DEFAULT_FROM; // number of events to skip // default value: 0

    @Positive
    private Integer size = PaginationDefaults.DEFAULT_SIZE; // number of events in the result set // default value: 10
}