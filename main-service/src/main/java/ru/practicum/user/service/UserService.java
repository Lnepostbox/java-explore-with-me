package ru.practicum.user.service;

import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;
import java.util.List;

public interface UserService {

    List<UserDto> getAll(List<Long> usersId, int from, int size);

    User getById(long userId);

    UserDto create(UserDto userDto);

    void delete(long userId);

}
