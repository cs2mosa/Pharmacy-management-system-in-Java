package Http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class ApiJson {
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();

    private ApiJson() {
    }

    public static Gson gson() {
        return GSON;
    }
}
