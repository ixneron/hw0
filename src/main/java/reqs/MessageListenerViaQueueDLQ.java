package reqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.jms.Message;
import javax.jms.MessageListener;

public class MessageListenerViaQueueDLQ implements MessageListener {

    private static Logger logger = LoggerFactory.getLogger(MessageListenerViaQueueDLQ.class);

    @Autowired
    private Jaxb2Marshaller marshaller;


    @Override
    public void onMessage(Message message) {

        logger.info("перехватываем сообщение, которое не удалось доставить (ролбэк, 7 попыток редоставки)");
        Magic.createMagic(message, logger, marshaller);
    }
}
