package se.kth.iv1201.appserv.bank.presentation.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import se.kth.iv1201.appserv.bank.presentation.acct.AcctController;

/**
 * Throws exceptions in order to facilitate testing exception handling.
 */
@Controller
public class ExceptionThrowingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcctController.class);

    static final String URL_THAT_THROWS_EXCEPTION = "throw-exception";

    /**
     * Used for exception handling tests.
     *
     * @throws Exception
     */
    @GetMapping("/" + URL_THAT_THROWS_EXCEPTION)
    public void throwException() throws Exception {
        LOGGER.trace("Call to {}.", URL_THAT_THROWS_EXCEPTION);
        throw new Exception("This is the exception message");
    }
}
