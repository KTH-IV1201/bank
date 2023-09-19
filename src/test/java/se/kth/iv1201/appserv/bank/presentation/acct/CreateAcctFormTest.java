package se.kth.iv1201.appserv.bank.presentation.acct;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import se.kth.iv1201.appserv.bank.repository.DbUtil;

@SpringJUnitWebConfig(initializers = ConfigDataApplicationContextInitializer.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = { "se.kth.iv1201.appserv.bank" })
// @SpringBootTest can be used instead of @SpringJUnitWebConfig,
// @EnableAutoConfiguration and @ComponentScan, but are we using
// JUnit5 in that case?
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, CreateAcctFormTest.class })
class CreateAcctFormTest implements TestExecutionListener {
    @Autowired
    private DbUtil dbUtil;
    @Autowired
    private Validator validator;

    @Override
    public void beforeTestClass(TestContext testContext) throws SQLException, IOException, ClassNotFoundException {
        dbUtil = testContext.getApplicationContext().getBean(DbUtil.class);
        enableCreatingEMFWhichIsNeededForTheApplicationContext();
    }

    private void enableCreatingEMFWhichIsNeededForTheApplicationContext()
            throws SQLException, IOException, ClassNotFoundException {
        dbUtil.emptyDb();
    }

    @Test
    void testNegBalance() {
        testInvalidBalance(-1, "{create-acct.balance.negative}");
    }

    @Test
    void testNullBalance() {
        testInvalidBalance(null, "{create-acct.balance.missing}");
    }

    @Test
    void testCorrectPosBalance() {
        testvalidBalance(1);
    }

    @Test
    void testCorrectZeroBalance() {
        testvalidBalance(0);
    }

    @Test
    void testNullHolder() {
        testInvalidHolder(null, "{create-acct.holder-name.missing}");
    }

    @Test
    void testEmptyHolder() {
        testInvalidHolder("", "{create-acct.holder-name.length}", "{create-acct.holder-name.missing}");
    }

    @Test
    void testTooShortHolder() {
        testInvalidHolder("a", "{create-acct.holder-name.length}");
    }

    @Test
    void testTooLongHolder() {
        testInvalidHolder("abcdeabcdeabcdeabcdeabcdeabcdep", "{create-acct" + ".holder-name" + ".length}");
    }

    @Test
    void testHolderWithInvalidChar() {
        testInvalidHolder("a.", "{create-acct.holder-name.invalid-char}");
    }

    @Test
    void testCorrectHolder() {
        testValidHolder("abc");
    }

    @Test
    void testCorrectHolderWithNonLatinChars() {
        testValidHolder("åäöÅÄÖéèëê");
    }

    private void testvalidBalance(Integer validBalance) {
        String validHolder = "abc";
        CreateAcctForm sut = new CreateAcctForm();
        sut.setHolderName(validHolder);
        sut.setBalance(validBalance);
        Set<ConstraintViolation<CreateAcctForm>> result = validator.validate(sut);
        assertThat(result, is(empty()));
    }

    private void testValidHolder(String validHolder) {
        int validBalance = 1;
        CreateAcctForm sut = new CreateAcctForm();
        sut.setHolderName(validHolder);
        sut.setBalance(validBalance);
        Set<ConstraintViolation<CreateAcctForm>> result = validator.validate(sut);
        assertThat(result, is(empty()));
    }

    private void testInvalidBalance(Integer invalidBalance, String expectedMsg) {
        String validHolder = "abc";
        CreateAcctForm sut = new CreateAcctForm();
        sut.setHolderName(validHolder);
        sut.setBalance(invalidBalance);
        Set<ConstraintViolation<CreateAcctForm>> result = validator.validate(sut);
        assertThat(result.size(), is(1));
        assertThat(result, hasItem(hasProperty("messageTemplate", equalTo(expectedMsg))));
    }

    private void testInvalidHolder(String invalidHolder, String... expectedMsgs) {
        int validBalance = 1;
        CreateAcctForm sut = new CreateAcctForm();
        sut.setBalance(validBalance);
        sut.setHolderName(invalidHolder);
        Set<ConstraintViolation<CreateAcctForm>> result = validator.validate(sut);
        assertThat(result.size(), is(expectedMsgs.length));
        for (String expectedMsg : expectedMsgs) {
            assertThat(result, hasItem(hasProperty("messageTemplate", equalTo(expectedMsg))));
        }
    }
}
