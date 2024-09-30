package ru.izpz.s21meet.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleAnalyticsService {
    @Autowired
    private final ResourceLoader resourceLoader;

    public GoogleAnalyticsService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private static final String MEASUREMENT_PROTOCOL_URL = "https://www.google-analytics.com/mp/collect";
    private final RestTemplate restTemplate = new RestTemplate();
    private final String trackingId = "G-FNN8TP82VC";
    private static final String API_SECRET = "3ZiEf4NjT6ak2tjF4GqAWg";

    public void sendEvent(String clientId, String eventName, Map<String, Object> eventParams) {
        try {
            // Создаем тело запроса
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("client_id", clientId);
            requestBody.put("events", new Object[]{
                    Map.of("name", eventName, "params", eventParams)
            });

            // Устанавливаем заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Создаем запрос
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Формируем URL с trackingId
            String url = MEASUREMENT_PROTOCOL_URL + "?measurement_id=" + trackingId + "&api_secret=" + API_SECRET;

            // Отправляем запрос
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
