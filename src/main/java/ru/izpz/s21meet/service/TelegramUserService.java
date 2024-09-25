package ru.izpz.s21meet.service;


import org.springframework.stereotype.Service;
import ru.izpz.s21meet.model.Status;
import ru.izpz.s21meet.model.TelegramUser;
import ru.izpz.s21meet.repository.TelegramUserRepository;

@Service
public class TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;

    public TelegramUserService(TelegramUserRepository telegramUserRepository) {
        this.telegramUserRepository = telegramUserRepository;
    }

    public boolean checkSchoolLogin(String login) {
        return telegramUserRepository.existsBySchoolLogin(login);
    }

    public boolean checkUser(long chatId) {
        return telegramUserRepository.existsByChatId(chatId);
    }

    public TelegramUser getUser(long chatId) {
        return telegramUserRepository.findByChatId(chatId);
    }

    public void addUser(long chatId) {
        TelegramUser telegramUser = new TelegramUser();
        telegramUser.setChatId(chatId);
        telegramUser.setStatus(Status.SCHOOL_LOGIN);
        telegramUserRepository.save(telegramUser);
    }

    public void update(TelegramUser telegramUser) {
        telegramUserRepository.save(telegramUser);
    }
}
