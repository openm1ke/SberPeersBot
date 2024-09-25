package ru.izpz.s21meet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.izpz.s21meet.model.TelegramUser;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    boolean existsByChatId(Long chatId);
    TelegramUser findByChatId(long chatId);
    boolean existsBySchoolLogin(String login);
}
