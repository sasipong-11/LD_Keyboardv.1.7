package th.or.nectec.twskeyboard;

public class LicenseManager {
    private static final String BASE_URL = "https://ld-api.uat.rtt.in.th";

    public static final String URL_ACTIVATED =
            BASE_URL + "/api/license/activated?prgVersion=2";

    public static final String PREF_LICENSE_KEY = "license_key";

    // msg values from server
    public static final String MSG_OK            = "OK";
    public static final String MSG_CHANGE_SERIAL = "CHANGE_SERIAL";

    public static boolean isValidFormat(String key) {
        return key != null &&
               key.matches("[A-Z0-9]{5}-[A-Z0-9]{5}-[A-Z0-9]{5}-[A-Z0-9]{5}-[A-Z0-9]{5}");
    }
}
