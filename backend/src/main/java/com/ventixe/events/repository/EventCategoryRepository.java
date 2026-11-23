package com.ventixe.events.repository;

import com.ventixe.events.entity.EventCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventCategoryRepository extends JpaRepository<EventCategory, Long> {

  List<EventCategory> findByActiveTrueOrderByNameAsc();

  Optional<EventCategory> findByIdAndActiveTrue(Long id);
}
