package reqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.jms.Message;
import javax.jms.MessageListener;

public class MessageListenerViaTopicSecondClient implements MessageListener{

    private static Logger logger = LoggerFactory.getLogger(MessageListenerViaTopicSecondClient.class);

    @Autowired
    private Jaxb2Marshaller marshaller;

    @Override
    public void onMessage(Message message) {
        Magic.createMagic(message, logger, marshaller);
    }
}
