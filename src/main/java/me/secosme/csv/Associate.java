package me.secosme.csv;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "associate")
public class Associate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "identification", unique = false, nullable = false)
    private String identification;

    @Column(name = "full_name", unique = false, nullable = false)
    private String fullName;

    @Column(name = "regional", unique = false, nullable = false)
    private String regional;

    @Column(name = "dependency_code", unique = false, nullable = false)
    private String dependencyCode;

    @Column(name = "dependency_name", unique = false, nullable = false)
    private String dependencyName;

    @Column(name = "zone", unique = false, nullable = false)
    private String zone;
}
