package pl.lodz.p.it.ssbd2019.ssbd03.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
/**
 * Klasa reprezentująca przedmioty.
 */
@Entity
@Table(name = "items", schema = "public", catalog = "ssbd03")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private long id;

    @Min(0)
    @Column(name = "size", nullable = false)
    private int size;

    @Min(0)
    @Column(name = "count", nullable = false)
    private int count;

    @Version
    @Min(0)
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne
    @JoinColumn(name = "item_type_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk__item__item_type", value = ConstraintMode.CONSTRAINT))
    private ItemType itemType;
}
