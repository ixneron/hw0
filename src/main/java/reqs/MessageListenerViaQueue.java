package reqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class MessageListenerViaQueue implements MessageListener {

    private static Logger logger = LoggerFactory.getLogger(MessageListenerViaQueue.class);

    @Qualifier("jms2QueueTemplate")
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Jaxb2Marshaller marshaller;

    @Override
    public void onMessage(Message message) {

        if (message instanceof TextMessage){
            TextMessage textMessage = (TextMessage) message;
            try {
                if (textMessage.getText().contains("roma - resender")){

                    logger.info("получено сообщение из первой очереди, оформляем посылку во вторую очередь + активируем");
                    sendToDoubleQueue(textMessage);

                }
                else Magic.createMagic(message, logger, marshaller);

            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToDoubleQueue(TextMessage textMessage) {

        jmsTemplate.send(session -> {

            String temp = textMessage.getText();
            temp = temp.replaceFirst("<cardStatus>inactive</cardStatus>","<cardStatus>active</cardStatus>");
            temp = temp.replaceFirst("<cardLimit>0</cardLimit>", "<cardLimit>1000</cardLimit>");

            TextMessage newMessage = session.createTextMessage();
            newMessage.setText(temp);

            return newMessage;
        });
    }
}
