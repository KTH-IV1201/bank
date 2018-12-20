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

import se.kth.id1212.appserv.bank.util.Util;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

/**
 * A form bean for the account creation form.
 */
class CreateAcctForm {
    @NotBlank(message = "{create-acct.holder-name.missing}")
    // The regex below should permit only characters, but asterisk is
    // unfortunately also valid.
    @Pattern(regexp = "^[\\p{L}\\p{M}*]*$", message = "{create-acct" +
                                                      ".holder-name" +
                                                      ".invalid-char}")
    @Size(min = 2, max = 30, message = "{create-acct.holder-name.length}")
    private String holderName;

    @NotNull(message = "{create-acct.balance.missing}")
    @PositiveOrZero(message = "{create-acct.balance.negative}")
    private Integer balance;

    /**
     * @return The initial balance of the account that will be created.
     */
    public Integer getBalance() {
        return balance;
    }

    /**
     * @param balance The initial balance of the account that will be created.
     */
    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    /**
     * @return The name of the holderName of the account that will be created.
     */
    public String getHolderName() {
        return holderName;
    }

    /**
     * @param holderName The name of the holderName of the account that will be
     *               created.
     */
    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    @Override
    public String toString() {
        return Util.toString(this);
    }
}
