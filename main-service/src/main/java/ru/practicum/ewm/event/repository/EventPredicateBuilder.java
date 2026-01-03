package ru.practicum.ewm.event.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class EventPredicateBuilder {
    private static final QEvent event = QEvent.event;
    private final BooleanBuilder builder =  new BooleanBuilder();

    public EventPredicateBuilder withInitiators(List<Long> initiatorsIds) {
        if (initiatorsIds != null &&  !initiatorsIds.isEmpty()) {
            builder.and(event.initiator.id.in(initiatorsIds));
        }
        return this;
    }

    public EventPredicateBuilder withStates(List<State> states) {
        if (states != null && !states.isEmpty()) {
            builder.and(event.state.in(states));
        }
        return this;
    }

    public EventPredicateBuilder withCategories(List<Long> categoriesIds) {
        if (categoriesIds != null && !categoriesIds.isEmpty()) {
            builder.and(event.category.id.in(categoriesIds));
        }
        return this;
    }

    public EventPredicateBuilder withDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {

        builder.and(event.eventDate.goe(
                Objects.requireNonNullElseGet(rangeStart, LocalDateTime::now)));

        if (rangeEnd != null) {
            builder.and(event.eventDate.loe(rangeEnd));
        }
        return this;
    }

    public Predicate build() {
        Predicate predicate = builder.getValue();
        return predicate != null ? predicate : Expressions.TRUE.isTrue();
    }
}
