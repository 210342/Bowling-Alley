package pl.lodz.p.it.ssbd2019.ssbd03.accountsmodule.web;

import pl.lodz.p.it.ssbd2019.ssbd03.accountsmodule.web.dto.AdminEditPasswordDto;
import pl.lodz.p.it.ssbd2019.ssbd03.accountsmodule.web.dto.BasicAccountDto;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.mvc.Models;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApplicationScoped
class DtoValidator {

    @Inject
    private Validator validator;



    public List<String> validatePasswordEquality(String password, String confirmPassword) {
        List<String> errors = new ArrayList<>();
        if (!password.equals(confirmPassword)) {
            errors.add("Passwords don't match.");
        }

        return errors;
    }
    public <T> List<String> validate(T dto) {
        List<String> errors = new ArrayList<>();
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        for (ConstraintViolation<T> violation : violations) {
            errors.add(violation.getMessage());
        }
        return errors;
    }
}
