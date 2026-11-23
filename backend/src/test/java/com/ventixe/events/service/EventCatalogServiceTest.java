package com.ventixe.events.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ventixe.events.entity.Event;
import com.ventixe.events.entity.EventStatus;
import com.ventixe.events.repository.EventCategoryRepository;
import com.ventixe.events.repository.EventImageRepository;
import com.ventixe.events.repository.EventRepository;
import com.ventixe.events.repository.TicketTypeRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class EventCatalogServiceTest {

  @Mock
  private EventRepository eventRepository;

  @Mock
  private EventCategoryRepository categoryRepository;

  @Mock
  private EventImageRepository imageRepository;

  @Mock
  private TicketTypeRepository ticketTypeRepository;

  private EventCatalogService service;

  @BeforeEach
  void setUp() {
    service = new EventCatalogService(
        eventRepository,
        categoryRepository,
        imageRepository,
        ticketTypeRepository);
  }

  @Test
  void searchNormalizesFiltersAndReturnsPageMetadata() {
    when(eventRepository.search(
        eq(EventStatus.PUBLISHED),
        eq("music"),
        eq("%Austin%"),
        eq("%jazz%"),
        eq(LocalDate.of(2026, 11, 1).atStartOfDay()),
        eq(LocalDate.of(2026, 11, 4).atStartOfDay()),
        any()))
        .thenReturn(new PageImpl<>(List.<Event>of(), PageRequest.of(0, 10), 0));
    var result = service.search(
        "  jazz ",
        " Austin ",
        " Music ",
        LocalDate.of(2026, 11, 1),
        LocalDate.of(2026, 11, 3),
        0,
        10,
        "soonest");

    assertThat(result.content()).isEmpty();
    assertThat(result.page()).isZero();
    assertThat(result.size()).isEqualTo(10);
    assertThat(result.totalElements()).isZero();
    verify(eventRepository).search(
        eq(EventStatus.PUBLISHED),
        eq("music"),
        eq("%Austin%"),
        eq("%jazz%"),
        eq(LocalDate.of(2026, 11, 1).atStartOfDay()),
        eq(LocalDate.of(2026, 11, 4).atStartOfDay()),
        any());
  }

  @Test
  void searchRejectsInvalidDateRange() {
    assertThatThrownBy(() -> service.search(
        null,
        null,
        null,
        LocalDate.of(2026, 11, 4),
        LocalDate.of(2026, 11, 3),
        0,
        12,
        "soonest"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("from must be on or before to");
  }

  @Test
  void searchRejectsUnsupportedSortValue() {
    assertThatThrownBy(() -> service.search(
        null, null, null, null, null, 0, 12, "revenue"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("sort must be one of: soonest, latest, name");
  }

  @Test
  void searchRejectsPageSizesAboveMaximum() {
    assertThatThrownBy(() -> service.search(
        null, null, null, null, null, 0, 51, "soonest"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("size must be between 1 and 50");
  }
}
