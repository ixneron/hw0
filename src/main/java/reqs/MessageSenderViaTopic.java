package reqs;

import generated.Card;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.jms.*;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;


public class MessageSenderViaTopic {

    private final JmsTemplate jmsTemplate;

    @Autowired
    private Jaxb2Marshaller marshaller;

    public MessageSenderViaTopic(final JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendRequestToActivation(Card card) {

        jmsTemplate.send(session -> {
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(convertObjToXML(card));
            return textMessage;
        });
    }

    private String convertObjToXML (Card card) {

        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        marshaller.marshal(card, result);
        return writer.toString();
    }

//    public void sendDurableRequestToActivation(Card card) {
//        try {
//            Connection connection = jmsTemplate.getConnectionFactory().createConnection();
//            connection.setClientID("100500");
//            connection.start();
//
//            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//            TextMessage textMessage = session.createTextMessage();
//            textMessage.setText(convertObjToXML(card));
//
//            MessageProducer producer = session.createProducer(jmsTemplate.getDefaultDestination());
//            producer.send(textMessage);
//
//            session.close();
//            connection.close();
//        } catch (JMSException e) {
//            e.printStackTrace();
//        }
//    }
}
