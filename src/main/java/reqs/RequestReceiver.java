package reqs;

import generated.Card;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.ByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.jms.*;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

@Component
public class RequestReceiver implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(RequestReceiver.class);

    @Autowired
    ActiveMQConnectionFactory mqConnectionFactory;

    @Override
    public void run() {
        try {
            QueueConnection queueConnection = mqConnectionFactory.createQueueConnection();
            queueConnection.start();
            QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);


            TopicConnection topicConnection = mqConnectionFactory.createTopicConnection();
            topicConnection.start();
            TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            TopicConnection topicConnection2 = mqConnectionFactory.createTopicConnection();
            topicConnection.start();
            TopicSession topicSession2 = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            logger.info("созданы соединения на получение сообщений");

            receiveFromTopic(topicSession,topicSession2);
            receiveFromQueue(queueSession);


            queueSession.close();
            queueConnection.close();
            topicSession.close();
            topicConnection.close();
            topicSession2.close();
            topicConnection2.close();

            logger.info("receive соединения закрыты");
        } catch (JMSException | JAXBException e) {
            e.printStackTrace();
        }
    }

    private void receiveFromQueue (QueueSession queueSession) throws JMSException, JAXBException {

        Destination destination = queueSession.createQueue("testQueue");
        MessageConsumer receiver = queueSession.createConsumer(destination);

        Message message = receiver.receive(1000);
        upgradeMessage(message);
        System.out.println("очередник");
    }

    public void receiveFromTopic (TopicSession topicSession, TopicSession topicSession2) throws JMSException, JAXBException {

        Topic topic = topicSession.createTopic("testTopic");
        TopicSubscriber subscriber = topicSession.createSubscriber(topic);
        TopicSubscriber subscriber1 = topicSession2.createSubscriber(topic);

        Message message = subscriber.receive();
        upgradeMessage(message);
        System.out.println("первый саб");
        Message message2 = subscriber1.receive();
        upgradeMessage(message2);
        System.out.println("второй саб");

    }

    private void convertBytesToXmlMessage(byte[] content) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(Card.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        Card card = (Card) unmarshaller.unmarshal(new ByteArrayInputStream(content));
        validateMessage(card);

        System.out.println(card.getCardOwner() + " " + card.getCardStatus());
    }

    private void validateMessage (Card card) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(Card.class);
        JAXBSource source = new JAXBSource(context,card);

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        try {

            Schema schema = schemaFactory.newSchema(new File("C:/JAVA/hw1/src/main/resources/card.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(source);

        } catch (SAXException e) {
            System.out.println("validation fail!");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void upgradeMessage(Message message) throws JMSException, JAXBException {

        if (message instanceof BytesMessage){
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] content = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(content);
            convertBytesToXmlMessage(content);
        }
    }

}

