package pl.lodz.p.it.ssbd2019.ssbd03.web.servlets.filters;

import pl.lodz.p.it.ssbd2019.ssbd03.exceptions.generalized.PropertiesLoadException;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.configuration.i18n.context.LanguageContext;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.configuration.i18n.LanguageMapFactory;
import pl.lodz.p.it.ssbd2019.ssbd03.utils.configuration.i18n.context.LocaleConfig;

import javax.inject.Inject;
import javax.mvc.Models;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Klasa odpowiedzialna za dodawanie elementów strony, które reprezentują treść dla zadanego jezyka, oraz zmianę języka.
 * Wysyła do obiektu klasy LanguageMapFactory zapytanie o mapę powiązań.
 * @see LanguageMapFactory
 * @see LanguageContext
 */
@WebFilter(value = "/*", dispatcherTypes = {DispatcherType.ERROR, DispatcherType.REQUEST, DispatcherType.FORWARD})
public class LanguageFilter extends HttpFilter {

    @Inject
    private LanguageContext languageContext;

    @Inject
    private LanguageMapFactory i18nManager;

    @Inject
    private Models models;

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Locale locale = request.getLocale();
        LocaleConfig localeConfig = languageContext.getLocaleConfig(locale.getLanguage());
        Hashtable<Object, Object> langMap = localeCheckSet(localeConfig);
        models.put("lang", langMap);
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
        chain.doFilter(request, response);
    }

    private Hashtable<Object, Object> localeCheckSet(LocaleConfig localeConfig) throws IOException {
        try {
            if (languageContext.isPreffered()) {
                return i18nManager.getLanguageMap(languageContext.getCurrent());
            }
            languageContext.setCurrent(localeConfig);
            return i18nManager.getLanguageMap(localeConfig);
        } catch (PropertiesLoadException e) {
            throw new IOException("Could not load properties", e);
        }
    }
}
