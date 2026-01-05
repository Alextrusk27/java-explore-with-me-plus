package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.dto.params.EventParams;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.exception.AccessException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.dto.UpdateRequestStatusDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.sharing.EntityFinder;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.ewm.request.model.RequestStatus.CANCELED;
import static ru.practicum.ewm.sharing.constants.AppConstants.REQUESTS_DEFAULT_PAGEABLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    private final EntityFinder finder;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {

        User user = finder.findUserOrThrow(userId);
        Event event = finder.findEventOrThrow(eventId);

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException(
                    "Cannot participate in event %d: event is not published (current state: %s)"
                            .formatted(eventId, event.getState())
            );
        }

        if (requestRepository.existsByEventAndRequester(event, user)) {
            throw new ConflictException(
                    "User %d already has a participation request for event %d"
                            .formatted(user.getId(), eventId)
            );
        }

        if (event.getInitiator().equals(user)) {
            throw new ConflictException(
                    "User %d cannot participate in their own event %d"
                            .formatted(user.getId(), eventId)
            );
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() > 0
                && !event.getRequestModeration()
                && event.getParticipantLimit() <= confirmedRequests) {
            throw new ConflictException(
                    "Event %d has reached participant limit (%d/%d)"
                            .formatted(eventId, confirmedRequests, event.getParticipantLimit())
            );
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);

        RequestStatus status = (!event.getRequestModeration() || event.getParticipantLimit() == 0)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        request.setStatus(status);

        ParticipationRequest saved = requestRepository.save(request);

        return requestMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> get(Long userId) {
        User user = finder.findUserOrThrow(userId);

        return requestRepository.findAllByRequester(user).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        User user = finder.findUserOrThrow(userId);

        ParticipationRequest request = finder.findEventRequestOrThrow(requestId);

        if (!request.getRequester().equals(user)) {
            throw new AccessException(
                    "User %d attempted to cancel request %d, but is not the requester"
                            .formatted(userId, requestId)
            );
        }

        request.setStatus(CANCELED);
        ParticipationRequest savedRequest = requestRepository.save(request);

        return requestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(EventParams params) {
        Event event = finder.findEventOrThrow(params.eventId());
        long initiatorId = event.getInitiator().getId();

        if (initiatorId != params.userId()) {
            throw new AccessException(
                    "User %d attempted to view requests for event %d, but is not the initiator"
                            .formatted(params.userId(), params.eventId()));
        }

        List<ParticipationRequest> result = requestRepository.findAllByEvent(event, REQUESTS_DEFAULT_PAGEABLE)
                .getContent();

        return result.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatus(UpdateRequestStatusDto dto) {
        Event event = finder.findEventOrThrow(dto.eventId());

        if (!event.getInitiator().getId().equals(dto.userId())) {
            throw new AccessException(
                    ("User %d is not authorized to manage requests for event %d. " +
                            "Only event initiator can perform this action")
                            .formatted(dto.userId(), dto.eventId())
            );
        }

        if (!event.getRequestModeration()) {
            throw new ConflictException(
                    "Event %d has request moderation disabled. Cannot update request statuses"
                            .formatted(dto.eventId())
            );
        }

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException(
                    "Cannot update request statuses for event %d: event is not published (current state: %s)"
                            .formatted(dto.eventId(), event.getState())
            );
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(dto.eventId(), RequestStatus.CONFIRMED);
        int participantLimit = event.getParticipantLimit();

        List<ParticipationRequest> requests = requestRepository.findAllById(dto.requestIds());

        List<ParticipationRequest> toConfirm = new ArrayList<>();
        List<ParticipationRequest> toReject = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException(
                        "Request %d is not in PENDING state (current: %s). Only PENDING requests can be updated"
                                .formatted(request.getId(), request.getStatus())
                );
            }

            if (dto.status() == RequestStatus.CONFIRMED) {
                if (participantLimit > 0 && confirmedCount >= participantLimit) {
                    throw new ConflictException(
                            "Event %d has reached participant limit. Cannot confirm more requests (%d/%d)"
                                    .formatted(dto.eventId(), confirmedCount, participantLimit)
                    );
                }

                request.setStatus(RequestStatus.CONFIRMED);
                toConfirm.add(request);
                confirmedCount++;

            } else if (dto.status() == RequestStatus.REJECTED) {
                request.setStatus(RequestStatus.REJECTED);
                toReject.add(request);
            }
        }

        requestRepository.saveAll(requests);

        List<ParticipationRequestDto> confirmedDtos = toConfirm.stream()
                .map(requestMapper::toDto)
                .toList();

        List<ParticipationRequestDto> rejectedDtos = toReject.stream()
                .map(requestMapper::toDto)
                .toList();

        return new EventRequestStatusUpdateResult(confirmedDtos, rejectedDtos);
    }
}