package ru.izpz.s21meet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class YandexMetricaService {
    private static final String METRICA_URL = "https://mc.yandex.ru/watch/{counterId}";
    private final RestTemplate restTemplate = new RestTemplate();
    private final String counterId = "98499865"; // Замените на ваш счетчик ID

    public void sendEvent(String eventName, Map<String, String> eventParams) {
        try {
            // Создаем параметры запроса
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("page-url", "http://example.com/"); // Замените на реальный URL или оставьте фиктивным
            requestBody.put("browser-info", "sendEvent"); // Необходимый параметр для Метрики
            requestBody.put("params", eventParams);

            // Устанавливаем заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Создаем запрос
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Отправляем запрос на сервер Яндекс Метрики
            ResponseEntity<String> response = restTemplate.postForEntity(METRICA_URL, requestEntity, String.class, counterId);

            log.info("Event sent to Yandex Metrica: {}", response.getBody());
        } catch (Exception e) {
            log.error("Failed to send event to Yandex Metrica", e);
        }
    }
}
