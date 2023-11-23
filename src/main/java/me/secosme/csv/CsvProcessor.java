package me.secosme.csv;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.var;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CsvProcessor {

    private CsvFile2AssociateCsvDTO csvFile2AssociateCsvDTO;

    private ICsvMapper csvMapper;

    private IAssociateRepository associateRepository;

    private int batchSize;

    private SimpMessagingTemplate template;

    public CsvProcessor(CsvFile2AssociateCsvDTO csvFile2AssociateCsvDTO,
        ICsvMapper csvMapper, IAssociateRepository associateRepository,
        @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}") int batchSize,
        SimpMessagingTemplate template) {
        this.csvFile2AssociateCsvDTO = csvFile2AssociateCsvDTO;
        this.csvMapper = csvMapper;
        this.associateRepository = associateRepository;
        this.batchSize = batchSize;
        this.template = template;
    }

    @Async
    @Transactional
    public void process(byte[] csvFile) {

        MDC.put("thread", Thread.currentThread().toString());
        MDC.put("batchSize", Integer.toString(batchSize));

        var message = String.format("Processing CSV started at %s on thread %s by bulks of %s", System.currentTimeMillis(),
            Thread.currentThread().toString(), batchSize);

        log.info(message);
        template.convertAndSend("/topic/csv/progress", new CsvProcessorStatus(CsvProcessorStatusEnum.STARTED, LocalDateTime.now(), message));

        var dtos = csvFile2AssociateCsvDTO.apply(new StringReader(new String(csvFile, StandardCharsets.UTF_8)));
        var batch = new ArrayList<AssociateCsvDTO>();

        while (dtos.hasNext()) {

            batch.add(dtos.next());

            if (batch.size() == batchSize) {
                processBulk(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            processBulk(batch);
        }

        message = String.format("Processing CSV finished at %s on thread %s", System.currentTimeMillis(),
            Thread.currentThread().toString());

        log.info(message);
        template.convertAndSend("/topic/csv/progress", new CsvProcessorStatus(CsvProcessorStatusEnum.FINISHED, LocalDateTime.now(), message));

        MDC.clear();

    }

    private void processBulk(List<AssociateCsvDTO> bulk) {

        var identifications = bulk.stream().map(AssociateCsvDTO::getIdentification).collect(Collectors.toList());
        var existingAssociates = associateRepository.findByIdentificationIn(identifications);

        var newAssociates = bulk.stream()
            .filter(a -> existingAssociates.stream().noneMatch(ea -> ea.getIdentification().equals(a.getIdentification())))
            .map(csvMapper::toAssociate)
            .collect(Collectors.toList());

        existingAssociates.forEach(ea -> {
            var optional = bulk.stream().filter(a -> a.getIdentification().equals(ea.getIdentification())).findFirst();
            optional.ifPresent(a -> csvMapper.toAssociate(a, ea));
        });

        if (!newAssociates.isEmpty()) {
            log.info("Saving {} new associates", newAssociates.size());
            associateRepository.saveAll(newAssociates);
        }

        if (!existingAssociates.isEmpty()) {
            log.info("Saving {} existing associates", existingAssociates.size());
            associateRepository.saveAll(existingAssociates);
        }

        associateRepository.saveAll(existingAssociates);
        associateRepository.flush();

    }

    public record CsvProcessorStatus(CsvProcessorStatusEnum status, LocalDateTime timestamp, String message) {
    }

    public enum CsvProcessorStatusEnum {
        STARTED, FINISHED
    }

}
