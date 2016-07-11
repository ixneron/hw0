package reqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ErrorHandler;

public class MyErrorHandler implements ErrorHandler {

    private static Logger logger = LoggerFactory.getLogger(MyErrorHandler.class);
    @Override
    public void handleError(Throwable throwable) {
        logger.error("--->" + throwable.toString() + " --- " + throwable.getStackTrace()[0]);
    }
}
