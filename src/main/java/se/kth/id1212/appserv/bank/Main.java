/*
 * The MIT License
 *
 * Copyright 2018 Leif Lindbäck <leifl@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id1212.appserv.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import se.kth.id1212.appserv.bank.config.ServerProperties;

/**
 * Starts the bank application.
 */
@SpringBootApplication
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    @Autowired
    private ServerProperties serverProps;

    /**
     * Starts the bank application.
     *
     * @param args There are no command line parameters.
     */
    public static void main(String[] args) {
        LOGGER.debug("log level is set after app.run(args), the default " + "level (debug) is used here: "
                + LOGGER.isTraceEnabled());
        SpringApplication app = new SpringApplication(Main.class);
        app.setBanner((environment, sourceClass, out) -> {
            out.println("\n>>>>>>>>>>>>>> RUN IN TERMINAL TO SEE FRAMEWORK"
                    + "VERSIONS. USE 'mvn spring-boot:run' TO SHOW VERSIONS"
                    + " AND START. USE FOR EXAMPLE 'mvn dependency:resolve" + " -Dsort' TO JUST SEE DEPENDENCIES. "
                    + "<<<<<<<<<<<<<<<<<\n");
        });
        app.run(args);
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> getWebServerFactoryCustomizer() {
        LOGGER.trace("Setting WebServerFactory.");
        return serverFactory -> {
            LOGGER.trace("Setting context root.");
            serverFactory.setContextPath(serverProps.getContextRoot());
        };
    }
}
