package ru.izpz.s21meet.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.izpz.s21meet.model.Status;
import ru.izpz.s21meet.model.TelegramUser;
import ru.izpz.s21meet.service.GoogleSheetsService;
import ru.izpz.s21meet.service.TelegramMessageService;
import ru.izpz.s21meet.service.TelegramUserService;
import ru.izpz.s21meet.util.ReplyMessages;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static ru.izpz.s21meet.util.ValidationUtils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler {

    @Value("${bot.admin.id}")
    private Long ADMIN_ID;

    @Value("${bot.join.url}")
    private String JOIN_URL;

    private final TelegramUserService telegramUserService;
    private final TelegramMessageService telegramMessageService;
    private final GoogleSheetsService googleSheetsService;

    public void handle(long chatId, String message) {
        TelegramUser telegramUser = telegramUserService.getUser(chatId);
        message = message.trim();
        if (telegramUser == null) {
            handleNewUser(chatId);
        } else {
            handleUserByStatus(telegramUser, message);
        }
    }

    private void handleNewUser(long chatId) {
        telegramMessageService.sendMessage(chatId, ReplyMessages.NEW_USER);
        telegramMessageService.sendMessage(chatId, ReplyMessages.SCHOOL_LOGIN);
        telegramUserService.addUser(chatId);
        log.info("User with chatId {} not found. Registering...", chatId);
    }

    private void handleUserByStatus(TelegramUser telegramUser, String message) {
        switch (telegramUser.getStatus()) {
            case SCHOOL_LOGIN:
                handleSchoolLogin(telegramUser, message);
                break;
            case SBER_LOGIN:
                handleSberLogin(telegramUser, message);
                break;
            case SBER_TEAM:
                handleSberTeam(telegramUser, message);
                break;
            case SBER_ROLE:
                handleSberRole(telegramUser, message);
                break;
            case READY:
                handleReady(telegramUser);
                break;
            default:
                handleError(telegramUser);
                log.error("Unexpected status value: {}", telegramUser.getStatus());
        }
    }

    private void handleSchoolLogin(TelegramUser telegramUser, String message) {
        String replyMessage = ReplyMessages.INVALID_LOGIN;
        if (isValidSchoolLogin(message)) {
            if(telegramUserService.checkSchoolLogin(message)) {
                telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.ALREADY_REGISTERED);
                return;
            }
            telegramUser.setSchoolLogin(message);
            telegramUser.setStatus(Status.SBER_LOGIN);
            telegramUserService.update(telegramUser);
            replyMessage = ReplyMessages.SBER_LOGIN;
        }
        telegramMessageService.sendMessage(telegramUser.getChatId(), replyMessage);
    }

    private void handleSberLogin(TelegramUser telegramUser, String message) {
        String replyMessage = ReplyMessages.INVALID_LOGIN;
        if (isValidText(message)) {
            telegramUser.setSberLogin(message);
            telegramUser.setStatus(Status.SBER_TEAM);
            telegramUserService.update(telegramUser);
            replyMessage = ReplyMessages.SBER_TEAM;
        }
        telegramMessageService.sendMessage(telegramUser.getChatId(), replyMessage);
    }

    private void handleSberTeam(TelegramUser telegramUser, String message) {
        String replyMessage = ReplyMessages.INVALID_TEXT;
        if (isValidText(message)) {
            telegramUser.setSberTeam(message);
            telegramUser.setStatus(Status.SBER_ROLE);
            telegramUserService.update(telegramUser);
            replyMessage = ReplyMessages.SBER_ROLE;
        }
        telegramMessageService.sendMessage(telegramUser.getChatId(), replyMessage);
    }

    private void handleSberRole(TelegramUser telegramUser, String message) {
        String replyMessage = ReplyMessages.INVALID_TEXT;
        if (isValidText(message)) {
            telegramUser.setSberRole(message);
            telegramUser.setStatus(Status.READY);
            telegramUserService.update(telegramUser);
            replyMessage = ReplyMessages.READY;

            // Создание кнопки
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(ReplyMessages.JOIN_COMMUNITY);
            inlineKeyboardButton.setUrl(JOIN_URL);  // Твой URL

            // Добавляем кнопку в строку и клавиатуру
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(inlineKeyboardButton);
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            rowsInline.add(rowInline);
            inlineKeyboardMarkup.setKeyboard(rowsInline);

            // Отправка сообщения с кнопкой
            telegramMessageService.sendMessage(telegramUser.getChatId(), replyMessage, inlineKeyboardMarkup);
            try {
                googleSheetsService.addUserToSheet(telegramUser);
            } catch (IOException | GeneralSecurityException e) {
                telegramMessageService.sendMessage(ADMIN_ID, e.getMessage());
            }
        } else {
            telegramMessageService.sendMessage(telegramUser.getChatId(), replyMessage);
        }
    }

    private void handleReady(TelegramUser telegramUser) {
        telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.READY);
    }

    private void handleError(TelegramUser telegramUser) {
        telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.ERROR);
    }
}
