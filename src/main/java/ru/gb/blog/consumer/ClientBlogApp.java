package ru.gb.blog.consumer;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class ClientBlogApp {
    private final static String EXCHANGE = "blog";
    private final static String SUBSCRIBE_BLOG = "set_blog";
    private final static String UNSUBSCRIBE_BLOG = "unsubscribe";
    private static List<String> blogs = new LinkedList<>();


    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            Channel channel = factory.newConnection().createChannel();
            channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.TOPIC);
            String queue = channel.queueDeclare().getQueue();

            Scanner scanner = new Scanner(System.in);

            System.out.println("Для подписки на интересующий блог(тему) введите: \"set_blog имя_блога\".");
            System.out.println("Чтобы отписаться от блога(темы) введите: \"unsubscribe имя_блога\".");
            while (true) {
                String input = scanner.nextLine().trim();
                switch (input.split(" ")[0]) {
                    case SUBSCRIBE_BLOG:
                        blogs.add(input.substring(SUBSCRIBE_BLOG.length() + 1));
                        break;
                    case UNSUBSCRIBE_BLOG:
                        channel.queueDelete(queue);
                        queue = channel.queueDeclare().getQueue();
                        blogs.remove(input.substring(UNSUBSCRIBE_BLOG.length() + 1));
                        break;
                }
                System.out.println("Вы подписаны на: " + blogs);

                for (String b : blogs) {
                    channel.queueBind(queue, EXCHANGE, b);
                }

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    System.out.println(" [x] Received " + delivery.getEnvelope().getRoutingKey() + ": \'" + message + "'");
                };

                channel.basicConsume(queue, true, deliverCallback, consumerTag -> {
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
