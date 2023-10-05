package me.secosme.csv;

import com.opencsv.bean.CsvBindByName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssociateCsvDTO {

    @CsvBindByName(column = "CEDULA", required = true)
    private String identification;

    @CsvBindByName(column = "NOMBRE", required = true)
    private String fullName;

    @CsvBindByName(column = "REGIONAL", required = true)
    private String regional;

    @CsvBindByName(column = "COD_DEP", required = true)
    private String dependencyCode;

    @CsvBindByName(column = "NM_DEPENDENCIA", required = true)
    private String dependencyName;

    @CsvBindByName(column = "ZONA", required = true)
    private String zone;

}
