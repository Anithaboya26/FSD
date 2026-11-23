package com.ventixe.events.repository;

import com.ventixe.events.entity.EventImage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {

  List<EventImage> findByEvent_IdInOrderBySortOrderAsc(Collection<Long> eventIds);
}
