package me.secosme.csv;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IAssociateRepository extends JpaRepository<Associate, UUID> {

    Optional<Associate> findByIdentification(String identification);

}
