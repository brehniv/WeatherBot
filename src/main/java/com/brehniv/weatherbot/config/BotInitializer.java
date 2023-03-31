package com.brehniv.weatherbot.config;


import com.brehniv.weatherbot.services.TelegramBot;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
@Slf4j
@Component
@EnableScheduling
public class BotInitializer {

    @Autowired
    TelegramBot bot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            log.error("Error occured" + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 7 * * *")
    private void sendforecast() {
        System.out.println("Test");
        try {
            bot.sendForecastALL();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



