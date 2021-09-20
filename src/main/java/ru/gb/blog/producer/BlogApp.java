package ru.gb.blog.producer;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class BlogApp {
    private final static String EXCHANGE = "blog";

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()
        ) {
            channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.TOPIC);
            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите \"имя_блога сообщение\":");
            String input = scanner.nextLine().trim();

            String routingKey = input.split(" ")[0];
            String message = input.substring(routingKey.length() + 1);

            channel.basicPublish(EXCHANGE, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("[+] Sent message: " + message);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
