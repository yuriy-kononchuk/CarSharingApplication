package com.example.project.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService {
    public static final String TELEGRAM_API_ENDPOINT
            = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
    @Value("${telegram.api.bot.token}")
    private String botToken;
    @Value("${telegram.api.chat.id}")
    private String chatId;
    private final ApiService apiService;

    public void sendMessage(String message) {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String url = String.format(TELEGRAM_API_ENDPOINT, botToken, chatId, encodedMessage);

        apiService.fetchDataFromApi(url);
    }
}
