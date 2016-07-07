package reqs;

import generated.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.xml.bind.JAXBException;


public class Test {
    private static DefaultMessageListenerContainer topicListenerFirst;
    private static DefaultMessageListenerContainer topicListenerSecond;
    private static DefaultMessageListenerContainer topicListenerNonDur;

    private static Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws JAXBException, InterruptedException {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("appCtx.xml");
        MessageSenderViaQueue messageSenderViaQueue = ctx.getBean(MessageSenderViaQueue.class);
        MessageSenderViaTopic messageSenderViaTopic = ctx.getBean(MessageSenderViaTopic.class);
        topicListenerFirst = (DefaultMessageListenerContainer) ctx.getBean("topicListenerFirst");
        topicListenerSecond = (DefaultMessageListenerContainer) ctx.getBean("topicListenerSecond");
        topicListenerNonDur = (DefaultMessageListenerContainer) ctx.getBean("topicListenerNonDur");

        logger.info("поднялся контекст");
        logger.info("==========================================================================================");
        logger.info("тестируем первый сценарий (отправка в очередь и чтение + отправка в топик и чтение (2 подписчика)");

        testProducerAndConsumer(messageSenderViaQueue, messageSenderViaTopic);

            Thread.sleep(1000);

        logger.info("==========================================================================================");
        logger.info("тестируем второй сценарий (отправка в топик, когда клиенты оффлайн (потеря сообщений)");
        testOfflineConsumer(messageSenderViaTopic, ctx);

            Thread.sleep(1000);

        logger.info("==========================================================================================");
        logger.info("тестируем третий сценарий (отправка в топик, когда клиент оффлайн и получение сообщения, когда клиент залогинился");
        testDurableConsumer(messageSenderViaTopic, ctx);

    }

    public static void testProducerAndConsumer (MessageSenderViaQueue messageSenderViaQueue, MessageSenderViaTopic messageSenderViaTopic) {
        Card queueCard = new Card();
        queueCard.setCardOwner("Alex - Queue");
        queueCard.setCardStatus("N");
        queueCard.setCardLimit(0);

        Card topicCard = new Card();
        topicCard.setCardOwner("Valera - Topic");
        topicCard.setCardStatus("N");
        topicCard.setCardLimit(0);

        logger.info("отправка запроса на чтение (queue)");
        messageSenderViaQueue.sendRequestToActivation(queueCard);

        logger.info("отправка запроса на чтение (topic)");
        messageSenderViaTopic.sendRequestToActivation(topicCard);
    }

    public static void testDurableConsumer (MessageSenderViaTopic messageSenderViaTopic, ClassPathXmlApplicationContext ctx) {
        Card topicCard = new Card();
        topicCard.setCardOwner("Alena - Topic");
        topicCard.setCardStatus("N");
        topicCard.setCardLimit(0);

        topicListenerFirst.stop();
        topicListenerSecond.stop();
        topicListenerNonDur.stop();
        logger.info("отключаем клиентов");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("отправляем сообщение в топик");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("подключаем клиентов  и получаем сохраненные сообщения");
        topicListenerSecond.start();
        topicListenerFirst.start();
    }

    public static void testOfflineConsumer(MessageSenderViaTopic messageSenderViaTopic, ClassPathXmlApplicationContext ctx) {
        Card topicCard = new Card();
        topicCard.setCardOwner("Sasha - Topic");
        topicCard.setCardStatus("N");
        topicCard.setCardLimit(0);

        logger.info("отключаем клиентов");
        topicListenerFirst.stop();
        topicListenerSecond.stop();
        topicListenerNonDur.stop();

        logger.info("отправляем сообщение");
        messageSenderViaTopic.sendRequestToActivation(topicCard);


        topicListenerNonDur.start();
        logger.info("подключаем клиентов и видим, что сообщения не приходят");
    }
}
