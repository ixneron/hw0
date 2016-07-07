package reqs;

import generated.Card;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

@Component
public class RequestSender implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(RequestSender.class);

    @Autowired
    private ActiveMQConnectionFactory mqConnectionFactory;

    @Override
    public void run() {

        try {
            QueueConnection queueConnection = mqConnectionFactory.createQueueConnection();
            queueConnection.start();
            QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            TopicConnection topicConnection = mqConnectionFactory.createTopicConnection();
            topicConnection.start();
            TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            logger.info("созданы sender соеденинения");

            sendViaQueue(queueSession);
            sendViaTopic(topicSession);

            queueSession.close();
            queueConnection.close();
            topicSession.close();
            topicConnection.close();

            logger.info("закрываем sender оединения...");

        } catch (JMSException | JAXBException e) {
            e.printStackTrace();
        }
    }

    public byte[] convertXmlMessageToBytes () throws JAXBException {

        logger.info("конвертируем сообщение в байты ...");

        Card card = new Card();
        card.setCardOwner("Vasiliy");
        card.setCardStatus("diactiv");
        card.setCardLimit(0);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JAXBContext context = JAXBContext.newInstance(Card.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(card, outputStream);

        return outputStream.toByteArray();
    }

    public void sendViaQueue (QueueSession session) throws JMSException, JAXBException {

        Destination destination = session.createQueue("testQueue");

        MessageProducer messageSender = session.createProducer(destination);
        messageSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(convertXmlMessageToBytes());

        messageSender.send(bytesMessage);

        logger.info("отправляем сообщение в очередь ...");
    }

    public void sendViaTopic (TopicSession session) throws JMSException, JAXBException {

        Topic topic = session.createTopic("testTopic");
        TopicPublisher publisher = session.createPublisher(topic);

        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(convertXmlMessageToBytes());

        publisher.send(bytesMessage);

        logger.info("отправляем сообщение в топик ...");
    }
}
