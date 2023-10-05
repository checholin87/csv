package me.secosme.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.opencsv.bean.CsvToBeanBuilder;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CsvFile2AssociateCsvDTO implements Function<Reader, Iterator<AssociateCsvDTO>> {

    @Override
    public Iterator<AssociateCsvDTO> apply(Reader reader) {
        return new CsvToBeanBuilder<AssociateCsvDTO>(reader)
            .withType(AssociateCsvDTO.class)
            .withIgnoreLeadingWhiteSpace(true)
            .withSeparator(';')
            .build()
            .iterator();
    }

}
