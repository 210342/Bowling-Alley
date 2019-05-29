package pl.lodz.p.it.ssbd2019.ssbd03.mok.service;

import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import pl.lodz.p.it.ssbd2019.ssbd03.entities.AccessLevel;
import pl.lodz.p.it.ssbd2019.ssbd03.entities.AccountAccessLevel;
import pl.lodz.p.it.ssbd2019.ssbd03.entities.PreviousUserPassword;
import pl.lodz.p.it.ssbd2019.ssbd03.entities.UserAccount;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.SsbdApplicationException;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.conflict.AccountPasswordNotUniqueException;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.entity.AccessLevelDoesNotExistException;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.entity.EntityRetrievalException;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.entity.LoginDoesNotExistException;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.entity.UserIdDoesNotExistException;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.generalized.ChangePasswordException;
import pl.lodz.p.it.ssbd2019.ssbd03.mok.repository.AccessLevelRepositoryLocal;
import pl.lodz.p.it.ssbd2019.ssbd03.mok.repository.UserAccountRepositoryLocal;
import pl.lodz.p.it.ssbd2019.ssbd03.mok.web.dto.AccountDetailsDto;
import pl.lodz.p.it.ssbd2019.ssbd03.mok.web.dto.UserRolesDto;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.SHA256Provider;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.localization.LocalizedMessageProvider;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.messaging.Messenger;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.roles.MokRoles;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.tracker.InterceptorTracker;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.tracker.TransactionTracker;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import java.util.List;
import java.util.stream.Collectors;

@Stateful
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Interceptors(InterceptorTracker.class)
public class UserAccountServiceImpl extends TransactionTracker implements UserAccountService {
    @EJB(beanName = "MOKUserRepository")
    UserAccountRepositoryLocal userAccountRepositoryLocal;

    @EJB(beanName = "MOKAccessLevelRepository")
    AccessLevelRepositoryLocal accessLevelRepositoryLocal;

    @EJB
    private Messenger messenger;

    @Inject
    private LocalizedMessageProvider localization;


    private ModelMapper modelMapper = new ModelMapper();

    private UserAccount userAccount;

    @Override
    @RolesAllowed(MokRoles.GET_ALL_USERS_LIST)
    public List<UserAccount> getAllUsers() throws SsbdApplicationException {
        return userAccountRepositoryLocal.findAll();
    }

    @Override
    @RolesAllowed(MokRoles.GET_USER_DETAILS)
    public AccountDetailsDto getUserById(Long id) throws SsbdApplicationException {
        this.userAccount = userAccountRepositoryLocal.findById(id).orElseThrow(
                () -> new UserIdDoesNotExistException("Account with id '" + id + "' does not exist."));
        Hibernate.initialize(this.userAccount.getAccountAccessLevels());
        AccountDetailsDto accountDetailsDto = modelMapper.map(this.userAccount, AccountDetailsDto.class);
        return accountDetailsDto;
    }


    @Override
    @RolesAllowed({MokRoles.EDIT_USER_ACCOUNT, MokRoles.EDIT_OWN_ACCOUNT})
    public void updateUser(AccountDetailsDto userAccountDto) throws SsbdApplicationException {
        this.userAccount.setFirstName(userAccountDto.getFirstName());
        this.userAccount.setLastName(userAccountDto.getLastName());
        this.userAccount.setEmail(userAccountDto.getEmail());
        this.userAccount.setPhone(userAccountDto.getPhone());
        userAccountRepositoryLocal.edit(this.userAccount);
    }

    @Override
    @RolesAllowed({MokRoles.CHANGE_ACCESS_LEVEL, MokRoles.EDIT_OWN_ACCOUNT})
    public void updateUserAccessLevels(UserRolesDto userAccountDto, List<String> selectedAccessLevels) throws SsbdApplicationException {
        setActiveFieldForExistingAccountAccessLevelsOfEditedUser(userAccount.getAccountAccessLevels(), selectedAccessLevels);
        addNewAccountAccessLevelsForEditedUser(userAccount, selectedAccessLevels);
        userAccountRepositoryLocal.edit(userAccount);
    }

    @Override
    @PermitAll
    public AccountDetailsDto getByLogin(String login) throws SsbdApplicationException {
        this.userAccount = userAccountRepositoryLocal.findByLogin(login).orElseThrow(
                () -> new LoginDoesNotExistException("Account with login '" + login + "' does not exist."));
        Hibernate.initialize(this.userAccount.getAccountAccessLevels());

        AccountDetailsDto accountDetailsDto = modelMapper.map(this.userAccount, AccountDetailsDto.class);
        System.out.println("AccountDetails: " + accountDetailsDto);
        return accountDetailsDto;
    }

    @Override
    @RolesAllowed(MokRoles.CHANGE_OWN_PASSWORD)
    public void changePasswordByLogin(String login, String currentPassword, String newPassword) throws SsbdApplicationException {
        this.userAccount = userAccountRepositoryLocal.findByLogin(login).orElseThrow(
                () -> new LoginDoesNotExistException("Account with login '" + login + "' does not exist."));
        String currentPasswordHash = SHA256Provider.encode(currentPassword);

        if (!currentPasswordHash.equals(this.userAccount.getPassword())) {
            throw new ChangePasswordException("Current password is incorrect.");
        }

        setNewPassword(newPassword);
    }

    @Override
    @RolesAllowed(MokRoles.CHANGE_USER_PASSWORD)
    public void changePasswordById(long id, String newPassword) throws SsbdApplicationException {
        AccountDetailsDto account = this.getUserById(id);
        setNewPassword(newPassword);
    }

    @Override
    @RolesAllowed(MokRoles.LOCK_UNLOCK_ACCOUNT)
    public UserAccount updateLockStatusOnAccountById(Long id, boolean isActive) throws SsbdApplicationException {
        UserAccount account = userAccountRepositoryLocal.findById(id).orElseThrow(
                () -> new UserIdDoesNotExistException("Account with id '" + id + "' does not exist."));
        account.setAccountActive(isActive);
        UserAccount editedAccount = userAccountRepositoryLocal.editWithoutMerge(account);

        messenger.sendMessage(
                account.getEmail(),
                localization.get("bowlingAlley") + " - " + localization.get("accountStatusChanged"),
                account.isAccountActive() ? localization.get("yourAccountUnlocked") : localization.get("yourAccountLocked")
        );

        return editedAccount;
    }

    @Override
    @PermitAll
    public List<UserAccount> getAllByNameOrLastName(String name) {
        List<UserAccount> users = userAccountRepositoryLocal.findAllByNameOrLastName(name);
        for (UserAccount user : users) {
            Hibernate.initialize(user.getAccountAccessLevels());
        }
        return users;
    }

    /**
     * Ustawia prawidłowy stan dla flagi active
     * w istniejących dla użytkownika poziomach dostępu.
     */
    private void setActiveFieldForExistingAccountAccessLevelsOfEditedUser(List<AccountAccessLevel> accountAccessLevels,
                                                                          List<String> selectedAccessLevels) {
        for (AccountAccessLevel accountAccessLevel : accountAccessLevels) {
            if (selectedAccessLevels.contains(accountAccessLevel.getAccessLevel().getName())) {
                accountAccessLevel.setActive(true);
                selectedAccessLevels.remove(accountAccessLevel.getAccessLevel().getName());
            } else {
                accountAccessLevel.setActive(false);
            }
        }
    }

    /**
     * Dodaje dla użytkownika poziomy dostępu, które nie były dla niego wcześniej przydzielone.
     *
     * @param userAccount          Obiekt typu UserAccount, który jest edytowany.
     * @param selectedAccessLevels Obiekt typu List<String>, który reprezentuje zaznaczone przy edycji poziomy dostępu
     * @throws EntityRetrievalException w wypadku, gdy nie uda się pobrac poziomu dostępu.
     */
    private void addNewAccountAccessLevelsForEditedUser(UserAccount userAccount, List<String> selectedAccessLevels) throws EntityRetrievalException {
        for (String selectedAccessLevel : selectedAccessLevels) {
            AccessLevel accessLevel = accessLevelRepositoryLocal.findByName(selectedAccessLevel).orElseThrow(
                    () -> new AccessLevelDoesNotExistException("AccessLevel '" + selectedAccessLevel + "' does not exist."));
            userAccount.getAccountAccessLevels().add(AccountAccessLevel.builder()
                    .account(userAccount)
                    .accessLevel(accessLevel)
                    .active(true)
                    .build()
            );
        }
    }

    /**
     * Dopisuje aktualne hasło do historii haseł użytkownika i zmienia je.
     *
     * @param newPassword Nowe hasło dla konta.
     * @throws SsbdApplicationException w wypadku, gdy nie uda się zmienić hasła.
     */
    private void setNewPassword(String newPassword) throws SsbdApplicationException {
        String newPasswordHash = SHA256Provider.encode(newPassword);

        if (isNewPasswordUniqueForUser(this.userAccount, newPasswordHash)) {
            addCurrentPasswordToHistory(this.userAccount);
            this.userAccount.setPassword(newPasswordHash);
        } else {
            throw new AccountPasswordNotUniqueException("New password was used before.");
        }
        userAccountRepositoryLocal.edit(this.userAccount);
    }

    /**
     * Sprawdza czy nowe hasło nie było wcześniej używane przez użytkownika
     *
     * @param userAccount użytkownik dla którego sprawdzana jest unikalność hasla
     * @param newPassword nowe hasło
     * @return rezultat sprawdzenia
     */
    private boolean isNewPasswordUniqueForUser(UserAccount userAccount, String newPassword) {
        List<String> previousPasswords = userAccount.getPreviousUserPasswords().stream()
                .map(PreviousUserPassword::getPassword).collect(Collectors.toList());

        return !previousPasswords.contains(newPassword) && !userAccount.getPassword().equals(newPassword);
    }

    /**
     * Dodaje istniejące hasło użytkownika do historii haseł
     *
     * @param userAccount obiekt konta użytkownika
     */
    private void addCurrentPasswordToHistory(UserAccount userAccount) {
        PreviousUserPassword newPrevious = PreviousUserPassword.builder()
                .password(userAccount.getPassword())
                .build();
        userAccount.getPreviousUserPasswords().add(newPrevious);
    }
}
