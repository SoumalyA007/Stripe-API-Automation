package builders.requestbuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import enums.Dashboard;
import enums.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;


@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequestPayload {

    private String contact_email;
    private String display_name;
    private Identity identity;
    private Configuration configuration;
    private Defaults defaults;
    private Dashboard dashboard;
    private List<String> include;

}


