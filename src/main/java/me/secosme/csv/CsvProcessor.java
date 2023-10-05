package me.secosme.csv;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.var;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class CsvProcessor {
    
    private CsvFile2AssociateCsvDTO csvFile2AssociateCsvDTO;

    private ICsvMapper csvMapper;

    private IAssociateRepository associateRepository;

    @Async
    @Transactional
    public void process(byte[] csvFile) {

        log.info("Processing CSV started at {} on thread {}", System.currentTimeMillis(), 
            Thread.currentThread().toString());

        var dtos = csvFile2AssociateCsvDTO.apply(new StringReader(new String(csvFile, StandardCharsets.UTF_8)));

        while (dtos.hasNext()) {

            var dto = dtos.next();
            var optional = associateRepository.findByIdentification(dto.getIdentification());

            Associate associate;
    
            if(optional.isPresent()) {
                associate = optional.get();
                csvMapper.toAssociate(dto, associate);
            } else {
                associate = csvMapper.toAssociate(dto);
            }

            associateRepository.saveAndFlush(associate);

        }

        log.info("Processing CSV finished at {}", System.currentTimeMillis());

    }

}
