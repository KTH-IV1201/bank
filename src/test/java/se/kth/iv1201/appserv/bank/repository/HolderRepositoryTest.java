package se.kth.iv1201.appserv.bank.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Transactional;

import net.jcip.annotations.NotThreadSafe;
import se.kth.iv1201.appserv.bank.domain.Holder;

@SpringJUnitWebConfig(initializers = ConfigDataApplicationContextInitializer.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = { "se.kth.iv1201.appserv.bank" })
// @SpringBootTest can be used instead of @SpringJUnitWebConfig,
// @EnableAutoConfiguration and @ComponentScan, but are we using
// JUnit5 in that case?
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, HolderRepositoryTest.class,
        TransactionalTestExecutionListener.class })
@NotThreadSafe
@Transactional
@Commit
class HolderRepositoryTest implements TestExecutionListener {
    @Autowired
    private DbUtil dbUtil;
    @Autowired
    private HolderRepository instance;
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
    void setup() throws SQLException, IOException, ClassNotFoundException {
        holder = new Holder("holderName");
        dbUtil.emptyDb();
    }

    @Test
    void testCreateHolder() {
        instance.save(holder);
        startNewTransaction();
        List<Holder> holdersInDb = instance.findAll();
        assertThat(holdersInDb, containsInAnyOrder(holder));
    }

    @Test
    void testFindExistingHolderByHolderNo() {
        instance.save(holder);
        startNewTransaction();
        Holder holderInDb = instance.findHolderByHolderNo(holder.getHolderNo());
        assertThat(holderInDb.getHolderNo(), is(holder.getHolderNo()));
    }

    @Test
    void testFindNonExistingHolderByHolderNo() {
        instance.save(holder);
        startNewTransaction();
        Holder holderInDb = instance.findHolderByHolderNo(0);
        assertThat(holderInDb == null, is(true));
    }

    @Test
    void testRepoShallNotBeCalledWithoutTransaction() {
        TestTransaction.end();
        assertThrows(IllegalTransactionStateException.class, () -> {
            instance.save(holder);
        });
        assertThrows(IllegalTransactionStateException.class, () -> {
            instance.findHolderByHolderNo(holder.getHolderNo());
        });
        assertThrows(IllegalTransactionStateException.class, () -> {
            instance.findAll();
        });
    }

    private void startNewTransaction() {
        TestTransaction.end();
        TestTransaction.start();
    }
}
