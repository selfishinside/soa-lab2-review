package ru.itmo.soa.workers.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import ru.itmo.soa.workers.dto.CoordinatesDto;
import ru.itmo.soa.workers.dto.PersonDto;
import ru.itmo.soa.workers.dto.WorkerRequestDto;
import ru.itmo.soa.workers.dto.WorkerResponseDto;
import ru.itmo.soa.workers.exception.ApiException;
import ru.itmo.soa.workers.model.CoordinatesEmbeddable;
import ru.itmo.soa.workers.model.PersonEmbeddable;
import ru.itmo.soa.workers.model.WorkerEntity;
import ru.itmo.soa.workers.repository.WorkerRepository;

@ApplicationScoped
public class WorkerService {

    @Inject
    private WorkerRepository repository;

    @Transactional(Transactional.TxType.REQUIRED)
    public WorkerResponseDto create(WorkerRequestDto request) {
        WorkerEntity worker = toEntity(request);
        worker.setCreationDate(currentDate());

        repository.save(worker);
        repository.includeInOrganizationOne(worker.getId());

        return toDto(worker);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public WorkerResponseDto get(long id) {
        return toDto(findRequired(id, false));
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public WorkerResponseDto update(long id, WorkerRequestDto request) {
        WorkerEntity existingWorker = findRequired(id, true);
        WorkerEntity updatedWorker = toEntity(request);

        updatedWorker.setId(id);
        updatedWorker.setCreationDate(existingWorker.getCreationDate());

        return toDto(repository.save(updatedWorker));
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void delete(long id) {
        WorkerEntity worker = findRequired(id, true);
        repository.deleteMembership(id);
        repository.delete(worker);
    }

    private WorkerEntity findRequired(long id, boolean forUpdate) {
        WorkerEntity worker = forUpdate
                ? repository.findForUpdate(id)
                : repository.find(id);
        if (worker == null) {
            throw new ApiException(404, "Worker not found");
        }
        return worker;
    }

    private static WorkerEntity toEntity(WorkerRequestDto request) {
        CoordinatesEmbeddable coordinates = new CoordinatesEmbeddable(
                request.coordinates().x(),
                request.coordinates().y()
        );

        PersonEmbeddable person = request.person() == null
                ? null
                : new PersonEmbeddable(
                        request.person().passportID(),
                        request.person().eyeColor(),
                        request.person().nationality()
                );

        WorkerEntity worker = new WorkerEntity();
        worker.setName(request.name());
        worker.setCoordinates(coordinates);
        worker.setSalary(request.salary());
        worker.setPosition(request.position());
        worker.setStatus(request.status());
        worker.setPerson(person);
        return worker;
    }

    public static WorkerResponseDto toDto(WorkerEntity worker) {
        CoordinatesDto coordinates = new CoordinatesDto(
                worker.getCoordinates().getX(),
                worker.getCoordinates().getY()
        );

        PersonDto person = worker.getPerson() == null
                || worker.getPerson().getPassportID() == null
                ? null
                : new PersonDto(
                        worker.getPerson().getPassportID(),
                        worker.getPerson().getEyeColor(),
                        worker.getPerson().getNationality()
                );

        return new WorkerResponseDto(
                worker.getId(),
                worker.getName(),
                coordinates,
                worker.getCreationDate()
                        .toInstant()
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate(),
                worker.getSalary(),
                worker.getPosition(),
                worker.getStatus(),
                person
        );
    }

    private static Date currentDate() {
        return Date.from(
                LocalDate.now(ZoneOffset.UTC)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
        );
    }
}
