package pl.lodz.p.it.ssbd2019.ssbd03.mok.service;

import pl.lodz.p.it.ssbd2019.ssbd03.entities.UserAccount;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.SsbdApplicationException;
import pl.lodz.p.it.ssbd2019.ssbd03.mok.web.dto.AccountDetailsDto;
import pl.lodz.p.it.ssbd2019.ssbd03.mok.web.dto.UserRolesDto;

import java.util.List;

/**
 * Klasa reprezentująca logikę biznesową dla operacji związanych z obiektami oraz encjami klasy UserAccount.
 */
public interface UserAccountService {

    /**
     * Metoda zwracajaca listę wszystkich uzytkowników w bazie danych.
     * @return Lista encji użytkownika.
     * @throws SsbdApplicationException w wypadku gdy nie powiedzie się pobieranie użytkownika z bazy danych.
     */
    List<AccountDetailsDto> getAllUsers() throws SsbdApplicationException;

    /**
     * Metoda pobiera z bazy danych uzytkownika o podanym id.
     * @param id Identyfikator uzytkownika, którego należy pobrać z bazy danych.
     * @return Użytkownik o zadanym id.
     * @throws SsbdApplicationException w wypadku gdy nie powiedzie się pobieranie użytkownika z bazy danych,
     * bądź gdy nie znajdzie użytkownika.
     */
    AccountDetailsDto getUserById(Long id) throws SsbdApplicationException;

    /**
     * Aktualizuje dane użytkownika w bazie danych. Użytkownik musi być zawarty w obecnym kotekście (sesji).
     * @param userAccount Encja użytkownika do zaktualizowania.
     * @throws SsbdApplicationException w wypadku, gdy nie uda się aktualizacja.
     */
    void updateUser(AccountDetailsDto userAccount) throws SsbdApplicationException;
    
    /**
     * Aktualizuje poziomy dostępu użytkownika w bazie danych. Użytkownik musi być zawarty w obecnym kotekście (sesji).
     * @param userAccount Encja użytkownika do zaktualizowania.
     * @param selectedAccessLevels Przydzielone użytkownikowi poziomy dostępu.
     * @throws SsbdApplicationException w wypadku, gdy nie uda się aktualizacja.
     */
    void updateUserAccessLevels(UserRolesDto userAccount, List<String> selectedAccessLevels) throws SsbdApplicationException;

    /**
     * Metoda pobiera z bazy danych uzytkownika o podanym loginie.
     * @param login Login uzytkownika, którego należy pobrać z bazy danych.
     * @return Użytkownik o zadanym loginie.
     * @throws SsbdApplicationException w wypadku gdy nie powiedzie się pobieranie użytkownika z bazy danych,
     * bądź gdy nie znajdzie użytkownika.
     */
    AccountDetailsDto getByLogin(String login) throws SsbdApplicationException;

    /**
     * Metoda pozwalająca zmienić hasło użytkownika o podanym loginie.
     * Wymagane jest podanie obecnego hasła.
     *
     * @param login           login użytkownika
     * @param currentPassword aktualne hasło użytkownika
     * @param newPassword     nowe hasło użytkownika
     * @throws SsbdApplicationException wyjątek zmiany hasła
     */
    void changePasswordByLogin(String login, String currentPassword, String newPassword) throws SsbdApplicationException;

    /**
     * Zmienia flagę zablokowania konta użytkownika z podanym id
     *
     * @param id identyfikator użytkownika
     * @param isActive nowa wartość flagi zablokowania
     * @throws SsbdApplicationException w wypadku, gdy nie uda się aktualizacja.
     */
    void updateLockStatusOnAccountById(Long id, boolean isActive) throws SsbdApplicationException;

    /**
     * Metoda pozwalająca zmienić hasło użytkownika o podanym id.
     *
     * @param id           identyfikator użytkownika
     * @param newPassword     nowe hasło użytkownika
     * @throws SsbdApplicationException gdy nie uda się zmienic hasła
     */
    void changePasswordById(long id, String newPassword) throws SsbdApplicationException;

    /**
     * Metoda pobiera z bazy danych uzytkowników, których imię lub nazwisko zawiera podany ciąg znaków.
     * @param name Ciąg znaków, który musi zawierać się w imieniu bądź nazwisku użytkowników
     * @return Lista użytkowników, których imię lub nazwisko zawierają podany ciąg znaków
     * @throws SsbdApplicationException w wypadku gdy nie powiedzie się pobieranie użytkowników z bazy danych,
     * bądź gdy nie znajdzie żadnego użytkownika.
     */
    List<AccountDetailsDto> getAllByNameOrLastName(String name) throws SsbdApplicationException;

    /**
     * Metoda służy do potwierdzenia konta na bazie podanego tokena.
     * Metoda może wyrzucić błąd również w przypadku, gdy użytkownik nie isntieje.
     * @param token wartość tokena potwierdzenia
     * @throws SsbdApplicationException W przypadku, gdy nie uda się potwierdzić konta.
     */
    void activateAccountByToken(String token) throws SsbdApplicationException;
}
