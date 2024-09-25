package ru.izpz.s21meet.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import ru.izpz.s21meet.model.TelegramUser;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

@Service
public class GoogleSheetsService {

    @Autowired
    private final ResourceLoader resourceLoader;

    @Value("${google.spreadsheet.id}")
    private String spreadSheetId;

    @Value("${google.application.name}")
    private String applicationName;

    public GoogleSheetsService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Sheets getSheetsService() throws IOException, GeneralSecurityException {
        // Путь к JSON-файлу сервисного аккаунта
        InputStream credentialsStream = resourceLoader.getResource("classpath:service-account.json").getInputStream();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        // Подключение с использованием сервисного аккаунта
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, new HttpCredentialsAdapter(credentials))
                .setApplicationName(applicationName)
                .build();
    }

    public void addDataToSheet(String[] data, String sheetName) throws IOException, GeneralSecurityException {
        // Создаем сервис
        Sheets sheetsService = getSheetsService();

        // Данные для добавления
        ValueRange body = new ValueRange()
                .setValues(Collections.singletonList(Arrays.asList(data)));
        // Добавляем данные в Google таблицу
        sheetsService.spreadsheets().values()
                .append(spreadSheetId, sheetName, body)
                .setValueInputOption("RAW") // Или "USER_ENTERED"
                .execute();
    }

    public void addUserToSheet(TelegramUser telegramUser) throws IOException, GeneralSecurityException {
        String[] data = {
                String.valueOf(telegramUser.getId()),
                String.valueOf(telegramUser.getChatId()),
                telegramUser.getSchoolLogin(),
                telegramUser.getSberLogin(),
                telegramUser.getSberTeam(),
                telegramUser.getSberRole(),
                telegramUser.getStatus().toString()
        };
        addDataToSheet(data, "Sheet1");
    }
}
