package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findAllBySharerId(long ownerId, Pageable pageable);

    @Query(" select i from Item i " +
           "where upper(i.name) like upper(concat('%', ?1, '%')) " +
           "or upper(i.description) like upper(concat('%', ?1, '%'))")
    Page<Item> search(String text, Pageable pageable);

    Collection<Item> findByRequestIdIn(List<Long> requestIds);

}
