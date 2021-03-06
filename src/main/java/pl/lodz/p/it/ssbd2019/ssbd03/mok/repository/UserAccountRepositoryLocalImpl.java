package pl.lodz.p.it.ssbd2019.ssbd03.mok.repository;

import org.postgresql.util.PSQLException;
import pl.lodz.p.it.ssbd2019.ssbd03.entities.UserAccount;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.entity.*;
import pl.lodz.p.it.ssbd2019.ssbd03.repository.AbstractCruRepository;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.roles.MokRoles;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

@TransactionAttribute(TransactionAttributeType.MANDATORY)
@Stateless(name = "MOKUserRepository")
@DenyAll
public class UserAccountRepositoryLocalImpl extends AbstractCruRepository<UserAccount, Long> implements UserAccountRepositoryLocal {
    @PersistenceContext(unitName = "ssbd03mokPU")
    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    protected Class<UserAccount> getTypeParameterClass() {
        return UserAccount.class;
    }

    @Override
    @PermitAll
    public Optional<UserAccount> findByLogin(String login) throws DataAccessException {
        try {
            TypedQuery<UserAccount> namedQuery = this.createNamedQuery("UserAccount.findByLogin");
            namedQuery.setParameter("login", login);
            return Optional.of(namedQuery.getSingleResult());
        } catch (PersistenceException e) {
            throw new LoginDoesNotExistException("Could not find entity with given login", e);
        }
    }


    @Override
    @PermitAll
    public Optional<UserAccount> findByEmail(String email) {
        try {
            TypedQuery<UserAccount> namedQuery = this.createNamedQuery("UserAccount.findByEmail");
            namedQuery.setParameter("email", email);
            return Optional.of(namedQuery.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    @PermitAll
    public List<UserAccount> findAllByNameOrLastName(String name) {
        TypedQuery<UserAccount> namedQuery = this.createNamedQuery("UserAccount.filterByName");
        namedQuery.setParameter("name", "%" + name.toLowerCase() + "%");
        return namedQuery.getResultList();
    }

    @Override
    @PermitAll
    public Optional<UserAccount> findById(Long id) throws DataAccessException {
        return super.findById(id);
    }

    @Override
    @PermitAll
    public UserAccount create(UserAccount userAccount) throws DataAccessException {
        try {
            return super.create(userAccount);
        } catch (ConstraintViolationException e) {
            if(e.getMessage().contains("phone")) {
                throw new PhoneLengthConstraintViolationException("Given phone length: " + userAccount.getPhone().length()
                + " is invalid.");
            }
            throw new DatabaseConstraintViolationException("Violated constraint during user creation", e);
        } catch (PersistenceException e) {
            Throwable t = e.getCause();
            if(t!= null && (t.getCause() instanceof PSQLException)) {
                String message = t.getCause().getMessage();
                if (message.contains("email")) {
                    throw new NotUniqueEmailException("Could not create account with email '" + userAccount.getEmail() +
                            "' because it was already in use.", e);
                } else if (message.contains("login")) {
                    throw new NotUniqueLoginException("Could not create account with login '" + userAccount.getLogin() +
                            "' because it was already in use.", e);
                }
            }
            throw new EntityUpdateException("Could not perform create operation.", e);
        }
    }

    @Override
    @RolesAllowed({MokRoles.EDIT_USER_ACCOUNT, MokRoles.EDIT_OWN_ACCOUNT, MokRoles.CHANGE_ACCESS_LEVEL,
            MokRoles.CHANGE_OWN_PASSWORD, MokRoles.CHANGE_USER_PASSWORD})
    public void edit(UserAccount userAccount) throws DataAccessException {
        try {
            super.edit(userAccount);
        } catch (OptimisticLockException e) {
            throw new UserAccountOptimisticLockException("Account has been updated before these changes were made", e);
        } catch (ConstraintViolationException e) {
            if(e.getMessage().contains("phone")) {
                throw new PhoneLengthConstraintViolationException("Given phone length: " + userAccount.getPhone().length()
                        + " is invalid.");
            }
            throw new DatabaseConstraintViolationException("Violated constraint during user editing", e);
        } catch (PersistenceException e) {
            Throwable t = e.getCause();
            if(t!= null && (t.getCause() instanceof PSQLException)) {
                String message = t.getCause().getMessage();
                if (message.contains("email")) {
                    throw new NotUniqueEmailException("Could not update email to '" + userAccount.getEmail() +
                            "' because it was already in use.", e);
                }
            }
            throw new EntityUpdateException("Could not perform update operation.", e);
        }
    }

    @Override
    @PermitAll
    public UserAccount editWithoutMerge(UserAccount userAccount) throws DataAccessException {
        try {
            return super.editWithoutMerge(userAccount);
        } catch (OptimisticLockException e) {
            throw new UserAccountOptimisticLockException("Account has been updated before these changes were made", e);
        } catch (ConstraintViolationException e) {
            if(e.getMessage().contains("phone")) {
                throw new PhoneLengthConstraintViolationException("Given phone length: " + userAccount.getPhone().length()
                        + " is invalid.");
            }
            throw new DatabaseConstraintViolationException("Violated constraint during user editing", e);
        } catch (PersistenceException e) {
            Throwable t = e.getCause();
            if(t!= null && (t.getCause() instanceof PSQLException)) {
                String message = t.getCause().getMessage();
                if (message.contains("email")) {
                    throw new NotUniqueEmailException("Could not update email to '" + userAccount.getEmail() +
                            "' because it was already in use.", e);
                }
            }
            throw new EntityUpdateException("Could not perform update operation.", e);
        }
    }

    @Override
    @RolesAllowed(MokRoles.GET_ALL_USERS_LIST)
    public List<UserAccount> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserAccount> query = builder.createQuery(UserAccount.class);
        Root<UserAccount> root = query.from(UserAccount.class);
        query.orderBy(builder.asc(root.get("login")));
        return entityManager.createQuery(query).getResultList();
    }
}
