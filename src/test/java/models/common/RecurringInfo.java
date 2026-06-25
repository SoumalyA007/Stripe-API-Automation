package models.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the recurring billing configuration for a Stripe Price object.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecurringInfo {

    @JsonProperty("interval")
    private String interval;

    @JsonProperty("interval_count")
    private Integer intervalCount;

    @JsonProperty("usage_type")
    private String usageType;

    @JsonProperty("trial_period_days")
    private Integer trialPeriodDays;

    @JsonProperty("aggregate_usage")
    private String aggregateUsage;
}
