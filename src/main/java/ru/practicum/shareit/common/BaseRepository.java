package ru.practicum.shareit.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BaseRepository<T> {
    protected final Map<Long, T> table;
    protected long id;

    public BaseRepository() {
        table = new HashMap<>();
        id = 1L;
    }

    protected Collection<T> findMany() {
        return table.values();
    }

    protected Optional<T> findOne(long id) {
        return table.get(id) == null ? Optional.empty() : Optional.of(table.get(id));
    }

    protected T insert(long id, T value) {
        table.put(id, value);
        return value;
    }

    protected void delete(long id) {
        table.remove(id);
    }

    protected long idGenerator() {
        return id++;
    }
}
