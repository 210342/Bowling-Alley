package pl.lodz.p.it.ssbd2019.ssbd03.mor.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.lodz.p.it.ssbd2019.ssbd03.validators.EndTimeAfterStartTime;
import pl.lodz.p.it.ssbd2019.ssbd03.validators.StartDateAfterPresent;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@StartDateAfterPresent(message = "{validate.startDateBeforePresent}")
@EndTimeAfterStartTime(message = "{validate.endTimeAfterStartTime}")
public class DetailedReservationDto extends NewReservationDto {
    
    @NotNull(message = "{validate.endDateIsNull}")
    @FormParam("endDay")
    private String endDay;
    
    @FormParam("active")
    private boolean active;
    
    @FormParam("alleyNumber")
    private int alleyNumber;
    
    @FormParam("size")
    private List<@Min(1) Integer> size;
    
    @FormParam("count")
    private List<@Min(1) Integer> count;
}
