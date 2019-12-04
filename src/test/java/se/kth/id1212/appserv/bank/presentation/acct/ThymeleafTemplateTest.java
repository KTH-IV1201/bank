package se.kth.id1212.appserv.bank.presentation.acct;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import se.kth.id1212.appserv.bank.repository.DbUtil;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static se.kth.id1212.appserv.bank.presentation.PresentationTestHelper.addParam;
import static se.kth.id1212.appserv.bank.presentation.PresentationTestHelper.containsElements;
import static se.kth.id1212.appserv.bank.presentation.PresentationTestHelper.sendGetRequest;
import static se.kth.id1212.appserv.bank.presentation.PresentationTestHelper.sendPostRequest;

@SpringJUnitWebConfig(initializers = ConfigFileApplicationContextInitializer.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = { "se.kth.id1212.appserv.bank" })
// @SpringBootTest can be used instead of @SpringJUnitWebConfig,
// @EnableAutoConfiguration and @ComponentScan, but are we using
// JUnit5 in that case?
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, ThymeleafTemplateTest.class })
class ThymeleafTemplateTest implements TestExecutionListener {
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
    void testHeadingIsIncluded() throws Exception {
        sendGetRequest(mockMvc, AcctController.SELECT_ACCT_PAGE_URL).andExpect(status().isOk())
                .andExpect(containsElements("head link[href$=bank.css]"));
    }

    @Test
    void testHeaderIsIncluded() throws Exception {
        sendGetRequest(mockMvc, AcctController.SELECT_ACCT_PAGE_URL).andExpect(status().isOk())
                .andExpect(containsElements("header img[src$=/logo.png]"));
    }

    @Test
    void testNavigationIsIncluded() throws Exception {
        sendGetRequest(mockMvc, AcctController.SELECT_ACCT_PAGE_URL).andExpect(status().isOk())
                .andExpect(containsElements("nav>ul>li>a"));
    }

    @Test
    void testFooterIsIncluded() throws Exception {
        sendGetRequest(mockMvc, AcctController.SELECT_ACCT_PAGE_URL).andExpect(status().isOk())
                .andExpect(containsElements("footer"));
    }

    @Test
    void testContentIsIncluded() throws Exception {
        sendGetRequest(mockMvc, AcctController.SELECT_ACCT_PAGE_URL).andExpect(status().isOk())
                .andExpect(containsElements("main>section>h1:contains(Account)"));
    }

    @Test
    void testSelectAccountPageHasAllFragments() throws Exception {
        sendGetRequest(mockMvc, AcctController.SELECT_ACCT_PAGE_URL).andExpect(status().isOk())
                .andExpect(containsElements("head", "header", "nav", "main", "footer"));
    }

    @Test
    void testAccountPageHasAllFragments() throws Exception {
        HttpSession session = sendPostRequest(mockMvc, AcctController.CREATE_ACCT_URL,
                addParam(addParam("balance", "1"), "holderName", "ab")).andReturn().getRequest().getSession();
        sendGetRequest(mockMvc, AcctController.ACCT_PAGE_URL, session).andExpect(status().isOk())
                .andExpect(containsElements("head", "header", "nav", "main", "footer"));
    }

    @Test
    void testCorrectLanguageIsUsed() throws Exception {
        sendGetRequest(mockMvc, AcctController.SELECT_ACCT_PAGE_URL).andExpect(status().isOk())
                .andExpect(containsElements("footer>span:contains(Phone)"));
        sendGetRequest(mockMvc, AcctController.SELECT_ACCT_PAGE_URL + "?lang=sv").andExpect(status().isOk())
                .andExpect(containsElements("footer>span:contains(Telefon)"));
    }
}
