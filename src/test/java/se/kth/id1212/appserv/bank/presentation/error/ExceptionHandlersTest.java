package se.kth.id1212.appserv.bank.presentation.error;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import se.kth.id1212.appserv.bank.repository.DbUtil;

import javax.servlet.RequestDispatcher;

import java.io.IOException;
import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static se.kth.id1212.appserv.bank.presentation.PresentationTestHelper.containsElements;

@SpringJUnitWebConfig(initializers = ConfigFileApplicationContextInitializer.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = { "se.kth.id1212.appserv.bank" })
// @SpringBootTest can be used instead of @SpringJUnitWebConfig,
// @EnableAutoConfiguration and @ComponentScan, but are we using
// JUnit5 in that case?
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, ExceptionHandlersTest.class })
class ExceptionHandlersTest implements TestExecutionListener {
    @Autowired
    private DbUtil dbUtil;
    @Autowired
    private WebApplicationContext webappContext;
    private MockMvc mockMvc;

    @Override
    public void beforeTestClass(TestContext testContext) throws SQLException, IOException, ClassNotFoundException {
        dbUtil = testContext.getApplicationContext().getBean(DbUtil.class);
        enableCreatingEMFWhichIsNeededForTheApplicationContext();
    }

    private void enableCreatingEMFWhichIsNeededForTheApplicationContext()
            throws SQLException, IOException, ClassNotFoundException {
        dbUtil.emptyDb();
    }

    @BeforeEach
    void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webappContext).build();
    }

    @Test
    void testFallbackExceptionHandler() throws Exception {
        sendGetRequest(ExceptionThrowingController.URL_THAT_THROWS_EXCEPTION)
                .andExpect(status().isInternalServerError()).andExpect(isGenericErrorPage());
    }

    @Test
    void testHttp404() throws Exception {
        /*
         * It is impossible to test http status codes like below, since MockMvc doesn't
         * fully support forwarding requests, which is what the error page support uses.
         * See https://github.com/spring-projects/spring-boot/issues/5574 Instead make a
         * call directly to the error handling method, using the http status code that
         * shall be tested.
         * 
         * sendGetRequest("thereisnothingatthisurl")
         * .andExpect(status().isNotFound()).andExpect(isNotFoundErrorPage());
         */
        sendGetRequestWithStatusCode(ExceptionHandlers.ERROR_PATH, 404).andExpect(status().isNotFound())
                .andExpect(isNotFoundErrorPage());

    }

    @Test
    void testUnhandledHttpStatus() throws Exception {
        /*
         * It is impossible to test http status codes like below, since MockMvc doesn't
         * fully support forwarding requests, which is what the error page support uses.
         * See https://github.com/spring-projects/spring-boot/issues/5574 Instead make a
         * call directly to the error handling method, using the http status code that
         * shall be tested.
         * 
         * sendGetRequest("thereisnothingatthisurl")
         * .andExpect(status().isNotFound()).andExpect(isNotFoundErrorPage());
         */
        sendGetRequestWithStatusCode(ExceptionHandlers.ERROR_PATH, 502).andExpect(status().isBadGateway())
                .andExpect(isGenericErrorPage());
    }

    private ResultActions sendGetRequest(String Url) throws Exception {
        return mockMvc.perform(get("/" + Url)); // no context path in Url
        // since we are not using any server.
    }

    private ResultActions sendGetRequestWithStatusCode(String Url, int statusCode) throws Exception {
        return mockMvc.perform(get("/" + Url).requestAttr(RequestDispatcher.ERROR_STATUS_CODE, statusCode));
        // no context path in Url since we are not using any server.
    }

    private ResultMatcher isGenericErrorPage() {
        return containsElements("main h1:contains(Operation Failed)");
    }

    private ResultMatcher isNotFoundErrorPage() {
        return containsElements("main h1:contains(could not be found)");
    }
}
