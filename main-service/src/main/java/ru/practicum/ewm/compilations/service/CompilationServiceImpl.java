package ru.practicum.ewm.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilations.mapper.CompilationMapper;
import ru.practicum.ewm.compilations.model.Compilation;
import ru.practicum.ewm.compilations.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventDtoShortWithoutViews;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.repository.RequestRepository;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.events() != null && !newCompilationDto.events().isEmpty()) {
            compilation.setEvents(eventRepository.findAllByIdIn(newCompilationDto.events()));
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        List<Long> ids = savedCompilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(ids);

        List<EventDtoShortWithoutViews> eventsWithRequests = savedCompilation.getEvents().stream()
                .map(event -> eventMapper.toDtoShort(event, confirmedRequests.getOrDefault(event.getId(), 0L)))
                .toList();

        return new CompilationDto(savedCompilation.getId(),
                eventsWithRequests,
                savedCompilation.getPinned(),
                savedCompilation.getTitle());
    }

    @Override
    public Compilation getCompilation(Long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Compilation id=" + compilationId + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compilationId) {
        Compilation compilation = getCompilation(compilationId);
        CompilationDto compilationDto = compilationMapper.toDto(compilation);
        if (compilation.getEvents() != null) {
            List<Long> ids = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
            Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(ids);
            compilationDto.events().addAll(compilation.getEvents().stream()
                    .map(event ->
                            eventMapper.toDtoShort(event, confirmedRequests.getOrDefault(event.getId(), 0L)))
                    .toList());
        }
        return compilationDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations;
        List<CompilationDto> result = new ArrayList<>();
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
            for (Compilation compilation : compilations) {
                CompilationDto compilationDto = compilationMapper.toDto(compilation);
                if (compilation.getEvents() != null) {

                    List<Long> ids = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());

                    Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(ids);
                    compilationDto.events().addAll(compilation.getEvents().stream()
                            .map(event ->
                                    eventMapper.toDtoShort(event, confirmedRequests.getOrDefault(event.getId(), 0L)))
                            .toList());
                }
                result.add(compilationDto);
            }
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
            for (Compilation compilation : compilations) {
                CompilationDto compilationDto = compilationMapper.toDto(compilation);
                if (compilation.getEvents() != null) {

                    List<Long> ids = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());

                    Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(ids);

                    compilationDto.events().addAll(compilation.getEvents().stream()
                            .map(event ->
                                    eventMapper.toDtoShort(event,
                                            confirmedRequests.getOrDefault(event.getId(), 0L)))
                            .toList());
                }
                result.add(compilationDto);
            }
        }
        return result;
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilation) {
        Compilation compilation = getCompilation(compId);
        if (updateCompilation.events() != null) {
            Set<Event> events = updateCompilation.events().stream().map(id -> {
                Event event = new Event();
                event.setId(id);
                return event;
            }).collect(Collectors.toSet());
            compilation.setEvents(events);
        }
        if (updateCompilation.pinned() != null) {
            compilation.setPinned(updateCompilation.pinned());
        }
        String title = updateCompilation.title();
        if (title != null && !title.isBlank()) {
            compilation.setTitle(title);
        }
        CompilationDto compilationDto = compilationMapper.toDto(compilationRepository.save(compilation));
        if (compilation.getEvents() != null) {
            List<Long> ids = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
            Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(ids);

            compilationDto.events().addAll(compilation.getEvents().stream()
                    .map(event ->
                            eventMapper.toDtoShort(event,
                                    confirmedRequests.getOrDefault(event.getId(), 0L)))
                    .toList());
        }
        return compilationDto;
    }

    @Override
    public void deleteCompilation(Long compilationId) {
        getCompilation(compilationId);
        compilationRepository.deleteById(compilationId);
    }
}
