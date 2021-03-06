package pl.lodz.p.it.ssbd2019.ssbd03.mot.service;

import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.SsbdApplicationException;
import pl.lodz.p.it.ssbd2019.ssbd03.mot.web.dto.ReservationFullDto;

import java.util.List;

public interface ReservationService {

    /**
     * Pobiera wszystkie zakończone rezerwacje dla podanego toru
     * wraz z wynikami.
     *
     * @param alleyId identyfikator toru
     * @return Lista rezerwacji dla danego toru
     */
    List<ReservationFullDto> getFinishedReservationsForAlley(Long alleyId) throws SsbdApplicationException;
}
