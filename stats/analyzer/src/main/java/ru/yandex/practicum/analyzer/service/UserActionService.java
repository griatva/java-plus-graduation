package ru.yandex.practicum.analyzer.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionService {

    void handleUserAction(UserActionAvro message);

}

