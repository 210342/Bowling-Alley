package pl.lodz.p.it.ssbd2019.ssbd03.mok.web.controller.register;

import pl.lodz.p.it.ssbd2019.ssbd03.entities.UserAccount;
import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.SsbdApplicationException;
import pl.lodz.p.it.ssbd2019.ssbd03.mok.service.RegistrationService;
import pl.lodz.p.it.ssbd2019.ssbd03.mok.web.dto.BasicAccountDto;
import pl.lodz.p.it.ssbd2019.ssbd03.mok.web.dto.validators.DtoValidator;
import pl.lodz.p.it.ssbd2019.ssbd03.mok.web.dto.validators.PasswordDtoValidator;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.localization.LocalizedMessageProvider;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.redirect.RedirectUtil;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.mvc.Models;
import java.util.ArrayList;
import java.util.List;

public abstract class RegistrationController {

    @Inject
    protected Models models;

    @Inject
    protected RedirectUtil redirectUtil;

    @Inject
    protected LocalizedMessageProvider localization;

    @Inject
    private PasswordDtoValidator passwordValidator;

    @Inject
    private DtoValidator validator;

    @EJB
    private RegistrationService registrationService;

    protected List<String> errorMessages = new ArrayList<>();

    private static final String ERROR_PREFIX = "errors";

    /**
     * Metoda pomocnicza do uniknięcia duplikowania kodu
     * @param basicAccountDto DTO przechowujące dane formularza rejestracji.
     * @param accessLevelNames poziomy dostepu konta
     * @return Widok potwierdzający rejestrację bądź błąd rejestracji
     */
    String registerAccount(BasicAccountDto basicAccountDto, List<String> accessLevelNames, boolean isConfirmed) {
        models.put("data", basicAccountDto);
        errorMessages.addAll(validator.validate(basicAccountDto));
        errorMessages.addAll(passwordValidator.validatePassword(basicAccountDto.getPassword(), basicAccountDto.getConfirmPassword()));

        if (!errorMessages.isEmpty()) {
            return redirectUtil.redirectError(getRegisterEndpointUrl(), basicAccountDto, errorMessages);
        }

        UserAccount userAccount = UserAccount
                .builder()
                .login(basicAccountDto.getLogin())
                .password(basicAccountDto.getPassword())
                .accountConfirmed(isConfirmed)
                .accountActive(true)
                .email(basicAccountDto.getEmail())
                .firstName(basicAccountDto.getFirstName())
                .lastName(basicAccountDto.getLastName())
                .phone(basicAccountDto.getPhoneNumber())
                .version(0L) // TODO It's workaround for the bug.
                .build();
            try {
                registrationService.registerAccount(userAccount, accessLevelNames);
            } catch (SsbdApplicationException e) {
                errorMessages.add(e.getMessage());
            }

        if (!errorMessages.isEmpty()) {
            return redirectUtil.redirectError(getRegisterEndpointUrl(), basicAccountDto, errorMessages);
        }

        return String.format("redirect:%s/success", getRegisterEndpointUrl());
    }

    /**
     * Metoda pomocnicza pozwalająca uzyskać url do zwracanego widoku rejestracji
     * @return String url
     */
    protected abstract String getRegisterEndpointUrl();

    /**
     * Metoda pomocnicza pozwalająca uzyskać url do zwracanego widoku suckesu.
     * @return String url
     */
    protected abstract String getSuccessViewUrl();
}