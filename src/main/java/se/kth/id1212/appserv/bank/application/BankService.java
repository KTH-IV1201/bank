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
package se.kth.id1212.appserv.bank.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.kth.id1212.appserv.bank.domain.Account;
import se.kth.id1212.appserv.bank.domain.AccountDTO;
import se.kth.id1212.appserv.bank.domain.Holder;
import se.kth.id1212.appserv.bank.domain.HolderDTO;
import se.kth.id1212.appserv.bank.domain.IllegalBankTransactionException;
import se.kth.id1212.appserv.bank.repository.AccountRepository;
import se.kth.id1212.appserv.bank.repository.HolderRepository;

/**
 * <p>
 * This is the bank application class, which defines tasks that can be performed
 * by the domain layer.
 * </p>
 *
 * <p>
 * Transaction demarcation is defined by methods in this class, a transaction
 * starts when a method is called from the presentation layer, and ends (commit
 * or rollback) when that method returns.
 * </p>
 */
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class BankService {
    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private HolderRepository holderRepo;

    /**
     * Convenience method that creates both holder and account in the same
     * transaction, by calling first {@link #createHolder(String)} and then
     * {@link #createAccount(HolderDTO, int)}.
     *
     * @param holderName The account holder's name.
     * @param balance    The initial balance.
     * @return The newly created account.
     * @throws IllegalBankTransactionException If failed to create holder.
     */
    public AccountDTO createAccountAndHolder(String holderName, int balance) throws IllegalBankTransactionException {
        HolderDTO holder = createHolder(holderName);
        return createAccount(holder, balance);
    }

    /**
     * Creates an account owned by the specified account holder. Note that there is
     * only one type of account in the bank.
     *
     * @param holder  The account holder.
     * @param balance The initial balance.
     * @return The newly created account.
     * @throws IllegalBankTransactionException If the specified holder does not
     *                                         exist.
     */
    public AccountDTO createAccount(HolderDTO holder, int balance) throws IllegalBankTransactionException {
        Holder holderEntity = holderRepo.findHolderByHolderNo(holder.getHolderNo());
        if (holderEntity == null) {
            throw new IllegalBankTransactionException("Holder does not exist," + " holder: " + holder);
        }
        return accountRepo.save(new Account(holderEntity, balance));
    }

    /**
     * Searches for the account with the specified account number.
     *
     * @param acctNo The number of the searched account.
     * @return The account with the specified number, or null if no such account was
     *         found.
     */
    public AccountDTO findAccount(long acctNo) {
        return accountRepo.findAccountByAcctNo(acctNo);
    }

    /**
     * Deposits the specified amount to the specified account.
     *
     * @param acct The account to which to deposit.
     * @param amt  The amount to deposit.
     * @throws IllegalBankTransactionException When attempting to deposit a negative
     *                                         or zero amount.
     */
    public void deposit(AccountDTO acct, int amt) throws IllegalBankTransactionException {
        if (acct == null) {
            throw new IllegalBankTransactionException("Attempt to deposit to null account.");
        }

        Account acctEntity = accountRepo.findAccountByAcctNo(acct.getAcctNo());
        if (acctEntity == null) {
            throw new IllegalBankTransactionException("Attempt to deposit to non-existing account: " + acct);
        }

        acctEntity.deposit(amt);
    }

    /**
     * Withdraws the specified amount from the specified account.
     *
     * @param acct The account from which to withdraw.
     * @param amt  The amount to withdraw.
     * @throws IllegalBankTransactionException When attempting to withdraw a
     *                                         negative or zero amount, or if
     *                                         withdrawal would result in a negative
     *                                         balance.
     */
    public void withdraw(AccountDTO acct, int amt) throws IllegalBankTransactionException {
        if (acct == null) {
            throw new IllegalBankTransactionException("Attempt to withdraw from null account.");
        }

        Account acctEntity = accountRepo.findAccountByAcctNo(acct.getAcctNo());
        if (acctEntity == null) {
            throw new IllegalBankTransactionException("Attempt to withdraw from non-existing account: " + acct);
        }

        acctEntity.withdraw(amt);
    }

    /**
     * Creates a new holder with the specified name.
     *
     * @param holderName The name of the newly created holder.
     * @return The newly created holder.
     */
    public HolderDTO createHolder(String holderName) {
        return holderRepo.save(new Holder(holderName));
    }

    // /**
    // * Searches for the holder with the specified holder number.
    // *
    // * @param holderNo The number of the searched holder.
    // * @return The holder with the specified number, or null if no such holder
    // * was found.
    // */
    // public HolderDTO findHolder(long holderNo) {
    // return holderRepo.findHolderByHolderNo(holderNo);
    // }
    //
    // /**
    // * Searches for all holders with the specified name.
    // *
    // * @param holderName The name of the holder's to search for.
    // * @return A list containing all holders with the specified name. The list
    // * is empty if there are no such holders.
    // */
    // public List<HolderDTO> findHolders(String holderName) {
    // return (List<HolderDTO>)(List)holderRepo.findHoldersByName(holderName);
    // }
}
