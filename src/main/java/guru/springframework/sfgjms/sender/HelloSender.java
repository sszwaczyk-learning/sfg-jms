package guru.springframework.sfgjms.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.sfgjms.config.JmsConfig;
import guru.springframework.sfgjms.model.HelloWorldMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class HelloSender {

    private final JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 2000)
    public void sendMessage() {
        System.out.println("I'm sending a message");
        HelloWorldMessage message = HelloWorldMessage.builder()
                .id(UUID.randomUUID())
                .message("Hello")
                .build();

        jmsTemplate.convertAndSend(JmsConfig.MY_QUEUE, message);

        System.out.println("Message sent");
    }

    @Scheduled(fixedRate = 2000)
    public void sendMessageWithResponse() throws JMSException {
        System.out.println("I'm sending a message and waiting for response...");
        HelloWorldMessage message = HelloWorldMessage.builder()
                .id(UUID.randomUUID())
                .message("Hello")
                .build();

        Message receivedMessage = jmsTemplate.sendAndReceive(JmsConfig.REPLY_QUEUE, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message hellloMessage = null;
                try {
                    hellloMessage = session.createTextMessage(objectMapper.writeValueAsString(message));
                    hellloMessage.setStringProperty("_type", HelloWorldMessage.class.getName());
                    return hellloMessage;
                } catch (JsonProcessingException e) {
                    throw new JMSException("booom");
                }
            }
        });

        System.out.println(receivedMessage.getBody(String.class));
    }
}
