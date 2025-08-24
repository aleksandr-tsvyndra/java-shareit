package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.common.BaseRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class ItemRepositoryImpl extends BaseRepository<Item> implements ItemRepository {

    @Override
    public Collection<Item> findAll() {
        log.info("Поиск всех вещей в репозитории...");
        Collection<Item> items = findMany();
        log.info("Поиск всех вещей в БД окончен");
        return items;
    }

    @Override
    public Optional<Item> findById(long itemId) {
        log.info("Поиск вещи в репозитории по следующему id: {}...", itemId);
        Optional<Item> item = findOne(itemId);
        if (item.isPresent()) {
            log.info("Вещь с id {} была успешно найдена в БД", itemId);
        } else {
            log.info("Вещь с id {} не была найдена в БД", itemId);
        }
        return item;
    }

    @Override
    public Item create(Item newItem) {
        log.info("Сохранение в БД новой вещи...");
        newItem.setId(idGenerator());
        Item item = insert(newItem.getId(), newItem);
        log.info("Вещь с id {} была успешно сохранена в БД", item.getId());
        return item;
    }

    @Override
    public Item update(Item updateItem) {
        log.info("Обновление в БД вещи с id {}...", updateItem.getId());
        Item item = insert(updateItem.getId(), updateItem);
        log.info("Вещь с id {} была успешно обновлена в БД", item.getId());
        return item;
    }

    @Override
    public void delete(long itemId) {
        log.info("Удаление вещи из БД с id {}...", itemId);
        super.delete(itemId);
        log.info("Вещь с id {} была успешно удалена из БД", itemId);
    }
}
