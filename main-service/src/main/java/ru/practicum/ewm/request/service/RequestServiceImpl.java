package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.exception.AccessException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.request.model.RequestStatus.CANCELED;


@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        log.info("create({}, {})", userId, eventId);

        User user = userValidation(userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ConflictException("Событие с id=" + eventId + " не найдено"));

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.existsByEventAndRequester(event, user)) {
            throw new ConflictException("Нельзя отправить запрос повторно");
        }

        if (event.getInitiator().equals(user)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (event.getParticipantLimit() > 0
                && !event.getRequestModeration()
                && event.getParticipantLimit() <= requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)) {
            throw new ConflictException("Достигнут лимит участников события");
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
        log.info("Запрос на участие создан: {}", saved);

        return requestMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> get(Long userId) {
        log.info("get({})", userId);
        User user = userValidation(userId);

        List<ParticipationRequestDto> requests = requestRepository.findAllByRequester(user).stream()
                .map(requestMapper::toDto)
                .toList();

        log.info("По запросу пользователя возвращён список заявок: {}", requests);
        return requests;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        log.info("cancel({}, {})", userId, requestId);
        User user = userValidation(userId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Данные не найдены"));

        if (!request.getRequester().equals(user)) {
            throw new AccessException("Нет доступа");
        }

        request.setStatus(CANCELED);
        ParticipationRequest savedRequest = requestRepository.save(request);
        log.info("Заявка на участие в событии отменена: {}", savedRequest);

        return requestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventParticipationRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new AccessException("Только инициатор события может просматривать заявки");
        }

        return requestRepository.findAllByEvent(event).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatus(Long userId, Long eventId,
                                                                   EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new AccessException("Только инициатор может управлять заявками");
        }

        if (!event.getRequestModeration()) {
            throw new ConflictException("Модерация заявок отключена для этого события");
        }

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Изменять статус заявок можно только для опубликованного события");
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int limit = event.getParticipantLimit();

        List<ParticipationRequest> requests = requestRepository.findAllById(updateRequest.requestIds());

        List<ParticipationRequest> toConfirm = new java.util.ArrayList<>();
        List<ParticipationRequest> toReject = new java.util.ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Можно менять статус только для заявок в состоянии PENDING");
            }

            if (updateRequest.status() == RequestStatus.CONFIRMED) {
                if (limit > 0 && confirmedCount >= limit) {
                    throw new ConflictException("Достигнут лимит участников события");
                }
                request.setStatus(RequestStatus.CONFIRMED);
                toConfirm.add(request);
                confirmedCount++;
            } else if (updateRequest.status() == RequestStatus.REJECTED) {
                request.setStatus(RequestStatus.REJECTED);
                toReject.add(request);
            }
        }


        List<ParticipationRequest> updatedRequests = requestRepository.saveAll(requests);

        List<ParticipationRequestDto> confirmedDtos = toConfirm.stream()
                .map(requestMapper::toDto)
                .toList();

        List<ParticipationRequestDto> rejectedDtos = toReject.stream()
                .map(requestMapper::toDto)
                .toList();

        return new EventRequestStatusUpdateResult(confirmedDtos, rejectedDtos);
    }

    private User userValidation(Long userId) {
        log.info("userValidation({})", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        log.info("Запрос на поиск пользователя прошёл успешно: {}", user);
        return user;
    }
}