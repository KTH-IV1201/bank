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
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, FindAcctFormTest.class })
class FindAcctFormTest implements TestExecutionListener {
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
    void testNullNo() {
        testInvalidNo(null, "{find-acct.number.missing}");
    }

    @Test
    void testCorrectNo() {
        FindAcctForm sut = new FindAcctForm();
        sut.setNumber(1L);
        Set<ConstraintViolation<FindAcctForm>> result = validator.validate(sut);
        assertThat(result, is(empty()));
    }

    private void testInvalidNo(Long invalidNo, String expectedMsg) {
        FindAcctForm sut = new FindAcctForm();
        sut.setNumber(invalidNo);
        Set<ConstraintViolation<FindAcctForm>> result = validator.validate(sut);
        assertThat(result.size(), is(1));
        assertThat(result, hasItem(hasProperty("messageTemplate", equalTo(expectedMsg))));
    }
}
