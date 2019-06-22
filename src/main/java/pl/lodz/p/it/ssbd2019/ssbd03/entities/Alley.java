package pl.lodz.p.it.ssbd2019.ssbd03.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Klasa reprezentująca tory.
 */
@Entity
@Table(name = "alleys", schema = "public", catalog = "ssbd03")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedQueries(
        value = {
                @NamedQuery(
                        name = "Alley.findAlleysNotReservedBetweenTimes", 
                        query = "select a from Alley a where a.id not in (select distinct a.id from Alley a, Reservation r where a.id = r.alley.id and ((r.startDate <= :startTime and :startTime <= r.endDate) or (r.startDate <= :endTime and :endTime <= r.endDate)))"
                ),
                @NamedQuery(
                        name = "Alley.findByNumber",
                        query = "select a from Alley a where a.number = :number"
                )
        }
)
public class Alley {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    @EqualsAndHashCode.Exclude
    private Long id;

    @Min(1)
    @NotNull
    @Column(name = "number", nullable = false, unique = true)
    @ToString.Exclude
    private int number;
    
    @NotNull
    @Column(name = "active", nullable = false)
    @ToString.Exclude
    private boolean active;
    
    @NotNull
    @Column(name = "max_score", nullable = false)
    @Min(0)
    @Max(300)
    @ToString.Exclude
    private int maxScore;

    @Version
    @NotNull
    @Min(0)
    @Column(name = "version", nullable = false)
    private long version;
}
