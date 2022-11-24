package se.kth.iv1201.appserv.bank.domain;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
// import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import net.jcip.annotations.NotThreadSafe;
import se.kth.iv1201.appserv.bank.repository.AccountRepository;
import se.kth.iv1201.appserv.bank.repository.DbUtil;

@SpringJUnitWebConfig(initializers = ConfigDataApplicationContextInitializer.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = { "se.kth.iv1201.appserv.bank" })
// @SpringBootTest can be used instead of @SpringJUnitWebConfig,
// @EnableAutoConfiguration and @ComponentScan, but are we using
// JUnit5 in that case?

// Med @SpringBootTest sker anropet över HTTP, dvs "på riktigt" och inte med mockad server.
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, AccountTest.class,
        TransactionalTestExecutionListener.class })
@NotThreadSafe
@Transactional
@Commit
class AccountTest implements TestExecutionListener {
    @Autowired
    private DbUtil dbUtil;
    @Autowired
    private AccountRepository repository;
    private Account instance;

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
    void setup() throws SQLException, IOException, ClassNotFoundException {
        instance = new Account(new Holder("holderName"), 10);
        dbUtil.emptyDb();
    }

    @Test
    void testAcctNoIsGenerated() {
        assertThat(instance.getAcctNo(), is(not(0L)));
    }

    @Test
    @Rollback
    void testNegBalance() {
        testInvalidAcct(new Account(instance.getHolder(), -10), "{acct.balance.negative}");
    }

    @Test
    @Rollback
    void testMissingHolder() {
        testInvalidAcct(new Account(null, 10), "{acct.holder.missing}");
    }

    @Test
    void testValidAcctIsPersisted() throws IOException, SQLException, ClassNotFoundException {
        repository.save(instance);
        startNewTransaction();
        List<Account> holdersInDb = repository.findAll();
        assertThat(holdersInDb, containsInAnyOrder(instance));
    }

    @Test
    void testZeroBalanceIsValid() throws IOException, SQLException, ClassNotFoundException {
        Account acctWithZeroBalance = new Account(instance.getHolder(), 0);
        repository.save(acctWithZeroBalance);
        startNewTransaction();
        List<Account> holdersInDb = repository.findAll();
        assertThat(holdersInDb, containsInAnyOrder(acctWithZeroBalance));
    }

    @Test
    void testNotEqualToNull() {
        assertThat(instance.equals(null), is(not(true)));
    }

    @Test
    void testNotEqualToObjectOfOtherClass() {
        assertThat(instance.equals(new Object()), is(not(true)));
    }

    @Test
    void testEqualToIdenticalAcct() {
        assertThat(instance.equals(instance), is(true));
    }

    @Test
    void testToString() {
        String stringRepresentation = instance.toString();
        assertThat(stringRepresentation, containsString("Account"));
        assertThat(stringRepresentation, containsString("id"));
        assertThat(stringRepresentation, containsString("balance"));
        assertThat(stringRepresentation, containsString("acctNo"));
        assertThat(stringRepresentation, containsString("holder"));
        assertThat(stringRepresentation, containsString("optLockVersion"));
        assertThat(stringRepresentation, containsString(Integer.toString(instance.getBalance())));
    }

    private void testInvalidAcct(Account acct, String... expectedMsgs) {
        try {
            startNewTransaction();
            repository.save(acct);
        } catch (ConstraintViolationException exc) {
            Set<ConstraintViolation<?>> result = exc.getConstraintViolations();
            assertThat(result.size(), is(expectedMsgs.length));
            for (String expectedMsg : expectedMsgs) {
                assertThat(result, hasItem(hasProperty("messageTemplate", equalTo(expectedMsg))));
            }
        }
    }

    private void startNewTransaction() {
        TestTransaction.end();
        TestTransaction.start();
    }
}
