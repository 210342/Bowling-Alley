package pl.lodz.p.it.ssbd2019.ssbd03.entities;

import lombok.*;
import pl.lodz.p.it.ssbd2019.ssbd03.validators.ValidReservationDates;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.List;

/**
 * Klasa reprezentująca rezerwacje.
 */
@Entity
@Table(name = "reservations", schema = "public", catalog = "ssbd03")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedQueries(
        value = {
                @NamedQuery(name = "Reservation.findReservationsForUser", query = "select r from Reservation r where r.userAccount.id = :userId order by r.startDate desc"),
                @NamedQuery(name = "Reservation.findReservationsForAlley", query = "select r from Reservation r where r.alley.id = :alleyId order by r.startDate desc" ),
                @NamedQuery(
                        name = "Reservation.getReservationsWithinTimeRange",
                        query = "SELECT DISTINCT r " +
                                "FROM Reservation r " +
                                "WHERE r.active = true and " +
                                    "(r.startDate < :startTime and :startTime < r.endDate) or " +
                                    "(r.startDate < :endTime and :endTime < r.endDate) or " +
                                    "(:startTime < r.startDate and r.endDate < :endTime) "
                )
        }
)
@ValidReservationDates
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    @EqualsAndHashCode.Exclude
    private Long id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk__reservations__users", value = ConstraintMode.CONSTRAINT))
    @ToString.Exclude
    private UserAccount userAccount;

    @NotNull
    @Column(name = "start_date", nullable = false)
    @FutureOrPresent
    @ToString.Exclude
    private Timestamp startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    @Future
    @ToString.Exclude
    private Timestamp endDate;

    @Min(1)
    @NotNull
    @Column(name = "players_count", nullable = false)
    @ToString.Exclude
    private int playersCount;

    @NotNull
    @Column(name = "active", nullable = false)
    @ToString.Exclude
    private boolean active;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "alley_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk__reservations__alleys", value = ConstraintMode.CONSTRAINT))
    @ToString.Exclude
    private Alley alley;

    @ToString.Exclude
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.MERGE)
    private List<Comment> comments;

    @ToString.Exclude
    @OneToMany(mappedBy = "reservation", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    private List<ReservationItem> reservationItems;
    
    @Version
    @NotNull
    @Min(0)
    @Column(name = "version", nullable = false)
    private long version;
}
