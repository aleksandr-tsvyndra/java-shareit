package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.common.BaseRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class UserRepositoryImpl extends BaseRepository<User> implements UserRepository {

    @Override
    public Collection<User> findAll() {
        log.info("Поиск всех юзеров в репозитории...");
        Collection<User> users = findMany();
        log.info("Поиск всех юзеров в БД окончен");
        return users;
    }

    @Override
    public Optional<User> findById(long userId) {
        log.info("Поиск юзера в репозитории по следующему id: {}...", userId);
        Optional<User> user = findOne(userId);
        if (user.isPresent()) {
            log.info("Юзер с id {} был успешно найден в БД", userId);
        } else {
            log.info("Юзер с id {} не был найден в БД", userId);
        }
        return user;
    }

    @Override
    public User create(User newUser) {
        log.info("Сохранение в БД нового юзера...");
        newUser.setId(idGenerator());
        User user = insert(newUser.getId(), newUser);
        log.info("Юзер с id {} был успешно сохранён в БД", user.getId());
        return user;
    }

    @Override
    public User update(User updateUser) {
        log.info("Обновление в БД юзера с id {}...", updateUser.getId());
        User user = insert(updateUser.getId(), updateUser);
        log.info("Юзер с id {} был успешно обновлен в БД", user.getId());
        return user;
    }

    @Override
    public void delete(long userId) {
        log.info("Удаление юзера из БД с id {}...", userId);
        super.delete(userId);
        log.info("Юзер с id {} был успешно удален из БД", userId);
    }
}
