package com.ventixe.events.repository;

import com.ventixe.events.entity.Event;
import com.ventixe.events.entity.EventStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

  @EntityGraph(attributePaths = {"category", "venue"})
  @Query("""
      select e
      from Event e
      join e.category c
      join e.venue v
      where e.status = :status
        and c.active = true
        and (:categorySlug is null or c.slug = :categorySlug)
        and (:cityPattern is null or lower(v.city) like lower(:cityPattern))
        and (:queryPattern is null
          or lower(e.title) like lower(:queryPattern)
          or lower(e.description) like lower(:queryPattern)
          or lower(v.name) like lower(:queryPattern)
          or lower(v.city) like lower(:queryPattern))
        and (:fromInclusive is null or e.startsAt >= :fromInclusive)
        and (:toExclusive is null or e.startsAt < :toExclusive)
      """)
  Page<Event> search(
      @Param("status") EventStatus status,
      @Param("categorySlug") String categorySlug,
      @Param("cityPattern") String cityPattern,
      @Param("queryPattern") String queryPattern,
      @Param("fromInclusive") LocalDateTime fromInclusive,
      @Param("toExclusive") LocalDateTime toExclusive,
      Pageable pageable);

  @EntityGraph(attributePaths = {"category", "venue"})
  Optional<Event> findBySlugAndStatus(String slug, EventStatus status);

  @EntityGraph(attributePaths = {"category", "venue"})
  List<Event> findByOrganizer_UserIdOrderByCreatedAtDesc(Long organizerUserId);

  @EntityGraph(attributePaths = {"category", "venue"})
  Optional<Event> findByIdAndOrganizer_UserId(Long id, Long organizerUserId);

  boolean existsBySlug(String slug);
}
