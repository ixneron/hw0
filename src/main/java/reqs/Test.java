package reqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.bind.JAXBException;


public class Test {

    private static Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws JAXBException {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("appCtx.xml");
        RequestReceiver requestReceiver = ctx.getBean(RequestReceiver.class);
        RequestSender requestSender = ctx.getBean(RequestSender.class);

        logger.info("поднялся контекст");


        Thread t = new Thread(requestSender);
        Thread r = new Thread(requestReceiver);

        logger.info("запускаются потоки");

        t.start();
        r.start();
    }

}
