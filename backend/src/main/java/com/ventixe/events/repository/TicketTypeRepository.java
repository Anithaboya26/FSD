package com.ventixe.events.repository;

import com.ventixe.events.entity.TicketType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

  List<TicketType> findByEvent_IdInAndActiveTrueOrderByPriceAsc(Collection<Long> eventIds);

  List<TicketType> findByEvent_IdOrderByPriceAsc(Long eventId);

  List<TicketType> findByEvent_IdAndActiveTrueOrderByPriceAsc(Long eventId);

  Optional<TicketType> findByIdAndEvent_Id(Long id, Long eventId);
}
