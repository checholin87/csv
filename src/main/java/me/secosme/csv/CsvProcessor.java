package me.secosme.csv;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
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

    public CsvProcessor(CsvFile2AssociateCsvDTO csvFile2AssociateCsvDTO, 
        ICsvMapper csvMapper, IAssociateRepository associateRepository, 
        @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}") int batchSize) {
        this.csvFile2AssociateCsvDTO = csvFile2AssociateCsvDTO;
        this.csvMapper = csvMapper;
        this.associateRepository = associateRepository;
        this.batchSize = batchSize;
    }

    @Async
    @Transactional
    public void process(byte[] csvFile) {
        log.info("Processing CSV started at {} on thread {} by bulks of {}", System.currentTimeMillis(), 
            Thread.currentThread().toString(), batchSize);

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

        log.info("Processing CSV finished at {}", System.currentTimeMillis());
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

}
