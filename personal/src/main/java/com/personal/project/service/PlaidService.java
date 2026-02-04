package com.personal.project.service;

import com.personal.project.config.PlaidProperties;
import com.personal.project.model.PlaidItem;
import com.personal.project.repository.PlaidItemRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PlaidService {
    private final PlaidProperties properties;
    private final PlaidItemRepository plaidItemRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public PlaidService(PlaidProperties properties, PlaidItemRepository plaidItemRepository) {
        this.properties = properties;
        this.plaidItemRepository = plaidItemRepository;
    }

    public Map<String, Object> createLinkToken(String userId) {
        if (isBlank(userId)) {
            userId = "demo-user";
        }
        Map<String, Object> payload = basePayload();
        payload.put("user", Map.of("client_user_id", userId));
        payload.put("client_name", properties.getClientName());
        payload.put("language", properties.getLanguage());
        payload.put("country_codes", properties.getCountryCodes());
        payload.put("products", properties.getProducts());
        if (!isBlank(properties.getRedirectUri())) {
            payload.put("redirect_uri", properties.getRedirectUri());
        }

        return post("/link/token/create", payload);
    }

    public Map<String, Object> exchangePublicToken(String publicToken, String userId) {
        if (isBlank(publicToken)) {
            throw new ResponseStatusException(BAD_REQUEST, "publicToken is required");
        }
        Map<String, Object> payload = basePayload();
        payload.put("public_token", publicToken);

        Map<String, Object> response = post("/item/public_token/exchange", payload);
        String accessToken = asString(response.get("access_token"));
        String itemId = asString(response.get("item_id"));

        if (!isBlank(accessToken)) {
            saveAccessToken(userId, accessToken, itemId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("item_id", itemId);
        result.put("request_id", response.get("request_id"));
        return result;
    }

    public Map<String, Object> getTransactions(String userId, LocalDate startDate, LocalDate endDate) {
        PlaidItem item = plaidItemRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No Plaid item found for user"));

        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(30);

        Map<String, Object> payload = basePayload();
        payload.put("access_token", item.getAccessToken());
        payload.put("start_date", start.toString());
        payload.put("end_date", end.toString());

        return post("/transactions/get", payload);
    }

    private void saveAccessToken(String userId, String accessToken, String itemId) {
        String resolvedUserId = isBlank(userId) ? "demo-user" : userId;
        Optional<PlaidItem> existing = plaidItemRepository.findByUserId(resolvedUserId);
        PlaidItem item = existing.orElseGet(PlaidItem::new);
        item.setUserId(resolvedUserId);
        item.setAccessToken(accessToken);
        item.setItemId(itemId);
        plaidItemRepository.save(item);
    }

    private Map<String, Object> basePayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("client_id", properties.getClientId());
        payload.put("secret", properties.getSecret());
        return payload;
    }

    private Map<String, Object> post(String path, Map<String, Object> payload) {
        if (isBlank(properties.getBaseUrl()) || isBlank(properties.getClientId()) || isBlank(properties.getSecret())) {
            throw new ResponseStatusException(BAD_REQUEST, "Plaid configuration is missing");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                properties.getBaseUrl() + path,
                HttpMethod.POST,
                request,
                Map.class
        );
        return response.getBody();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
