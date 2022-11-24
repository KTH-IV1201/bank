package se.kth.iv1201.appserv.bank.presentation.acct;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static se.kth.iv1201.appserv.bank.presentation.PresentationTestHelper.addParam;
import static se.kth.iv1201.appserv.bank.presentation.PresentationTestHelper.containsElements;
import static se.kth.iv1201.appserv.bank.presentation.PresentationTestHelper.doesNotContainElements;
import static se.kth.iv1201.appserv.bank.presentation.PresentationTestHelper.sendGetRequest;
import static se.kth.iv1201.appserv.bank.presentation.PresentationTestHelper.sendPostRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import se.kth.iv1201.appserv.bank.domain.Account;
import se.kth.iv1201.appserv.bank.domain.AccountDTO;
import se.kth.iv1201.appserv.bank.domain.Holder;
import se.kth.iv1201.appserv.bank.presentation.error.ExceptionHandlers;
import se.kth.iv1201.appserv.bank.repository.AccountRepository;
import se.kth.iv1201.appserv.bank.repository.DbUtil;

@SpringJUnitWebConfig(initializers = ConfigDataApplicationContextInitializer.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = { "se.kth.iv1201.appserv.bank" })
// @SpringBootTest can be used instead of @SpringJUnitWebConfig,
// @EnableAutoConfiguration and @ComponentScan, but are we using
// JUnit5 in that case?
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, AcctControllerTest.class,
        TransactionalTestExecutionListener.class })
@Transactional
@Commit
class AcctControllerTest implements TestExecutionListener {
    @Autowired
    private DbUtil dbUtil;
    @Autowired
    AccountRepository acctRepo;
    @Autowired
    private WebApplicationContext webappContext;
    private MockMvc mockMvc;
    private Account acct;
    private Holder holder;

    @Override
    public void beforeTestClass(TestContext testContext) throws SQLException, IOException, ClassNotFoundException {
        dbUtil = testContext.getApplicationContext().getBean(DbUtil.class);
        enableCreatingEMFWhichIsNeededForTheApplicationContext();
    }

    @Override
    public void afterTestClass(TestContext testContext) throws SQLException, IOException, ClassNotFoundException {
        enableCreatingEMFWhichIsNeededForTheApplicationContext();
    }

    private void enableCreatingEMFWhichIsNeededForTheApplicationContext()
            throws SQLException, IOException, ClassNotFoundException {
        dbUtil.emptyDb();
    }

    @BeforeEach
    void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webappContext).build();
        dbUtil.emptyDb();
        holder = new Holder("holderName");
        acct = new Account(holder, 10);
    }

    @Test
    void testCorrectViewForDeafultUrl() throws Exception {
        sendGetRequest(mockMvc, "").andExpect(status().is3xxRedirection()).andExpect(header().exists("Location"));
    }

    @Test
    void testCorrectViewForSelectAcctUrl() throws Exception {
        sendGetRequest(mockMvc, AcctController.SELECT_ACCT_PAGE_URL).andExpect(status().isOk())
                .andExpect(isSelectAcctPage());
    }

    @Test
    void testCreateCorrectParams() throws Exception {
        sendPostRequest(mockMvc, AcctController.CREATE_ACCT_URL,
                addParam(addParam("balance", Integer.toString(acct.getBalance())), "holderName", holder.getName()))
                        .andExpect(status().isOk()).andExpect(isAcctPage())
                        .andExpect(doesNotContainElements("span.error"));
        startNewTransaction();
        List<Account> acctsInDb = acctRepo.findAll();
        assertThat(acctsInDb.size(), is(1));
        assertThat(acctsInDb, hasItem(hasProperty("balance", equalTo(acct.getBalance()))));
        assertThat(acctsInDb.get(0).getHolder().getName(), is(holder.getName()));
    }

    @Test
    void testCreateAndChangeLang() throws Exception {
        sendPostRequest(mockMvc, AcctController.CREATE_ACCT_URL,
                addParam(addParam("balance", "-1"), "holderName", "ab")).andExpect(status().isOk())
                        .andExpect(isSelectAcctPage());
        sendGetRequest(mockMvc, AcctController.CREATE_ACCT_URL + "?lang=en").andExpect(status().isOk())
                .andExpect(isSelectAcctPage());
    }

    @Test
    void testCreateAcctTooShortHolder() throws Exception {
        sendPostRequest(mockMvc, AcctController.CREATE_ACCT_URL, addParam(addParam("balance", "1"), "holderName", "1"))
                .andExpect(status().isOk()).andExpect(isSelectAcctPage())
                .andExpect(containsErrorMsg("create-acct-holder", "2"));
    }

    @Test
    void testCreateAcctMissingHolder() throws Exception {
        sendPostRequest(mockMvc, AcctController.CREATE_ACCT_URL, addParam(addParam("balance", "1"), "holderName", ""))
                .andExpect(status().isOk()).andExpect(isSelectAcctPage())
                .andExpect(containsErrorMsg("create-acct-holder", "specify " + "account " + "holder"));
    }

    @Test
    void testCreateAcctWrongCharInHolder() throws Exception {
        sendPostRequest(mockMvc, AcctController.CREATE_ACCT_URL,
                addParam(addParam("balance", "1"), "holderName", "123")).andExpect(status().isOk())
                        .andExpect(isSelectAcctPage())
                        .andExpect(containsErrorMsg("create-acct-holder", "only letters"));
    }

    @Test
    void testCreateAcctTooLongHolder() throws Exception {
        sendPostRequest(mockMvc, AcctController.CREATE_ACCT_URL,
                addParam(addParam("balance", "1"), "holderName", "1234567890123456789012345678901"))
                        .andExpect(status().isOk()).andExpect(isSelectAcctPage())
                        .andExpect(containsErrorMsg("create-acct-holder", "30"));
    }

    @Test
    void testCreateAcctMissingBalance() throws Exception {
        sendPostRequest(mockMvc, AcctController.CREATE_ACCT_URL, addParam("holderName", "12"))
                .andExpect(status().isOk()).andExpect(isSelectAcctPage())
                .andExpect(containsErrorMsg("create-balance", "specify balance"));
    }

    @Test
    void testCreateAcctWrongCharInBalance() throws Exception {
        sendPostRequest(mockMvc, AcctController.CREATE_ACCT_URL,
                addParam(addParam("balance", "a"), "holderName", "abc")).andExpect(status().isOk())
                        .andExpect(isSelectAcctPage()).andExpect(containsErrorMsg("create-balance", "only numbers"));
    }

    @Test
    void testCreateAcctNegativeBalance() throws Exception {
        sendPostRequest(mockMvc, AcctController.CREATE_ACCT_URL,
                addParam(addParam("balance", "-1"), "holderName", "12")).andExpect(status().isOk())
                        .andExpect(isSelectAcctPage()).andExpect(containsErrorMsg("create-balance", "zero or greater"));
    }

    @Test
    void testFindExistingAcct() throws Exception {
        acctRepo.save(acct);
        startNewTransaction();
        sendPostRequest(mockMvc, AcctController.FIND_ACCT_URL, addParam("number", Long.toString(acct.getAcctNo())))
                .andExpect(status().isOk()).andExpect(isAcctPage()).andExpect(doesNotContainElements("span.error"));
        AccountDTO foundAcct = acctRepo.findAccountByAcctNo(acct.getAcctNo());
        assertThat(foundAcct.getAcctNo(), is(acct.getAcctNo()));
    }

    @Test
    void testFindNonExistingAcct() throws Exception {
        sendPostRequest(mockMvc, AcctController.FIND_ACCT_URL, addParam("number", Long.toString(acct.getAcctNo())))
                .andExpect(status().isOk()).andExpect(isErrorPage())
                .andExpect(containsElements("main h1:contains(No Account Found)"));
    }

    @Test
    void testFindAndChangeLang() throws Exception {
        sendPostRequest(mockMvc, AcctController.FIND_ACCT_URL, addParam("number", "a")).andExpect(status().isOk())
                .andExpect(isSelectAcctPage());
        sendGetRequest(mockMvc, AcctController.FIND_ACCT_URL + "?lang=en").andExpect(status().isOk())
                .andExpect(isSelectAcctPage());
    }

    @Test
    void testFindAcctMissingAcctNo() throws Exception {
        sendPostRequest(mockMvc, AcctController.FIND_ACCT_URL).andExpect(status().isOk()).andExpect(isSelectAcctPage())
                .andExpect(containsErrorMsg("search-acct-number", "specify " + "account " + "number"));
    }

    @Test
    void testFindAcctWrongCharInAcctNo() throws Exception {
        sendPostRequest(mockMvc, AcctController.FIND_ACCT_URL, addParam("number", "a")).andExpect(status().isOk())
                .andExpect(isSelectAcctPage()).andExpect(containsErrorMsg("search-acct-number", "only numbers"));
    }

    @Test
    void testDepositCorrectParams() throws Exception {
        int amtToDeposit = 1;
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.DEPOSIT_URL, session,
                addParam("amount", Integer.toString(amtToDeposit))).andExpect(status().isOk()).andExpect(isAcctPage())
                        .andExpect(containsElements(
                                "span:contains(balance)+span:contains(" + (acct.getBalance() + amtToDeposit) + ")"));
        startNewTransaction();
        List<Account> acctsInDb = acctRepo.findAll();
        assertThat(acctsInDb.size(), is(1));
        assertThat(acctsInDb, hasItem(hasProperty("balance", equalTo(acct.getBalance() + amtToDeposit))));
    }

    @Test
    void testDepositWhenNoCurrentAcct() throws Exception {
        int amtToDeposit = 1;
        sendPostRequest(mockMvc, AcctController.DEPOSIT_URL, addParam("amount", Integer.toString(amtToDeposit)))
                .andExpect(status().isInternalServerError()).andExpect(isErrorPage())
                .andExpect(containsElements("main h1:contains(Deposit Failed)"));
        startNewTransaction();
        List<Account> acctsInDb = acctRepo.findAll();
        assertThat(acctsInDb, empty());
    }

    @Test
    void testDepositAndChangeLang() throws Exception {
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.DEPOSIT_URL, session, addParam("amount", "a"))
                .andExpect(status().isOk()).andExpect(isAcctPage());
        sendGetRequest(mockMvc, AcctController.DEPOSIT_URL + "?lang=en", session).andExpect(status().isOk())
                .andExpect(isAcctPage());
    }

    @Test
    void testDepositMissingAmt() throws Exception {
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.DEPOSIT_URL, session).andExpect(status().isOk()).andExpect(isAcctPage())
                .andExpect(containsErrorMsg("deposit-amt", "specify amount"));
    }

    @Test
    void testDepositWrongCharInAmt() throws Exception {
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.DEPOSIT_URL, session, addParam("amount", "a"))
                .andExpect(status().isOk()).andExpect(isAcctPage())
                .andExpect(containsErrorMsg("deposit-amt", "only numbers"));
    }

    @Test
    void testDepositNegAmt() throws Exception {
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.DEPOSIT_URL, session, addParam("amount", "-1"))
                .andExpect(status().isOk()).andExpect(isAcctPage())
                .andExpect(containsErrorMsg("deposit-amt", "greater than zero"));
    }

    @Test
    void testDepositZeroAmt() throws Exception {
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.DEPOSIT_URL, session, addParam("amount", "0"))
                .andExpect(status().isOk()).andExpect(isAcctPage())
                .andExpect(containsErrorMsg("deposit-amt", "greater than zero"));
    }

    @Test
    void testWithdrawCorrectParams() throws Exception {
        int amtToWithdraw = 1;
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.WITHDRAW_URL, session,
                addParam("amount", Integer.toString(amtToWithdraw))).andExpect(status().isOk()).andExpect(isAcctPage())
                        .andExpect(containsElements(
                                "span:contains(balance)+span:contains(" + (acct.getBalance() - amtToWithdraw) + ")"));
        startNewTransaction();
        List<Account> acctsInDb = acctRepo.findAll();
        assertThat(acctsInDb.size(), is(1));
        assertThat(acctsInDb, hasItem(hasProperty("balance", equalTo(acct.getBalance() - amtToWithdraw))));
    }

    @Test
    void testWithdrawAmtGreaterThanBalance() throws Exception {
        int amtToWithdraw = acct.getBalance() + 1;
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.WITHDRAW_URL, session,
                addParam("amount", Integer.toString(amtToWithdraw))).andExpect(status().isInternalServerError())
                        .andExpect(isErrorPage()).andExpect(containsElements("main h1:contains(Withdrawal Failed)"));
        startNewTransaction();
        List<Account> acctsInDb = acctRepo.findAll();
        assertThat(acctsInDb.size(), is(1));
        assertThat(acctsInDb, hasItem(hasProperty("balance", equalTo(acct.getBalance()))));
    }

    @Test
    void testWithdrawWhenNoCurrentAcct() throws Exception {
        int amtToWithdraw = 1;
        sendPostRequest(mockMvc, AcctController.WITHDRAW_URL, addParam("amount", Integer.toString(amtToWithdraw)))
                .andExpect(status().isInternalServerError()).andExpect(isErrorPage())
                .andExpect(containsElements("main h1:contains(Withdrawal Failed)"));
        startNewTransaction();
        List<Account> acctsInDb = acctRepo.findAll();
        assertThat(acctsInDb, empty());
    }

    @Test
    void testWithdrawAndChangeLang() throws Exception {
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.WITHDRAW_URL, session, addParam("amount", "a"))
                .andExpect(status().isOk()).andExpect(isAcctPage());
        sendGetRequest(mockMvc, AcctController.WITHDRAW_URL + "?lang=en", session).andExpect(status().isOk())
                .andExpect(isAcctPage());
    }

    @Test
    void testWithdrawMissingAmt() throws Exception {
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.WITHDRAW_URL, session).andExpect(status().isOk())
                .andExpect(isAcctPage()).andExpect(containsErrorMsg("withdraw-amt", "specify amount"));
    }

    @Test
    void testWithdrawWrongCharInAmt() throws Exception {
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.WITHDRAW_URL, session, addParam("amount", "a"))
                .andExpect(status().isOk()).andExpect(isAcctPage())
                .andExpect(containsErrorMsg("withdraw-amt", "only numbers"));
    }

    @Test
    void testWithdrawNegAmt() throws Exception {
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.WITHDRAW_URL, session, addParam("amount", "-1"))
                .andExpect(status().isOk()).andExpect(isAcctPage())
                .andExpect(containsErrorMsg("withdraw-amt", "greater than zero"));
    }

    @Test
    void testWithdrawZeroAmt() throws Exception {
        HttpSession session = createSessionWithContrThatHasAcct();
        sendPostRequest(mockMvc, AcctController.WITHDRAW_URL, session, addParam("amount", "0"))
                .andExpect(status().isOk()).andExpect(isAcctPage())
                .andExpect(containsErrorMsg("withdraw-amt", "greater than zero"));
    }

    @Test
    void testCorrectViewForAcctUrl() throws Exception {
        HttpSession session = createSessionWithContrThatHasAcct();
        sendGetRequest(mockMvc, AcctController.ACCT_PAGE_URL, session).andExpect(status().isOk())
                .andExpect(isAcctPage());
    }

    private HttpSession createSessionWithContrThatHasAcct() throws Exception {
        return sendPostRequest(mockMvc, AcctController.CREATE_ACCT_URL,
                addParam(addParam("balance", Integer.toString(acct.getBalance())), "holderName", holder.getName()))
                        .andReturn().getRequest().getSession();
    }

    private ResultMatcher containsErrorMsg(String idForErrorElem, String errorMsg) {
        return containsElements("input[id=" + idForErrorElem + "]" + "~span.error:contains(" + errorMsg + ")");
    }

    private ResultMatcher isAcctPage() {
        return containsElements("main h1:contains(Deposit)", "main h1:contains(Withdraw)");
    }

    private ResultMatcher isSelectAcctPage() {
        return containsElements("main h1:contains(Search Account)", "main h1:contains(Create Account)");
    }

    private ResultMatcher isErrorPage() {
        return view().name(is(ExceptionHandlers.ERROR_PAGE_URL));
    }

    private void startNewTransaction() {
        TestTransaction.end();
        TestTransaction.start();
    }
}
