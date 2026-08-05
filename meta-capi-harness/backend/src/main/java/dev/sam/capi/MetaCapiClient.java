package dev.sam.capi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class MetaCapiClient implements MetaGateway {
    private final RestClient restClient;
    private final String apiVersion;
    private final String pixelId;
    private final String accessToken;
    private final String testEventCode;

    public MetaCapiClient(RestClient.Builder builder,
                          @Value("${app.meta.api-version}") String apiVersion,
                          @Value("${app.meta.pixel-id:}") String pixelId,
                          @Value("${app.meta.access-token:}") String accessToken,
                          @Value("${app.meta.test-event-code:}") String testEventCode) {
        this.restClient = builder.baseUrl("https://graph.facebook.com").build();
        this.apiVersion = apiVersion;
        this.pixelId = pixelId;
        this.accessToken = accessToken;
        this.testEventCode = testEventCode;
    }

    @Override
    public boolean configured() {
        return !pixelId.isBlank() && !accessToken.isBlank() && !testEventCode.isBlank();
    }

    @Override
    public MetaGatewayResponse send(Map<String, Object> event) {
        if (!configured()) {
            throw new IllegalStateException("Meta Test Events credentials are not configured");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("data", List.of(event));
        body.put("test_event_code", testEventCode);
        try {
            String response = restClient.post()
                    .uri(uri -> uri.path("/{version}/{pixelId}/events")
                            .queryParam("access_token", accessToken)
                            .build(apiVersion, pixelId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return new MetaGatewayResponse(true, 200, summarize(response));
        } catch (HttpStatusCodeException failure) {
            return new MetaGatewayResponse(false, failure.getStatusCode().value(), summarize(failure.getResponseBodyAsString()));
        } catch (RestClientException failure) {
            return new MetaGatewayResponse(false, 0, "Meta endpoint was unreachable");
        }
    }

    private String summarize(String value) {
        if (value == null || value.isBlank()) return "Empty response";
        return value.length() <= 500 ? value : value.substring(0, 500) + "…";
    }
}
