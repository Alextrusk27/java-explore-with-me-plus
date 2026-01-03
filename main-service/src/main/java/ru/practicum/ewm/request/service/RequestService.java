package ru.practicum.ewm.request.service;

import org.springframework.data.domain.Page;
import ru.practicum.ewm.event.dto.params.EventParams;
import ru.practicum.ewm.request.dto.UpdateRequestStatusBody;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.dto.UpdateRequestStatusDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> get(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    Page<ParticipationRequestDto> getEventRequests(EventParams params);

    EventRequestStatusUpdateResult updateEventRequestStatus(UpdateRequestStatusDto dto);
}
