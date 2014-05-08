package ApproxsimClient.map;

/**
 * This class contains several constants used in this package.
 */
public class MapConstants {
    /**
     * The force units.
     */
    public static final String[] forceUnits = { "Region", "Army Group/Front",
            "Army", "Corps/Mef", "Division", "Brigade", "Regiment/Group",
            "Battalion", "Company", "Platoon", "Section", "Squad", "Team/Crew" };

    /**
     * The force symbols.
     */
    public static final char[] forceSymbols = { 'M', 'L', 'K', 'J', 'I', 'H',
            'G', 'F', 'E', 'D', 'C', 'B', 'A' };

    /**
     * The mobility groups.
     */
    public static final String[] mobileUnits = { "Headquarters (HQ)",
            "Task Force HQ", "Feint Dummy HQ", "Feint Dummy/Task Force HQ",
            "Task Force", "Feint Dummy", "Feint Dummy/Task Force",
            "Installation", "Nuclear Yields in Kilotons", "Mobility Equipment" };

    /**
     * The mobility symbols.
     */
    public static final char[] mobileSymbols = { 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'K', 'M' };

    /**
     * The agencies.
     */
    public static final String[] agencies = { "Shelter Team", "Water Team",
            "Health Team", "Food Team", "Police Team", "Custom Team" };

    /**
     * The agency types.
     */
    public static final String[] agencyTypes = { "ShelterAgencyTeam",
            "WaterAgencyTeam", "HealthAgencyTeam", "FoodAgencyTeam",
            "PoliceAgencyTeam", "CustomAgencyTeam" };

    /**
     * One inch expressed in meters.
     */
    public static final double ONE_INCH = 0.0254;

    /**
     * The degree symbol.
     */
    public static String DEGREE_SYMBOL = new String(
            (new Character('\u00B0')).toString());

    /**
     * Affiliation of the military units.
     */
    public static final String[] affiliationDescription = { "Pending",
            "Unknown", "Assumed Friends", "Friends", "Neutral", "Suspect",
            "Hostile", "Joker", "Faker", "None Specified" };
    /**
     * The affiliation symbols.
     */
    public static final char[] affiliationSymbols = { 'P', 'U', 'A', 'F', 'N',
            'S', 'H', 'J', 'K', 'O' };
    /**
     * Categories of the process variables.
     */
    public static final String[] pvCategories = { "Forces", "Environmental",
            "Quality of Life", "Social", "Economical", "Governance",
            "Political" };

}
