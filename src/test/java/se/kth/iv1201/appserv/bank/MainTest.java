package se.kth.iv1201.appserv.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import se.kth.iv1201.appserv.bank.repository.DbUtil;

@SpringJUnitWebConfig(initializers = ConfigDataApplicationContextInitializer.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = { "se.kth.iv1201.appserv.bank" })
// @SpringBootTest can be used instead of @SpringJUnitWebConfig,
// @EnableAutoConfiguration and @ComponentScan, but are we using
// JUnit5 in that case?
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, MainTest.class })
class MainTest implements TestExecutionListener {
        @Autowired
        private DbUtil dbUtil;
        @Autowired
        @Qualifier("getWebServerFactoryCustomizer")
        private WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> factoryCustomizer;

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
        void testWebServerFactoryCustomizerCreation() {
                TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
                factoryCustomizer.customize(factory);
                assertEquals("/bank", factory.getContextPath(), "Wrong context root in server factory");
        }
}
