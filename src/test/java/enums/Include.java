package enums;

public enum Include {


    CONFIGURATION_CUSTOMER("configuration.customer"),
    CONFIGURATION_MERCHANT("configuration.merchant"),
    CONFIGURATION_RECIPIENT("configuration.recipient"),
    CONFIGURATION_STORER("configuration.storer"),
    DEFAULTS("defaults"),
    FUTURE_REQUIREMENTS("future_requirements"),
    IDENTITY("identity"),
    REQUIREMENTS("requirements");

    private final String value;

    Include(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
