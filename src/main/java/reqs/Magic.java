package reqs;

import generated.Card;
import org.slf4j.Logger;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

public class Magic {

    public static synchronized void createMagic(Message message, Logger logger, Jaxb2Marshaller marshaller) {

        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                Card card = (Card) marshaller.unmarshal(new StreamSource(new StringReader(textMessage.getText())));
                logger.info("получено сообщение -- владелец карты : " + card.getCardOwner() + "- лимит :" + card.getCardLimit() + "- статус :" + card.getCardStatus() + " " + message.getJMSMessageID());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

}
