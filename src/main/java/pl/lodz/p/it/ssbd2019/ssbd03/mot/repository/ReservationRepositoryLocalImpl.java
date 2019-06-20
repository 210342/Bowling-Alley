package pl.lodz.p.it.ssbd2019.ssbd03.mot.repository;

import pl.lodz.p.it.ssbd2019.ssbd03.entities.Reservation;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.entity.DataAccessException;
import pl.lodz.p.it.ssbd2019.ssbd03.repository.AbstractCruRepository;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.roles.MorRoles;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.roles.MotRoles;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionRolledbackLocalException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless(name = "MOTReservationRepository")
@TransactionAttribute(TransactionAttributeType.MANDATORY)
@DenyAll
public class ReservationRepositoryLocalImpl extends AbstractCruRepository<Reservation, Long> implements ReservationRepositoryLocal {

    @PersistenceContext(unitName = "ssbd03motPU")
    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    protected Class<Reservation> getTypeParameterClass() {
        return Reservation.class;
    }

    @Override
    @RolesAllowed(MotRoles.ENTER_GAME_RESULT)
    public void edit(Reservation reservation) throws DataAccessException {
        super.edit(reservation);
    }

    @Override
    @RolesAllowed({MorRoles.GET_RESERVATIONS_FOR_ALLEY})
    public List<Reservation> findReservationsForAlley(Long alleyId) throws DataAccessException {
        try {
            TypedQuery<Reservation> namedQuery = this.createNamedQuery("Reservation.findReservationsForAlley");
            namedQuery.setParameter("alleyId", alleyId);
            return namedQuery.getResultList();
        } catch (TransactionRolledbackLocalException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
