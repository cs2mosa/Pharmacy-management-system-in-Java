package Http;

import java.net.URI;

/**
 * Base URL for the Pharmacy Web API (no trailing slash). Override with
 * {@code -Dpharmacy.api.baseUrl=http://host:port} or {@code PHARMACY_API_BASE_URL}.
 */
public final class ApiConfig {
    private ApiConfig() {
    }

    public static String baseUrl() {
        String prop = System.getProperty("pharmacy.api.baseUrl");
        if (prop != null && !prop.isBlank()) {
            return prop.trim().replaceAll("/$", "");
        }
        String env = System.getenv("PHARMACY_API_BASE_URL");
        if (env != null && !env.isBlank()) {
            return env.trim().replaceAll("/$", "");
        }
        return "http://localhost:5288";
    }

    public static URI uri(String path) {
        String p = path.startsWith("/") ? path : "/" + path;
        return URI.create(baseUrl() + p);
    }
}
