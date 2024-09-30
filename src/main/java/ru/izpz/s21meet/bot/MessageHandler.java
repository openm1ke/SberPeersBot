package ru.izpz.s21meet.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.izpz.s21meet.model.Status;
import ru.izpz.s21meet.model.TelegramUser;
import ru.izpz.s21meet.service.GoogleAnalyticsService;
import ru.izpz.s21meet.service.GoogleSheetsService;
import ru.izpz.s21meet.service.TelegramMessageService;
import ru.izpz.s21meet.service.TelegramUserService;
import ru.izpz.s21meet.util.ReplyMessages;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final GoogleAnalyticsService googleAnalyticsService;

    public void handle(long chatId, String message) {
        message = message.trim();
        TelegramUser telegramUser = telegramUserService.getUser(chatId);
        if (telegramUser == null) {
            registerNewUser(chatId);
        } else {
            processUserStatus(telegramUser, message);
        }

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("message_content", message);
        eventParams.put("chat_id", chatId);

        googleAnalyticsService.sendEvent(String.valueOf(chatId), "message_received", eventParams);
    }

    private void registerNewUser(long chatId) {
        telegramMessageService.sendMessage(chatId, ReplyMessages.NEW_USER);
        telegramMessageService.sendMessage(chatId, ReplyMessages.SCHOOL_LOGIN);
        telegramUserService.addUser(chatId);

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("status", Status.NEW.toString());
        eventParams.put("chat_id", chatId);

        googleAnalyticsService.sendEvent(String.valueOf(chatId), "status", eventParams);
    }

    private void processUserStatus(TelegramUser telegramUser, String message) {
        if (message.equals("/start")) {
            log.info("message /start");
            telegramUser.setSchoolLogin(null);
            telegramUser.setStatus(Status.NEW);
        }

        switch (telegramUser.getStatus()) {
            case NEW -> promptSchoolLogin(telegramUser);
            case SCHOOL_LOGIN -> processSchoolLogin(telegramUser, message);
            case SBER_LOGIN -> processSberLogin(telegramUser, message);
            case TELEGRAM_LOGIN -> processTelegramLogin(telegramUser, message);
            case SBER_TEAM -> processSberTeam(telegramUser, message);
            case SBER_ROLE -> processSberRole(telegramUser, message);
            case SBER_COMMENT -> processSberComment(telegramUser, message);
            case READY -> handleReady(telegramUser);
            default -> handleUnknownStatus(telegramUser);
        }

        // Сохранение изменений только один раз после всей обработки
        telegramUserService.update(telegramUser);

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("status", telegramUser.getStatus().toString());
        eventParams.put("chat_id", telegramUser.getChatId());

        googleAnalyticsService.sendEvent(String.valueOf(telegramUser.getChatId()), "status", eventParams);
    }
    private void promptSchoolLogin(TelegramUser telegramUser) {
        telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.SCHOOL_LOGIN);
        telegramUser.setStatus(Status.SCHOOL_LOGIN);
    }

    private void processSchoolLogin(TelegramUser telegramUser, String message) {
        if (isValidSchoolLogin(message)) {
            if(telegramUserService.checkSchoolLogin(message)) {
                telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.ALREADY_REGISTERED);
                return;
            }
            telegramUser.setSchoolLogin(message);
            telegramUser.setStatus(Status.SBER_LOGIN);
            telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.SBER_LOGIN);
        } else {
            telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.INVALID_LOGIN);
        }
    }

    private void processSberLogin(TelegramUser telegramUser, String message) {
        if (isValidText(message)) {
            telegramUser.setSberLogin(message);
            telegramUser.setStatus(Status.TELEGRAM_LOGIN);
            telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.TELEGRAM_LOGIN);
        } else {
            telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.INVALID_LOGIN);
        }
    }


    private void processTelegramLogin(TelegramUser telegramUser, String message) {
        if (isValidText(message)) {
            telegramUser.setTelegramLogin(message);
            telegramUser.setStatus(Status.SBER_TEAM);
            telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.SBER_TEAM);
        } else {
            telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.INVALID_LOGIN);
        }
    }

    private void processSberTeam(TelegramUser telegramUser, String message) {
        if (isValidText(message)) {
            telegramUser.setSberTeam(message);
            telegramUser.setStatus(Status.SBER_ROLE);
            telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.SBER_ROLE);
        } else {
            telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.INVALID_TEXT);
        }
    }

    private void processSberRole(TelegramUser telegramUser, String message) {
        if (isValidText(message)) {
            telegramUser.setSberRole(message);
            telegramUser.setStatus(Status.SBER_COMMENT);
            telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.SBER_COMMENT);
        } else {
            telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.INVALID_TEXT);
        }
    }


    private void processSberComment(TelegramUser telegramUser, String message) {
        if (isValidText(message)) {
            telegramUser.setSberComment(message);
            telegramUser.setStatus(Status.READY);
            replyJoinCommunity(telegramUser, ReplyMessages.READY);
            try {
                googleSheetsService.addUserToSheet(telegramUser);
            } catch (IOException | GeneralSecurityException e) {
                telegramMessageService.sendMessage(ADMIN_ID, e.getMessage());
            }
        } else {
            telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.INVALID_TEXT);
        }
    }

    private void replyJoinCommunity(TelegramUser telegramUser, String replyMessage) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(ReplyMessages.JOIN_COMMUNITY);
        inlineKeyboardButton.setUrl(JOIN_URL);

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(inlineKeyboardButton);
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);

        telegramMessageService.sendMessage(telegramUser.getChatId(), replyMessage, inlineKeyboardMarkup);
    }

    private void handleReady(TelegramUser telegramUser) {
        replyJoinCommunity(telegramUser, ReplyMessages.READY);
    }

    private void handleUnknownStatus(TelegramUser telegramUser) {
        telegramMessageService.sendMessage(telegramUser.getChatId(), ReplyMessages.ERROR);
        log.error("Unexpected status value: {}", telegramUser.getStatus());
    }
}
