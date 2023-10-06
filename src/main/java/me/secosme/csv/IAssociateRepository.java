package me.secosme.csv;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IAssociateRepository extends JpaRepository<Associate, UUID> {

    List<Associate> findByIdentificationIn(List<String> identifications);

}
