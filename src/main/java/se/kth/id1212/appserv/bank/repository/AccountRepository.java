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
package se.kth.id1212.appserv.bank.repository;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.kth.id1212.appserv.bank.domain.Account;

import java.util.List;

/**
 * Contains all database access concerning accounts.
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY) // Applies only to methods explicitly declared in this interface,
// not to those inherited from JpaRepository. This is quite dangerous, there
// will be no error if an inherited method is
// called, but default transaction configuration will be used instead of what is
// declared here.
public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * Returns the account with the specified account number, or null if there is no
     * such account.
     *
     * @param acctNo The number of the account to search for.
     * @return The account with the specified account number, or null if there is no
     *         such account.
     * @throws IncorrectResultSizeDataAccessException If more than one account with
     *                                                the specified number was
     *                                                found.
     */
    Account findAccountByAcctNo(long acctNo);

    @Override
    List<Account> findAll();

    @Override
    Account save(Account acct);
}
