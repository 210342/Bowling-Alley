package pl.lodz.p.it.ssbd2019.ssbd03.mor.web;

import pl.lodz.p.it.ssbd2019.ssbd03.entities.Reservation;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.SsbdApplicationException;
import pl.lodz.p.it.ssbd2019.ssbd03.mor.service.ReservationService;
import pl.lodz.p.it.ssbd2019.ssbd03.mor.web.dto.*;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.DtoValidator;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.helpers.ReservationValidator;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.localization.LocalizedMessageProvider;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.redirect.FormData;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.redirect.RedirectUtil;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.roles.MorRoles;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.mvc.Controller;
import javax.mvc.Models;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SessionScoped
@Controller
@Path("reservations")
public class EmployeeReservationController implements Serializable {

    private static final String ERROR = "errors";
    private static final String RESERVATION_VIEW = "mor/reservation.hbs";
    private static final String RESERVATION_LIST_VIEW = "mor/reservationList.hbs";
    private static final String RESERVATION_DETAILS_PATH = "reservations/details/";
    private static final String NEW_RESERVATION_VIEW = "mor/newReservation/employeeNewReservation.hbs";
    private static final String NEW_RESERVATION_URL = "/reservations/new";

    @EJB(beanName = "MORReservationService")
    private ReservationService reservationService;

    @Inject
    private Models models;

    @Inject
    private LocalizedMessageProvider localization;

    @Inject
    private RedirectUtil redirectUtil;

    @Inject
    private DtoValidator validator;

    private transient EmployeeNewReservationDto newReservationDto;

    /**
     * Pobiera widok pozwalający klientowi stworzyć rezerwację.
     *
     * @return Widok z formularzem.
     */
    @GET
    @Path("new")
    @RolesAllowed(MorRoles.CREATE_RESERVATION_FOR_USER)
    @Produces(MediaType.TEXT_HTML)
    public String getAvailableAlleys(@QueryParam("idCache") Long idCache) {
        redirectUtil.injectFormDataToModels(idCache, models);
        return NEW_RESERVATION_VIEW;
    }

    /**
     * Pobiera dostępne tory w zadanym przedziale czasowym.
     *
     * @param newReservationDto dane rezerwacji
     * @return widok z dostępnymi torami
     */
    @POST
    @Path("new")
    @RolesAllowed(MorRoles.CREATE_RESERVATION_FOR_USER)
    @Produces(MediaType.TEXT_HTML)
    public String getAvailableAlleys(@BeanParam EmployeeNewReservationDto newReservationDto) {
        List<String> errorMessages = validator.validate(newReservationDto);

        NewReservationAllForm newReservationAllForm = new NewReservationAllForm();
        newReservationAllForm.setNewReservationDto(newReservationDto);

        if (!errorMessages.isEmpty()) {
            return redirectUtil.redirectError(NEW_RESERVATION_URL, newReservationAllForm, errorMessages);
        }

        try {
            List<AvailableAlleyDto> availableAlleys = reservationService.getAvailableAlleysInTimeRange(newReservationDto);
            this.newReservationDto = newReservationDto;

            newReservationAllForm.setAvailableAlleys(availableAlleys);
            newReservationAllForm.setSelfUrl(NEW_RESERVATION_URL);
            FormData formData = FormData.builder().data(newReservationAllForm).build();
            return redirectUtil.redirect(NEW_RESERVATION_URL, formData);
        } catch (SsbdApplicationException e) {
            return redirectUtil.redirectError(NEW_RESERVATION_URL, newReservationAllForm, Arrays.asList(localization.get(e.getCode())));
        }
    }

    /**
     * Tworzy rezerwacje
     *
     * @param alleyId
     * @return informacja o wyniku rezerwacji
     */
    @GET
    @Path("new/{alley_id}")
    @RolesAllowed(MorRoles.CREATE_RESERVATION_FOR_USER)
    @Produces(MediaType.TEXT_HTML)
    public String createReservation(@PathParam("alley_id") Long alleyId) {
        if (newReservationDto == null) {
            return redirectUtil.redirect(NEW_RESERVATION_URL, new FormData());
        }

        List<String> errorMessages = validator.validate(newReservationDto);

        NewReservationAllForm newReservationAllForm = new NewReservationAllForm();
        newReservationAllForm.setNewReservationDto(newReservationDto);
        if (!errorMessages.isEmpty()) {
            return redirectUtil.redirectError(NEW_RESERVATION_URL, newReservationAllForm, errorMessages);
        }

        FormData formData = new FormData();
        formData.setData(newReservationAllForm);
        try {
            reservationService.addReservation(newReservationDto, alleyId, newReservationDto.getUserLogin());
            formData.setInfos(Collections.singletonList(localization.get("newReservationCreated")));
            return redirectUtil.redirect(NEW_RESERVATION_URL, formData);
        } catch (SsbdApplicationException e) {
            formData.setErrors(Collections.singletonList(localization.get(e.getCode())));
            return redirectUtil.redirect(NEW_RESERVATION_URL, formData);
        }
    }

    /**
     * Pobiera widok pozwalający pracownikowi edytować własną rezerwację
     *
     * @param id identyfikator edytowanej rezerwacji
     * @return Widok z formularzem.
     */
    @GET
    @Path("{id}/edit")
    @RolesAllowed(MorRoles.EDIT_RESERVATION_FOR_USER)
    @Produces(MediaType.TEXT_HTML)
    public String editReservation(@PathParam("id") Long id) {
        throw new UnsupportedOperationException();
    }

    /**
     * Pobiera rezerwacje wybranego klienta
     *
     * Scenariusz:
     *     1) Użytkownik jest zalogowany na koncie z rolą "Employee" lub "Admin".
     *     2) System wyświetla listę Clientów
     *     3) Użytkownik wyszukuje Clienta na liście
     *     4) Użytkownik przechodzi na listę rezerwacji Clienta klikając przycisk "Rezerwacje" na pozycji listy
     *     5) System wyświetla listę rezerwacji Clienta, lista może być pusta.
     *
     * @param id identyfikator klienta
     * @return Widok z rezultatem.
     */
    @GET
    @Path("user/{id}")
    @RolesAllowed(MorRoles.GET_RESERVATIONS_FOR_USER)
    @Produces(MediaType.TEXT_HTML)
    public String getReservationsForUser(@PathParam("id") Long id) {
        try {
            List<ReservationFullDto> reservations = reservationService.getReservationsForUser(id);
            models.put("reservationsList", reservations);
            models.put("reservationListHeading", localization.get("userReservationList"));
            models.put("reservationContext", "reservations");
        } catch (SsbdApplicationException e) {
            displayError(localization.get("reservationListError"));
        }
        return RESERVATION_LIST_VIEW;
    }

    /**
     * Pobiera rezerwacje wybranego toru
     *
     * 1. Użytkownik jest zalogowany na koncie z rolą "Employee".
     * 2. System wyświetla listę torów
     * 3. Użytkownik klika przycisk "Pokaż rezerwację"
     * 4. System wyświetla listę rezerwacji powiązaną z torem
     *
     * @param id identyfikator toru
     * @return Widok z rezultatem.
     */
    @GET
    @Path("alley/{id}")
    @RolesAllowed(MorRoles.GET_RESERVATIONS_FOR_ALLEY)
    @Produces(MediaType.TEXT_HTML)
    public String getReservationsForAlley(@PathParam("id") Long id) {
        try {
            List<ReservationFullDto> reservations = reservationService.getReservationsForAlley(id);
            models.put("reservationsList", reservations);
            models.put("reservationListHeading", localization.get("alleyReservationList"));
            models.put("reservationContext", "reservations");
        } catch (SsbdApplicationException e) {
            displayError(localization.get("reservationListError"));
        }
        return RESERVATION_LIST_VIEW;
    }

    /**
     * Pobiera widok pozwalający pracownikowi przejrzeć szegóły wybranej rezererwacji
     *
     * 1. Użytkownik jest zalogowany na koncie z rolą "Employee" lub "Admin"
     * 2. Użytkownik przechodzi na listę rezerwacji
     * 3. Użytkownik klika "Pokaż szczegóły"
     * 4. System wyświetla szczegóły rezerwacji
     *
     * @param reservationId identyfikator rezerwacji
     * @param idCache       opcjonalny identyfikator do obsługi przekierowań
     * @return Widok z rezultatem.
     */
    @GET
    @Path("details/{id}")
    @RolesAllowed(MorRoles.GET_RESERVATION_DETAILS)
    @Produces(MediaType.TEXT_HTML)
    public String getReservationDetails(@PathParam("id") Long reservationId, @QueryParam("idCache") Long idCache) {
        redirectUtil.injectFormDataToModels(idCache, models);
        try {
            ReservationFullDto reservation = reservationService.getReservationById(reservationId);
            boolean isExpired = ReservationValidator.isExpired(reservation.getStartDate());
            Boolean isCancelable = !isExpired && reservation.isActive();
            models.put("reservation", reservation);
            models.put("isExpired", isExpired);
            models.put("isCancelable", isCancelable);
            models.put("ownReservation", false);
        } catch (SsbdApplicationException e) {
            displayError(localization.get(e.getCode()));
        }

        return RESERVATION_VIEW;
    }

    /**
     * Pozwala pracownikowi anulować rezerwację
     *
     * @param reservationId identyfikator rezerwacji
     * @return rezulat operacji
     */
    @POST
    @Path("details/{id}")
    @RolesAllowed(MorRoles.CANCEL_RESERVATION_FOR_USER)
    @Produces(MediaType.TEXT_HTML)
    public String cancelReservation(@PathParam("id") Long reservationId) {
        try {
            reservationService.cancelReservation(reservationId);
            FormData formData = new FormData();
            String message = localization.get("reservationCancelSuccess");
            formData.setInfos(Collections.singletonList(message));
            return redirectUtil.redirect(RESERVATION_DETAILS_PATH + reservationId, formData);
        } catch (SsbdApplicationException e) {
            return redirectUtil.redirectError(
                    RESERVATION_DETAILS_PATH + reservationId,
                    null,
                    Collections.singletonList(localization.get(e.getCode()))
            );
        }
    }

    /**
     * Aktualizuje rezerwację
     *
     * @param reservation obiekt zaktualizowanej rezerwacji
     * @return rezultat operacji
     */
    @POST
    @Path("{id}/edit")
    @RolesAllowed(MorRoles.EDIT_RESERVATION_FOR_USER)
    @Produces(MediaType.TEXT_HTML)
    public String editReservation(Reservation reservation) {
        throw new UnsupportedOperationException();
    }

    /**
     * edytuje wybrany komentarz do rezerwacji
     *
     * @param id wybrana komentarza
     * @return Widok z rezultatem.
     */
    @POST
    @Path("{id}/details/edit")
    @RolesAllowed(MorRoles.EDIT_COMMENT_FOR_RESERVATION)
    @Produces(MediaType.TEXT_HTML)
    public String editCommentForReservation(Long id) {
        throw new UnsupportedOperationException();
    }

    /**
     * Blokuje wybrany komentarz do rezerwacji
     *
     * 1. Użytkownik jest zalogowany na koncie z rolą "Admin" lub "Employee".
     * 2. Użytkownik przechodzi do szczegółów rezerwacji.
     * 3. System wyświetla szczegóły wybranej rezerwacji.
     * 4. Użytkownik klika "zablokuj" na pozycji komentarza.
     * 5. System zapisuje stan komentarza jako zablokowany.
     *
     * @param id wybrana komentarza
     * @return Widok z rezultatem.
     */
    @POST
    @Path("details/disable-comment/{id}")
    @RolesAllowed(MorRoles.DISABLE_COMMENT)
    @Produces(MediaType.TEXT_HTML)
    public String disableComment(@FormParam("reservationId") Long reservationId, @PathParam("id") Long id) {
        try {
            reservationService.disableComment(id);
            FormData formData = new FormData();
            String message = localization.get("commentDisabled");
            formData.setInfos(Collections.singletonList(message));
            return redirectUtil.redirect(RESERVATION_DETAILS_PATH + reservationId, formData);
        } catch (SsbdApplicationException e) {
            return redirectUtil.redirectError(
                    RESERVATION_DETAILS_PATH + reservationId,
                    null,
                    Collections.singletonList(localization.get(e.getCode()))
            );
        }
    }

    private void displayError(String s) {
        models.put(ERROR, Collections.singletonList(s));
    }

}
