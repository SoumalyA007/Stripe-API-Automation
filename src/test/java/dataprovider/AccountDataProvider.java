package dataprovider;

import org.testng.annotations.DataProvider;

import enums.Dashboard;
import enums.DefaultsResponsibilitiesFeesCollector;
import enums.DefaultsResponsibilitiesLossesCollector;
import enums.IdentityEntity;
import helpers.AccountsHelper;

public class AccountDataProvider {

    // ============== POSITIVE: Different Entity Types ==============

    @DataProvider(name = "validEntityTypes")
    public Object[][] validEntityTypes() {
        return new Object[][] {
                { "Company Entity", AccountsHelper.accountWithEntityType(IdentityEntity.company) },
                { "Individual Entity", AccountsHelper.accountWithEntityType(IdentityEntity.individual) },
                { "Non-Profit Entity", AccountsHelper.accountWithEntityType(IdentityEntity.non_profit) },
                { "Government Entity", AccountsHelper.accountWithEntityType(IdentityEntity.government_entity) }
        };
    }

    // ============== POSITIVE: Different Dashboard Types ==============

    @DataProvider(name = "validDashboardTypes")
    public Object[][] validDashboardTypes() {
        return new Object[][] {
                { "Full Dashboard", AccountsHelper.accountWithDashboard(Dashboard.full) },
                { "Express Dashboard", AccountsHelper.accountWithDashboard(Dashboard.express) },
                { "No Dashboard", AccountsHelper.accountWithDashboard(Dashboard.none) }
        };
    }

    // ============== NEGATIVE: Invalid Country Codes ==============

    @DataProvider(name = "invalidCountryCodes")
    public Object[][] invalidCountryCodes() {
        return new Object[][] {
                { "Unsupported Country (SY)", "SY", "Account creation is currently unavailable" },
                { "Unsupported Country (IR)", "IR", "Account creation is currently unavailable" },
                { "Unsupported Country (KP)", "KP", "Account creation is currently unavailable" },
                { "Empty Country Code", "", "country" },
                { "Numeric Country Code", "99", "country" },
                { "Too Long Country Code", "USAA", "country" }
        };
    }

    // ============== NEGATIVE: Invalid Fees/Losses Collector Combinations
    // ==============

    @DataProvider(name = "invalidResponsibilityCombinations")
    public Object[][] invalidResponsibilityCombinations() {
        return new Object[][] {
                { "Application Custom Fees + Application Losses",
                        DefaultsResponsibilitiesFeesCollector.application_custom,
                        DefaultsResponsibilitiesLossesCollector.application },
                { "Application Express Fees + Application Losses",
                        DefaultsResponsibilitiesFeesCollector.application_express,
                        DefaultsResponsibilitiesLossesCollector.application }
        };
    }

}
