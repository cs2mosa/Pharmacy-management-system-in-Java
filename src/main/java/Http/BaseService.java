package Http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Shared HTTP helpers for REST calls to the Pharmacy API (JSON / Gson).
 */
public class BaseService {
    protected static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    protected final Gson gson = ApiJson.gson();

    protected URI uri(String path) {
        return ApiConfig.uri(path);
    }

    protected HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    protected Optional<String> getBody(String path) {
        try {
            var req = HttpRequest.newBuilder(uri(path))
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            var resp = send(req);
            if (resp.statusCode() == 404) {
                return Optional.empty();
            }
            if (resp.statusCode() >= 400) {
                throw new ApiException(resp.statusCode(), resp.body());
            }
            return Optional.ofNullable(resp.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    protected <T> Optional<T> getJson(String path, Class<T> type) {
        return getBody(path).map(b -> gson.fromJson(b, type));
    }

    protected <T> Optional<T> getJson(String path, Type type) {
        return getBody(path).map(b -> gson.fromJson(b, type));
    }

    protected OptionalInt postForInt(String path, Object body) {
        try {
            var req = HttpRequest.newBuilder(uri(path))
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                    .build();
            var resp = send(req);
            if (resp.statusCode() >= 400) {
                return OptionalInt.empty();
            }
            if (resp.body() == null || resp.body().isBlank()) {
                return OptionalInt.empty();
            }
            try {
                return OptionalInt.of(Integer.parseInt(resp.body().trim()));
            } catch (NumberFormatException e) {
                return OptionalInt.empty();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return OptionalInt.empty();
        } catch (IOException e) {
            return OptionalInt.empty();
        }
    }

    protected <T> Optional<T> postForJson(String path, Object body, Class<T> type) {
        try {
            var req = HttpRequest.newBuilder(uri(path))
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                    .build();
            var resp = send(req);
            if (resp.statusCode() == 401 || resp.statusCode() == 404) {
                return Optional.empty();
            }
            if (resp.statusCode() >= 400) {
                throw new ApiException(resp.statusCode(), resp.body());
            }
            if (resp.body() == null || resp.body().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(gson.fromJson(resp.body(), type));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    protected boolean patchJson(String path, Object body) {
        try {
            var req = HttpRequest.newBuilder(uri(path))
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                    .build();
            var resp = send(req);
            return resp.statusCode() >= 200 && resp.statusCode() < 300;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    protected boolean postNoBody(String path, Object body) {
        try {
            var req = HttpRequest.newBuilder(uri(path))
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                    .build();
            var resp = send(req);
            return resp.statusCode() >= 200 && resp.statusCode() < 300;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    protected boolean putJson(String path, Object body) {
        try {
            var req = HttpRequest.newBuilder(uri(path))
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                    .build();
            var resp = send(req);
            return resp.statusCode() >= 200 && resp.statusCode() < 300;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    protected boolean delete(String path) {
        try {
            var req = HttpRequest.newBuilder(uri(path))
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/json")
                    .DELETE()
                    .build();
            var resp = send(req);
            return resp.statusCode() >= 200 && resp.statusCode() < 300;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    protected static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    protected static Type listOf(Type elementType) {
        return TypeToken.getParameterized(java.util.List.class, elementType).getType();
    }
}
