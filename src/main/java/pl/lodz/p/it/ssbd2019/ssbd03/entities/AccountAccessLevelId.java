package pl.lodz.p.it.ssbd2019.ssbd03.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Klasa reprezentująca klucz dla relacji powiązania między użytkownikami a poziomami dostępu.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountAccessLevelId implements Serializable {
    private UserAccount account;
    private AccessLevel accessLevel;
}
