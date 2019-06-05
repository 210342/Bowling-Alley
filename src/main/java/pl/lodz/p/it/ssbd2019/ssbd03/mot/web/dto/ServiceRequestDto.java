package pl.lodz.p.it.ssbd2019.ssbd03.mot.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.FormParam;

@Data
@NoArgsConstructor
public class ServiceRequestDto {
    @NotNull(message = "{validate.serviceRequestNotNull}")
    @Size(max = 256, message = "{validate.serviceRequestLength}")
    @FormParam("content")
    private String content;
}
