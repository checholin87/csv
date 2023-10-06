package me.secosme.csv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@RestController("csv")
public class CsvController {

    private CsvProcessor csvProcessor;

    @PostMapping("upload")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    public void upload(@RequestParam("file") MultipartFile file) throws IOException {

        log.info("Uploaded file name: {}", file.getOriginalFilename());
        log.info("Uploaded file size: {}", file.getSize());
        log.info("Uploaded file thread: {}", Thread.currentThread().toString());

        var baos = new ByteArrayOutputStream();
        file.getInputStream().transferTo(baos);

        csvProcessor.process(baos.toByteArray());

    }

}
