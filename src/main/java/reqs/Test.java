package reqs;

import generated.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.xml.bind.JAXBException;
import java.io.IOException;


public class Test {

    private static DefaultMessageListenerContainer topicListenerFirst;
    private static DefaultMessageListenerContainer topicListenerSecond;
    private static DefaultMessageListenerContainer topicListenerNonDur;

    private static Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws JAXBException, InterruptedException, IOException {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("appCtx.xml");
        MessageSenderViaQueue messageSenderViaQueue = ctx.getBean(MessageSenderViaQueue.class);
        MessageSenderViaTopic messageSenderViaTopic = ctx.getBean(MessageSenderViaTopic.class);
        topicListenerFirst = (DefaultMessageListenerContainer) ctx.getBean("topicListenerFirst");
        topicListenerSecond = (DefaultMessageListenerContainer) ctx.getBean("topicListenerSecond");
        topicListenerNonDur = (DefaultMessageListenerContainer) ctx.getBean("topicListenerNonDur");

        logger.info("поднялся контекст");
        System.out.println("===================================================================================================================");
        System.out.println("тестируем первый сценарий (отправка в очередь и чтение + отправка в топик и чтение (2 подписчика)");
        System.out.println("===================================================================================================================");

        testProducerAndConsumer(messageSenderViaQueue, messageSenderViaTopic);

        Thread.sleep(2000);

        System.out.println("===================================================================================================================");
        System.out.println("тестируем второй сценарий (отправка в топик, когда клиенты оффлайн (потеря сообщений)");
        System.out.println("===================================================================================================================");
        testOfflineConsumer(messageSenderViaTopic, ctx);

        Thread.sleep(2000);

        System.out.println("===================================================================================================================");
        System.out.println("старые сообщение :");
        System.out.println("===================================================================================================================");
        topicListenerFirst.initialize();
        topicListenerFirst.start();
        topicListenerSecond.initialize();
        topicListenerSecond.start();
        Thread.sleep(1500);
        System.out.println("===================================================================================================================");

        Thread.sleep(2000);

        System.out.println("тестируем третий сценарий (отправка в топик, когда клиент оффлайн и получение сообщения, когда клиент залогинился");
        System.out.println("=================================================================================================================== ");
        testDurableConsumer(messageSenderViaTopic, ctx);

        Thread.sleep(2000);

        System.out.println("===================================================================================================================");
        System.out.println("теструем четвертый сценарий (отправка сообщение в очередь А, сразу после получения сообщения из очереди B");
        System.out.println("===================================================================================================================");
        testResendFromQToQ(messageSenderViaQueue);

        Thread.sleep(2000);

        System.out.println("===================================================================================================================");
        System.out.println("тестируем пятый сценарий (откат транзакции)");
        System.out.println("===================================================================================================================");
        testTransact(messageSenderViaQueue);

        Thread.sleep(11000);
        System.exit(0);
    }

    public static void testProducerAndConsumer (MessageSenderViaQueue messageSenderViaQueue, MessageSenderViaTopic messageSenderViaTopic) {
        Card queueCard = new Card();
        queueCard.setCardOwner("Alex - Queue");
        queueCard.setCardStatus("inactive");
        queueCard.setCardLimit(0);

        Card topicCard = new Card();
        topicCard.setCardOwner("Valera - Topic");
        topicCard.setCardStatus("inactive");
        topicCard.setCardLimit(0);

        logger.info("отправка запроса на чтение (queue)");
        messageSenderViaQueue.sendRequestToActivation(queueCard);

        logger.info("отправка запроса на чтение (topic)");
        messageSenderViaTopic.sendRequestToActivation(topicCard);
    }

    public static void testDurableConsumer (MessageSenderViaTopic messageSenderViaTopic, ClassPathXmlApplicationContext ctx) throws InterruptedException, IOException {
        Card topicCard = new Card();
        topicCard.setCardOwner("Alena - Topic");
        topicCard.setCardStatus("inactive");
        topicCard.setCardLimit(0);

        topicListenerFirst.stop();
        topicListenerFirst.shutdown();
        topicListenerSecond.stop();
        topicListenerSecond.shutdown();
        topicListenerNonDur.stop();
        topicListenerNonDur.shutdown();
        logger.info("отключаем клиентов");

        Thread.sleep(1000);

        logger.info("отправляем сообщение в топик");
        messageSenderViaTopic.sendRequestToActivation(topicCard);

        topicListenerFirst.initialize();
        topicListenerFirst.start();
        topicListenerSecond.initialize();
        topicListenerSecond.start();
        topicListenerNonDur.initialize();
        topicListenerNonDur.start();

        Thread.sleep(1500);

        logger.info("2 из 3 подписчиков получили сообщения.");
    }

    public static void testOfflineConsumer(MessageSenderViaTopic messageSenderViaTopic, ClassPathXmlApplicationContext ctx) throws InterruptedException, IOException {

        Card topicCard = new Card();
        topicCard.setCardOwner("Sasha - Topic");
        topicCard.setCardStatus("inactive");
        topicCard.setCardLimit(0);

        logger.info("отключаем клиентов");

        topicListenerFirst.stop();
        topicListenerFirst.shutdown();
        topicListenerSecond.stop();
        topicListenerSecond.shutdown();
        topicListenerNonDur.stop();
        topicListenerNonDur.shutdown();

        logger.info("отправляем сообщение");
        messageSenderViaTopic.sendRequestToActivation(topicCard);

        Thread.sleep(2000);


        topicListenerNonDur.initialize();
        topicListenerNonDur.start();
        logger.info("вновь подключенные подписчики не получили сообщений");
    }

    public static void testResendFromQToQ(MessageSenderViaQueue messageSenderViaQueue) {

        Card resendCard = new Card();
        resendCard.setCardOwner("roma - resender");
        resendCard.setCardStatus("inactive");
        resendCard.setCardLimit(0);

        messageSenderViaQueue.sendRequestToActivation(resendCard);

    }

    public static void testTransact(MessageSenderViaQueue messageSenderViaQueue) throws InterruptedException {

        Card trCard = new Card();
        trCard.setCardOwner("katya-transaction");
        trCard.setCardStatus("inactive");
        trCard.setCardLimit(0);

        messageSenderViaQueue.sendRequestToActivation(trCard);
    }
}
