package me.secosme.csv;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ICsvMapper {

    AssociateCsvDTO toAssociateCsvDTO(Associate associate);

    @Mapping(target = "id", ignore = true)
    Associate toAssociate(AssociateCsvDTO associateCsvDTO);

    @Mapping(target = "id", ignore = true)
    void toAssociate(AssociateCsvDTO dto, @MappingTarget Associate associate);

}
