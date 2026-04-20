package builders.requestbuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String dashboard;
    private List<String> include;

}


