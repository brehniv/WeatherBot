package com.brehniv.weatherbot.services;

import WeatherPackage.WeatherRequest;
import com.brehniv.weatherbot.config.BotConfig;
import com.brehniv.weatherbot.model.User;
import com.brehniv.weatherbot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.Key;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    final BotConfig config;

    public TelegramBot(BotConfig config) {

        this.config = config;
        List<BotCommand> commandList = new ArrayList<>();
        Message test = new Message();
        commandList.add(new BotCommand("/start", "Start chat with bot"));
        commandList.add(new BotCommand("/setmaincity", "Set city for forecast"));
        commandList.add(new BotCommand("/info", "Info on bots"));
        commandList.add(new BotCommand("/changestatus", "Change sending forecast"));
        try {
            execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
        } catch (Exception e) {
            log.error("Error command list" + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update){
        long chatId=update.getMessage().getChatId();
        Message previousMessage = update.getMessage().getReplyToMessage();
        if (previousMessage != null) {
            String previousMessageText = previousMessage.getText();
            if(previousMessageText=="Відповіш ім'ям міста,селища на це повідомлення для встановлення його головним\n");
            {System.out.println(previousMessageText);
             sendMessage(chatId,String.format("Твоє головне місто зараз : %s",update.getMessage().getText()));
                SetMainCity(update.getMessage());
            return;
            }
        }
        if(update.hasMessage()&& update.getMessage().hasText()){
            String message=update.getMessage().getText();
            Map<String,String> statuskey=Map.of("Змінити статус","Change status","Отримати прогноз","Get forecast"
            ,"Отримати статус","Get status","Видалити дані","Delete data");
            if(statuskey.get(message)!=null)message=statuskey.get(message);
            switch (message) {
                case "/start":
                    SendStartMessage(chatId,update.getMessage().getChat().getFirstName());
                    RegisterUser(update.getMessage());
                    break;
                case "/setmaincity":
                    sendMessage(chatId,"Відповіси ім'ям пункта на це повідомлення для встановлення його головним");
                    break;
                case "/info":
                    sendMessage(chatId,"Бот написаний одним неадекватом по імені Константин для отримання прогнозу погоди кожного ранку.\nНахер я його зробив? та просто робити було нічого. Бот створений 27.03.2023 Мова програмування Java, Spring Framework");
                    break;
                case "Change status":
                    changeStatus(chatId);
                    break;
                case "Get forecast":
                    sendForecastTo(chatId);
                    break;
                case "Delete data":
                    deleteData(chatId);
                    sendMessage(chatId,"Дані успішно видалено");
                    break;
                case "Get status":
                    getStatus(chatId);
                    break;
                default:
                    sendMessage(chatId,"Невідома команда, єбаш ще раз");
                    break;
            }
        }

    }

    private void deleteData(long chatId) {
        userRepository.deleteById(chatId);
    }

    private void getStatus(long chatid){
        Optional<User> user = userRepository.findById(chatid);
        if(user.isPresent()) {
            Map<String, String> statuskey = Map.of("true", "ввімкнуто", "false", "вимкнено");
            sendMessage(chatid, "Отримання прогнозу: " + statuskey.get(String.valueOf(user.get().isRecieve_msg())) + "\nОбране місто для прогнозу: " + user.get().getFav_city().replace("_"," ") + "\n" + "Ви долучились до бота:" + user.get().getRegisteredAt());
        }
        else {
            sendMessage(chatid, "Користувача не знайдено.\nВідправте /start для автоматичної реєстрації");
        }
    }
    private void changeStatus(long chatid){
        Optional<User> user = userRepository.findById(chatid);
        boolean status=(user.get().isRecieve_msg());
        if (user.isPresent()) {
            User entityToUpdate = user.get();
            entityToUpdate.setRecieve_msg(!status);
            userRepository.save(entityToUpdate);
        }

        Map<String,String> statuskey=Map.of("true","ввімкнуто","false","вимкнено");
        sendMessage(chatid,"Отримання прогнозу: "+statuskey.get(String.valueOf(!status)));
    }
    public void sendForecastALL() throws IOException {
        List<User> users = (List<User>) userRepository.findAll();
        for (User item : users) {
            if(item.getFav_city()!=null&&item.isRecieve_msg()){
                try {
                sendMessage(item.getChatid(), WeatherRequest.GetWeather(item.getFav_city()));
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
        }
    }
    public void sendForecastTo(long chatid){
        Optional<User> user=userRepository.findById(chatid);

        if(user.get().getFav_city()!=null) {
            try {
                sendMessage(user.get().getChatid(), WeatherRequest.GetWeather(user.get().getFav_city()));
            } catch (Exception e) {
                log.error(e.toString());
            }

        }
        else sendMessage(chatid,"Пункт не вибрано. задай його через /setmaincity");
    }
    private void sendMessage(long chatId,String text){
        SendMessage message=new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        ReplyKeyboardMarkup keyboardMarkup=new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows=new ArrayList<>();
        KeyboardRow row=new KeyboardRow();
        row.add("Отримати прогноз");
        row.add("Отримати статус");
        keyboardRows.add(row);
        row=new KeyboardRow();
        row.add("Змінити статус");
        row.add("Видалити дані");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);


        try{
            execute(message);
        }
        catch (TelegramApiException e){
            log.error("Error "+e.getMessage());
        }


    }

    private void SendStartMessage(long chatid,String message){
        Optional<User> user = userRepository.findById(chatid);
        String answer="Привіт, "+message+", для роботи зі мною використовуй меню.";
        if(!user.isPresent()) {
        answer="Привіт, "+message+",я бот створений для надсилання прогнозу погоди кожного ранку.\nДля твоєї зручності я вже зібрав деякі дані про тебе.\nДля отримання ранкових прогнозів залишилось " +
                "зовсім трішки.\nВідправ мені повідомлення /setmaincity та відповіш на нього назвою пункту для якого потрібна погода.";
        log.info("Replied to " +message);
        }
        sendMessage(chatid,answer);
    }
    private void RegisterUser(Message msg){
        if(userRepository.findById(msg.getChatId()).isEmpty()){
            var chatId= msg.getChatId();
            var chat= msg.getChat();

            User user=new User();
            user.setChatid(chatId);
            user.setFirst_name(chat.getFirstName());
            user.setLast_name(chat.getLastName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            user.setRecieve_msg(true);
        userRepository.save(user);
        log.info("Add new user chatid: "+chatId);
        }
    }

    private void SetMainCity(Message msg){
        Optional<User> optionalEntity = userRepository.findById(msg.getChatId());
        if (optionalEntity.isPresent()) {
            User entityToUpdate = optionalEntity.get();
            String newcity=msg.getText();
            newcity=newcity.replace(" ","_");
            entityToUpdate.setFav_city(newcity);

            userRepository.save(entityToUpdate);
        }
    }
}
