package pl.lodz.p.it.ssbd2019.ssbd03.web.accountsmodule;

import javax.enterprise.context.RequestScoped;
import javax.mvc.Controller;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Controller
@Path("login")
public class LoginController {
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String loginPage() {
        return "accounts/login/login.html";
    }
}
