package ru.izpz.s21meet.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "telegram_users")
public class TelegramUser {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "school_login")
    private String schoolLogin;

    @Column(name = "sber_login")
    private String sberLogin;

    @Column(name = "telegram_login")
    private String telegramLogin;

    @Column(name = "sber_team")
    private String sberTeam;

    @Column(name = "sber_role")
    private String sberRole;

    @Column(name = "sber_comment")
    private String sberComment;

    @Column(name = "status")
    private Status status;

//    @Column(name = "first_name")
//    private String firstName;
//
//    @Column(name = "last_name")
//    private String lastName;


}
