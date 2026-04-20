package builders.requestbuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import enums.DefaultsResponsibilitiesFeesCollector;
import enums.DefaultsResponsibilitiesLossesCollector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class Responsibilities {

    private DefaultsResponsibilitiesFeesCollector fees_collector;
    private DefaultsResponsibilitiesLossesCollector losses_collector;

}
