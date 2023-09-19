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

import java.util.Locale;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

/**
 * Loads all configuration for the entire bank web app. Note that there are
 * config settings also in the file <code>application.properties</code>.
 */
@EnableTransactionManagement // Needed for @Transactional attribute outside
// repositories.
@EnableWebMvc
@Configuration
public class BankConfig implements WebMvcConfigurer, ApplicationContextAware {
    private ApplicationContext applicationContext;

    /**
     * @param applicationContext The application context used by the running
     *                           application.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Create a bean with all server related properties from application.properties.
     */
    @Bean
    public ServerProperties serverProperties() {
        return new ServerProperties();
    }

    /**
     * Create a <code>org.springframework.web.servlet .ViewResolver</code> bean that
     * delegates all views to thymeleaf's template engine. There is no need to
     * specify view name patterns since the will be the only existing view resolver.
     */
    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setCharacterEncoding("UTF-8");
        viewResolver.setContentType("text/html; charset=UTF-8");
        viewResolver.setTemplateEngine(templateEngine());
        return viewResolver;
    }

    /**
     * Create a <code>org.thymeleaf.ITemplateEngine</code> bean that manages
     * thymeleaf template integration with Spring. All template resolution will be
     * delegated to the specified template resolver.
     */
    @Bean(name = "bankTemplateEngine")
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        // Enabling the SpringEL compiler with Spring 4.2.4 or newer can
        // speed up execution in most scenarios, but might be incompatible
        // with specific cases when expressions in one template are reused
        // across different data types, so this flag is "false" by default
        // for safer backwards compatibility.
        templateEngine.setEnableSpringELCompiler(true);
        // Add the layout dialect, which enables reusing layout html pages.
        templateEngine.addDialect(new LayoutDialect());
        return templateEngine;
    }

    /**
     * Create a <code>org.thymeleaf.templateresolver.ITemplateResolver</code> that
     * can handle thymeleaf template integration with Spring. This will be the only
     * existing template resolver.
     */
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(this.applicationContext);
        // Templates file shall have the path /web-root/<template name>.html
        templateResolver.setPrefix("classpath:/web-root/");
        templateResolver.setSuffix(".html");
        // HTML is the default template mode, added here for the sake of
        // clarity.
        templateResolver.setTemplateMode(TemplateMode.HTML);
        // Template cache is true by default. Set to false to automatically
        // update templates that have been modified.
        templateResolver.setCacheable(true);
        return templateResolver;
    }

    /**
     * Configuration of requests for static files.
     **/
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        int cachePeriodForStaticFilesInSecs = 1;
        String rootDirForStaticFiles = "classpath:/web-root/";

        registry.addResourceHandler("/**").addResourceLocations(rootDirForStaticFiles)
                .setCachePeriod(cachePeriodForStaticFilesInSecs).resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

    /**
     * Register the i18n interceptor.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * Create a <code>org.springframework.web.servlet.i18n
     * .LocaleChangeInterceptor</code> for locale management.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        String nameOfHttpParamForLangCode = "lang";
        String[] allowedHttpMethodsForLocaleChange = { "GET", "POST" };

        LocaleChangeInterceptor i18nBean = new LocaleChangeInterceptor();
        i18nBean.setParamName(nameOfHttpParamForLangCode);
        i18nBean.setHttpMethods(allowedHttpMethodsForLocaleChange);
        i18nBean.setIgnoreInvalidLocale(true);
        return i18nBean;
    }

    /**
     * Create a <code>org.springframework.web.servlet.i18n
     * .SessionLocaleResolver</code> that stores the user's current locale in the
     * session object.
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(new Locale("en"));
        return localeResolver;
    }

    /**
     * Create a <code>org.springframework.context.support.
     * ReloadableResourceBundleMessageSource</code> that loads resource bundles for
     * i18n.
     */
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        String l10nMsgDir = "classpath:/i18n/Messages";
        String l10nValidationMsgDir = "classpath:/i18n/ValidationMessages";
        ReloadableResourceBundleMessageSource resource = new ReloadableResourceBundleMessageSource();
        resource.addBasenames(l10nMsgDir, l10nValidationMsgDir);
        resource.setDefaultEncoding("UTF-8");
        resource.setFallbackToSystemLocale(false);
        return resource;
    }

    /**
     * Use the bean defined by {@link #messageSource()} for validation messages.
     */
    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource());
        return validator;
    }
}
