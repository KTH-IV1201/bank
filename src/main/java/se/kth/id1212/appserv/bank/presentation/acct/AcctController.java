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
package se.kth.id1212.appserv.bank.presentation.acct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import se.kth.id1212.appserv.bank.application.BankService;
import se.kth.id1212.appserv.bank.domain.AccountDTO;
import se.kth.id1212.appserv.bank.domain.IllegalBankTransactionException;
import se.kth.id1212.appserv.bank.presentation.error.ExceptionHandlers;

import javax.validation.Valid;

/**
 * Handles all HTTP requests to context root.
 */
@Controller
@Scope("session")
public class AcctController {
    static final String DEFAULT_PAGE_URL = "/";
    static final String SELECT_ACCT_PAGE_URL = "select-acct";
    static final String ACCT_PAGE_URL = "acct";
    static final String CREATE_ACCT_URL = "create-acct";
    static final String FIND_ACCT_URL = "find-acct";
    static final String DEPOSIT_URL = "deposit";
    static final String WITHDRAW_URL = "withdraw";
    private static final Logger LOGGER = LoggerFactory.getLogger(AcctController.class);
    private static final String CURRENT_ACCT_OBJ_NAME = "currentAcct";
    private static final String DEPOSIT_FORM_OBJ_NAME = "depositForm";
    private static final String WITHDRAW_FORM_OBJ_NAME = "withdrawForm";
    private static final String FIND_ACCT_FORM_OBJ_NAME = "findAcctForm";
    private static final String CREATE_ACCT_FORM_OBJ_NAME = "createAcctForm";
    @Autowired
    private BankService service;
    private AccountDTO currentAcct;

    /**
     * No page is specified, redirect to the welcome page.
     *
     * @return A response that redirects the browser to the welcome page.
     */
    @GetMapping(DEFAULT_PAGE_URL)
    public String showDefaultView() {
        LOGGER.trace("Call to context root.");
        return "redirect:" + SELECT_ACCT_PAGE_URL;
    }

    /**
     * A get request for the account selection page.
     *
     * @param createAcctForm Used in the create account form.
     * @param findAcctForm   Used in the find account form.
     * @return The account selection page url.
     */
    @GetMapping("/" + SELECT_ACCT_PAGE_URL)
    public String showAccountSelectionView(CreateAcctForm createAcctForm, FindAcctForm findAcctForm) {
        LOGGER.trace("Call to account selection view.");
        return SELECT_ACCT_PAGE_URL;
    }

    /**
     * The create account form has been submitted.
     *
     * @param createAcctForm Content of the create account form.
     * @param bindingResult  Validation result for the create account form.
     * @param model          Model objects used by the account page.
     * @return The account page url if validation succeeds.
     */
    @PostMapping("/" + CREATE_ACCT_URL)
    public String createAccount(@Valid CreateAcctForm createAcctForm, BindingResult bindingResult, Model model)
        throws IllegalBankTransactionException {
        LOGGER.trace("Post of account creation data.");
        LOGGER.trace("Form data: " + createAcctForm);
        if (bindingResult.hasErrors()) {
            model.addAttribute(FIND_ACCT_FORM_OBJ_NAME, new FindAcctForm());
            return SELECT_ACCT_PAGE_URL;
        }
        currentAcct = service.createAccountAndHolder(createAcctForm.getHolderName(), createAcctForm.getBalance());
        return showAcctPage(model, new DepositOrWithdrawForm(), new DepositOrWithdrawForm());
    }

    private String showAcctPage(Model model, DepositOrWithdrawForm depositForm, DepositOrWithdrawForm withdrawForm) {
        if (currentAcct != null) {
            model.addAttribute(CURRENT_ACCT_OBJ_NAME, currentAcct);
        }
        if (depositForm != null) {
            model.addAttribute(DEPOSIT_FORM_OBJ_NAME, depositForm);
        }
        if (withdrawForm != null) {
            model.addAttribute(WITHDRAW_FORM_OBJ_NAME, withdrawForm);
        }
        return ACCT_PAGE_URL;
    }

    /**
     * Dummy method in case the user changes locale while the create acct view is displayed.
     */
    @GetMapping("/" + CREATE_ACCT_URL)
    public String getMetCreateAccount(@Valid CreateAcctForm createAcctForm, BindingResult bindingResult,
                                      FindAcctForm findAcctForm, Model model) {
        LOGGER.trace("Get of account creation data.");
        LOGGER.trace("Form data: " + createAcctForm);
        return SELECT_ACCT_PAGE_URL;
    }

    /**
     * The find account form has been submitted.
     *
     * @param findAcctForm  Content of the find account form.
     * @param bindingResult Validation result for the find account form.
     * @param model         Model objects used by the account page.
     * @return The account page url if validation succeeds and account is found. Returns the error page url if account
     * is not found
     */
    @PostMapping("/" + FIND_ACCT_URL)
    public String findAccount(@Valid FindAcctForm findAcctForm, BindingResult bindingResult, Model model) {
        LOGGER.trace("Post of account search data.");
        LOGGER.trace("Form data: " + findAcctForm);
        if (bindingResult.hasErrors()) {
            model.addAttribute(CREATE_ACCT_FORM_OBJ_NAME, new CreateAcctForm());
            return SELECT_ACCT_PAGE_URL;
        }
        currentAcct = service.findAccount(findAcctForm.getNumber());
        if (currentAcct == null) {
            model.addAttribute(ExceptionHandlers.ERROR_TYPE_KEY, ExceptionHandlers.ACCT_NOT_FOUND);
            return ExceptionHandlers.ERROR_PAGE_URL;
        }
        return showAcctPage(model, new DepositOrWithdrawForm(), new DepositOrWithdrawForm());
    }

    /**
     * Dummy method in case the user changes locale while the find acct view is displayed.
     */
    @GetMapping("/" + FIND_ACCT_URL)
    public String getMetFindAccount(@Valid FindAcctForm findAcctForm, BindingResult bindingResult,
                                    CreateAcctForm createAcctForm) {
        LOGGER.trace("Get of account search data.");
        LOGGER.trace("Form data: " + findAcctForm);
        return SELECT_ACCT_PAGE_URL;
    }

    /**
     * A get request for the account page.
     *
     * @param model Model objects used by the account page.
     * @return The account page url.
     */
    @GetMapping("/" + ACCT_PAGE_URL)
    public String showAccountView(Model model) {
        LOGGER.trace("Call to account view.");
        return showAcctPage(model, new DepositOrWithdrawForm(), new DepositOrWithdrawForm());
    }

    /**
     * The deposit form has been submitted.
     *
     * @param depositForm   Content of the deposit form.
     * @param bindingResult Validation result for the deposit form.
     * @param model         Model objects used by the account page.
     * @return The account page url.
     */
    @PostMapping("/" + DEPOSIT_URL)
    public String deposit(@Valid @ModelAttribute(DEPOSIT_FORM_OBJ_NAME) DepositOrWithdrawForm depositForm,
                          BindingResult bindingResult, Model model) throws IllegalBankTransactionException {
        LOGGER.trace("Post of deposit data.");
        LOGGER.trace("Form data: " + depositForm);
        if (!bindingResult.hasErrors()) {
            service.deposit(currentAcct, depositForm.getAmount());
            updateCurrentAcct();
            return showAcctPage(model, new DepositOrWithdrawForm(), new DepositOrWithdrawForm());
        }
        return showAcctPage(model, null, new DepositOrWithdrawForm());
    }

    private void updateCurrentAcct() {
        currentAcct = service.findAccount(currentAcct.getAcctNo());
    }

    /**
     * Dummy method in case the user changes locale while the deposit view is displayed.
     */
    @GetMapping("/" + DEPOSIT_URL)
    public String getMetDeposit(@Valid @ModelAttribute(DEPOSIT_FORM_OBJ_NAME) DepositOrWithdrawForm depositForm,
                                BindingResult bindingResult, Model model) {
        LOGGER.trace("Get of deposit data.");
        LOGGER.trace("Form data: " + depositForm);
        return showAcctPage(model, null, new DepositOrWithdrawForm());
    }

    /**
     * The withdraw form has been submitted.
     *
     * @param withdrawForm  Content of the withdraw form.
     * @param bindingResult Validation result for the withdraw form.
     * @param model         Model objects used by the account page.
     * @return The account page url.
     */
    @PostMapping("/" + WITHDRAW_URL)
    public String withdraw(@Valid @ModelAttribute(WITHDRAW_FORM_OBJ_NAME) DepositOrWithdrawForm withdrawForm,
                           BindingResult bindingResult, Model model) throws IllegalBankTransactionException {
        LOGGER.trace("Post of withdraw data.");
        LOGGER.trace("Form data: " + withdrawForm);
        if (!bindingResult.hasErrors()) {
            service.withdraw(currentAcct, withdrawForm.getAmount());
            updateCurrentAcct();
            return showAcctPage(model, new DepositOrWithdrawForm(), new DepositOrWithdrawForm());
        }
        return showAcctPage(model, new DepositOrWithdrawForm(), null);
    }

    /**
     * Dummy method in case the user changes locale while the withdraw view is displayed.
     */
    @GetMapping("/" + WITHDRAW_URL)
    public String getMetWithdraw(@Valid @ModelAttribute(WITHDRAW_FORM_OBJ_NAME) DepositOrWithdrawForm withdrawForm,
                                 BindingResult bindingResult, Model model) {
        LOGGER.trace("Get of withdraw data.");
        LOGGER.trace("Form data: " + withdrawForm);
        return showAcctPage(model, new DepositOrWithdrawForm(), null);
    }
}
