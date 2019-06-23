package pl.lodz.p.it.ssbd2019.ssbd03.mor.web;

import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.SsbdApplicationException;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.generalized.NotYourReservationException;
import pl.lodz.p.it.ssbd2019.ssbd03.mor.service.ReservationItemService;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SessionScoped
@Controller
@Path("myreservations")
public class ReservationController implements Serializable {

    private static final String ERROR = "errors";
    private static final String DATA = "data";
    private static final String USERNAME = "userName";
    
    private static final String RESERVATION_LIST_VIEW = "mor/reservationList.hbs";
    private static final String RESERVATION_LIST_URL = "/myreservations";
    
    private static final String RESERVATION_VIEW = "mor/reservation.hbs";
    
    private static final String NEW_RESERVATION_VIEW = "mor/newReservation.hbs";
    
    private static final String NEW_RESERVATION_URL = "/myreservations/new";
    private static final String RESERVATION_DETAILS_PATH = "/myreservations/details/";
    
    private static final String EDIT_RESERVATION_VIEW = "mor/editReservationForm.hbs";
    private static final String EDIT_OWN_RESERVATION_URL = "myreservations/edit/";
    
    @Inject
    private Models models;

    @EJB(beanName = "MORReservationService")
    private ReservationService reservationService;
    
    @EJB(beanName = "MORReservationItemService")
    private ReservationItemService reservationItemService;

    @Inject
    private RedirectUtil redirectUtil;

    @Inject
    private LocalizedMessageProvider localization;

    @Inject
    private DtoValidator validator;
    
    private transient NewReservationDto newReservationDto;

    /**
     * Pobiera widok pozwalający klientowi przejrzeć własne rezerwacje
     *
     * @return Widok z listą rezerwacji.
     */
    @GET
    @RolesAllowed(MorRoles.GET_OWN_RESERVATIONS)
    @Produces(MediaType.TEXT_HTML)
    public String getOwnReservations() {
        try {
            String login = (String) models.get(USERNAME);
            List<ReservationFullDto> reservations = reservationService.getReservationsByUserLogin(login);
            models.put("reservationsList", reservations);
            models.put("reservationListHeading", localization.get("ownReservationList"));
        } catch (SsbdApplicationException e) {
            displayError(localization.get("reservationListError"));
        }
        return RESERVATION_LIST_VIEW;
    }

    /**
     * Pobiera widok pozwalający klientowi stworzyć rezerwację.
     *
     * @return Widok z formularzem.
     */
    @GET
    @Path("/new")
    @RolesAllowed(MorRoles.CREATE_RESERVATION)
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
    @Path("/new")
    @RolesAllowed(MorRoles.CREATE_RESERVATION)
    @Produces(MediaType.TEXT_HTML)
    public String getAvailableAlleys(@BeanParam NewReservationDto newReservationDto) {
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
            FormData formData = FormData.builder().data(newReservationAllForm).build();
            return redirectUtil.redirect(NEW_RESERVATION_URL, formData);
        } catch (SsbdApplicationException e) {
            return redirectUtil.redirectError(NEW_RESERVATION_URL, newReservationAllForm, Collections.singletonList(localization.get(e.getCode())));
        }
    }

    /**
     * Tworzy rezerwacje
     *
     * @param alleyId identyfikator toru, na którym użytkownik chce utworzyć rezerwację
     * @return informacja o wyniku rezerwacji
     */
    @GET
    @Path("new/{alley_id}")
    @RolesAllowed(MorRoles.CREATE_RESERVATION)
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
            String login = (String) models.get(USERNAME);
            reservationService.addReservation(newReservationDto, alleyId, login);
            formData.setInfos(Collections.singletonList(localization.get("newReservationCreated")));
            return redirectUtil.redirect(NEW_RESERVATION_URL, formData);
        } catch (SsbdApplicationException e) {
            formData.setErrors(Collections.singletonList(localization.get(e.getCode())));
            return redirectUtil.redirect(NEW_RESERVATION_URL, formData);
        }
    }


    /**
     * Pobiera widok pozwalający klientowi edytować własną rezerwację
     *
     * @param id identyfikator rezerwacji
     * @param idCache identyfikator cache
     * @return Widok z formularzem edycji lub widok listy własnej rezerwacji, gdy nie udało się znaleźć rezerwacji o podanym id
     */
    @GET
    @Path("/edit/{id}")
    @RolesAllowed(MorRoles.EDIT_OWN_RESERVATION)
    @Produces(MediaType.TEXT_HTML)
    public String editReservation(@PathParam("id") long id, @QueryParam("idCache") Long idCache) {
        redirectUtil.injectFormDataToModels(idCache, models);
        if (models.get(DATA) == null) {
            try {
                DetailedReservationDto dto = reservationService.getOwnReservationById(id, (String) models.get(USERNAME));
                List<ReservationItemDto> items = reservationItemService.getItemsForReservation(id);
                models.put(DATA, dto);
                models.put("items", items);
            } catch (SsbdApplicationException e) {
                return redirectUtil.redirectError(RESERVATION_LIST_URL, null, Collections.singletonList(localization.get(e.getCode())));
            }
        }
        return EDIT_RESERVATION_VIEW;
    }
    
    /**
     * Edytuje rezerwację o podanym id
     *
     * @param id identyfikator rezerwacji
     * @param reservation dto reprezentujące rezerwację
     * @return widok ukończenia operacji lub widok listy własnej rezerwacji, gdy nie udało się znaleźć rezerwacji o podanym id
     */
    @POST
    @Path("/edit/{id}")
    @RolesAllowed(MorRoles.EDIT_OWN_RESERVATION)
    @Produces(MediaType.TEXT_HTML)
    public String editReservation(
            @PathParam("id") long id,
            @BeanParam DetailedReservationDto reservation) {
        List<String> errorMessages = validator.validate(reservation);
    
        if (!errorMessages.isEmpty()) {
            return redirectUtil.redirectError(
                    EDIT_OWN_RESERVATION_URL + id,
                    reservation,
                    errorMessages);
        }
    
        System.out.println(reservation.toString());
        
        try {
            DetailedReservationDto resultDto = reservationService.updateReservation(reservation, (String) models.get(USERNAME));
            FormData formData = FormData.builder()
                    .data(resultDto)
                    .infos(Collections.singletonList(localization.get("reservationUpdated")))
                    .build();
            return redirectUtil.redirect(RESERVATION_LIST_URL , formData);
        } catch (NotYourReservationException e) {
            return redirectUtil.redirectError(RESERVATION_LIST_URL, null, Collections.singletonList(localization.get(e.getCode())));
        } catch (SsbdApplicationException e) {
            return redirectUtil.redirectError(EDIT_OWN_RESERVATION_URL + id, reservation, Collections.singletonList(localization.get(e.getCode())));
        }
    }

    /**
     * Pobiera widok pozwalający klientowi przejrzeć szegóły własnej rezerwacji
     *
     * @param reservationId identyfikator rezerwacji
     * @param idCache       opcjonalny identyfikator do obsługi przekierowań
     * @return Widok z rezultatem.
     */
    @GET
    @Path("details/{id}")
    @RolesAllowed(MorRoles.GET_OWN_RESERVATION_DETAILS)
    @Produces(MediaType.TEXT_HTML)
    public String getOwnReservationDetails(@PathParam("id") Long reservationId,
                                           @QueryParam("idCache") Long idCache) {
        redirectUtil.injectFormDataToModels(idCache, models);
        String login = (String) models.get(USERNAME);
        try {
            ReservationFullDto reservation = reservationService.getUserReservationById(reservationId, login);
            boolean isExpired = ReservationValidator.isExpired(reservation.getStartDate());
            Boolean isCancelable = !isExpired && reservation.isActive();
            models.put("reservation", reservation);
            models.put("isExpired", isExpired);
            models.put("isCancelable", isCancelable);
        } catch (SsbdApplicationException e) {
            displayError(localization.get(e.getCode()));
        }
        return RESERVATION_VIEW;
    }

    /**
     * Pozwala klientowi anulować własną rezerwację
     *
     * @param reservationId identyfikator rezerwacji
     * @return rezulat operacji
     */
    @POST
    @Path("details/{id}")
    @RolesAllowed(MorRoles.CANCEL_OWN_RESERVATION)
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
     *
     * @param
     */
    @POST
    @Path("available-alleys")
    @RolesAllowed({MorRoles.EDIT_OWN_RESERVATION, MorRoles.CREATE_RESERVATION})
    @Produces(MediaType.TEXT_HTML)
    public String injectAvailableAlleys(
            @BeanParam DetailedReservationDto dto,
            @QueryParam("redirectTo") String redirectTo,
            @QueryParam("resId") Long reservationId) {
        
        try {
            models.put(
                    "availableAlleyNumbers",
                    reservationService.getAvailableAlleysInTimeRange(dto)
                            .stream()
                            .map(AvailableAlleyDto::getAlleyNumber)
                            .collect(Collectors.toList())
            );
            models.put(DATA, dto);
            
            switch (redirectTo) {
                case "create":
                    return getAvailableAlleys((Long) null);
                case "update":
                    return editReservation(reservationId, (Long) null);
                default:
                    return getOwnReservations();
            }
        } catch (SsbdApplicationException e) {
            return redirectUtil.redirectError(RESERVATION_LIST_URL, null, Collections.singletonList(localization.get(e.getCode())));
        }
    }
    
    private void displayError(String s) {
        models.put(ERROR, Collections.singletonList(s));
    }

}
