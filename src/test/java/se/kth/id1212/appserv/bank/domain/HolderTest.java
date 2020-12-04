package se.kth.id1212.appserv.bank.domain;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
// import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

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
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
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
import se.kth.id1212.appserv.bank.repository.DbUtil;
import se.kth.id1212.appserv.bank.repository.HolderRepository;

@SpringJUnitWebConfig(initializers = ConfigFileApplicationContextInitializer.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = { "se.kth.id1212.appserv.bank" })
// @SpringBootTest can be used instead of @SpringJUnitWebConfig,
// @EnableAutoConfiguration and @ComponentScan, but are we using
// JUnit5 in that case?
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, HolderTest.class,
        TransactionalTestExecutionListener.class })
@NotThreadSafe
@Transactional
@Commit
class HolderTest implements TestExecutionListener {
    @Autowired
    private DbUtil dbUtil;
    @Autowired
    private HolderRepository repository;
    private Holder instance;

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
        instance = new Holder("holderName");
        dbUtil.emptyDb();
    }

    @Test
    void testAddedAcctIsStored() {
        Account acct = new Account(instance);
        assertThat(instance.getAccounts(), is(empty()));
        instance.addAccount(acct);
        assertThat(instance.getAccounts(), containsInAnyOrder(acct));
    }

    @Test
    void testHolderNoIsGenerated() {
        assertThat(instance.getHolderNo(), is(not(0L)));
    }

    @Test
    @Rollback
    void testNullName() {
        testInvalidHolder(new Holder(null), "{holder.name.missing}");
    }

    @Test
    @Rollback
    void testEmptyHolder() {
        testInvalidHolder(new Holder(""), "{holder.name.length}");
    }

    @Test
    @Rollback
    void testTooShortHolder() {
        testInvalidHolder(new Holder("a"), "{holder.name.length}");
    }

    @Test
    @Rollback
    void testTooLongHolder() {
        testInvalidHolder(new Holder("abcdeabcdeabcdeabcdeabcdeabcdep"), "{holder.name.length}");
    }

    @Test
    @Rollback
    void testHolderWithInvalidChar() {
        testInvalidHolder(new Holder("a."), "{holder.name.invalid-char}");
    }

    @Test
    void testValidHolderIsPersisted() throws IOException, SQLException, ClassNotFoundException {
        repository.save(instance);
        startNewTransaction();
        List<Holder> holdersInDb = repository.findAll();
        assertThat(holdersInDb, containsInAnyOrder(instance));
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
    void testNotEqualToOtherHolderWithSameName() {
        assertThat(instance.equals(new Holder(instance.getName())), is(not(true)));
    }

    @Test
    void testEqualToIdenticalHolder() {
        assertThat(instance.equals(instance), is(true));
    }

    @Test
    void testToString() {
        String stringRepresentation = instance.toString();
        assertThat(stringRepresentation, containsString("Holder"));
        assertThat(stringRepresentation, containsString("id"));
        assertThat(stringRepresentation, containsString("accounts"));
        assertThat(stringRepresentation, containsString("name"));
        assertThat(stringRepresentation, containsString("holderNo"));
        assertThat(stringRepresentation, containsString("optLockVersion"));
        assertThat(stringRepresentation, containsString(instance.getName()));
    }

    private void testInvalidHolder(Holder holder, String... expectedMsgs) {
        try {
            repository.save(holder);
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
