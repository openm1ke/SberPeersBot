package ru.izpz.s21meet.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestPropertySource;
import ru.izpz.s21meet.model.TelegramUser;
import ru.izpz.s21meet.repository.TelegramUserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@TestPropertySource(locations = "classpath:application.properties")
//class GoogleSheetsServiceTest {
//
//    @Autowired
//    TelegramUserRepository telegramUserRepository;
//
//    @Autowired
//    private GoogleSheetsService googleSheetsService;
//
//    private final static String sheetName = "TestSheet";
//
//    @Test
//    void addDataToSheet() {
//        List<TelegramUser> users = telegramUserRepository.findAll();
//        for(TelegramUser entity : users) {
//            String[] row = {
//                    String.valueOf(entity.getId()),
//                    String.valueOf(entity.getChatId()),
//                    entity.getSchoolLogin(),
//                    entity.getSberLogin(),
//                    entity.getSberTeam(),
//                    entity.getSberRole(),
//                    entity.getStatus().toString()
//            };
//
//            try {
//                googleSheetsService.addDataToSheet(row, sheetName);
//            } catch (IOException | GeneralSecurityException e) {
//                fail("Произошла ошибка при добавлении строки в Google таблицу: " + e.getMessage());
//            }
//        }
//    }
//}