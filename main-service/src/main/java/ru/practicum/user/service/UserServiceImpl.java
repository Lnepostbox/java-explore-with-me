package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User getById(long userId) {
        log.info("UserService: getById. UserId {}.", userId);
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("UserId {} doesn't exist.", userId));
    }

    @Override
    public List<UserDto> getAll(List<Long> usersId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (usersId == null || usersId.isEmpty()) {
            log.info("UserService: getAll for null or empty.");

            return userRepository.findAll(pageable).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            log.info("UserService: getAll.");

            return userRepository.findAllById(usersId).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = userRepository.save(UserMapper.fromUserDto(userDto));
        log.info("UserService: create. User {}.", user);

        return UserMapper.toUserDto(user);
    }

    @Override
    public void delete(long userId) {
        getById(userId);
        userRepository.deleteById(userId);
        log.info("UserService: delete. UserId {}.", userId);
    }

}
