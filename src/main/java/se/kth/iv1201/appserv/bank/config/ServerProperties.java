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
package se.kth.iv1201.appserv.bank.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Contains properties from application.properities, starting with 'se.kth
 * .iv1201.server'.
 */
@ConfigurationProperties(prefix = "se.kth.iv1201.bank.server")
public class ServerProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerProperties.class);
    private String contextRoot;

    /**
     * @return The context root of the web site.
     */
    public String getContextRoot() {
        LOGGER.trace("Reading context root {}.", contextRoot);
        return contextRoot;
    }

    /**
     * @param contextRoot The new context root of the web site.
     */
    public void setContextRoot(String contextRoot) {
        LOGGER.trace("Setting context root {}.", contextRoot);
        this.contextRoot = contextRoot;
    }
}
