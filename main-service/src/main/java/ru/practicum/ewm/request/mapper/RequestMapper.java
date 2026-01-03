package ru.practicum.ewm.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "created", source = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    ParticipationRequestDto toDto(ParticipationRequest request);

    @Named("statusToString")
    default String statusToString(RequestStatus status) {
        return status != null ? status.toString() : null;
    }
}
