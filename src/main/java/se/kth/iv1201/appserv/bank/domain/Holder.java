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

package se.kth.iv1201.appserv.bank.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import se.kth.iv1201.appserv.bank.util.Util;

@Entity
@Table(name = "HOLDER")
public class Holder implements HolderDTO {
    private static final String SEQUENCE_NAME_KEY = "SEQ_NAME";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
    // SEQUENCE_NAME_KEY)
    // @SequenceGenerator(name = SEQUENCE_NAME_KEY, sequenceName = "BANK_SEQUENCE")
    @Column(name = "HLD_ID")
    private long id;

    @OneToMany(mappedBy = "holder", cascade = CascadeType.ALL)
    private Set<Account> accounts = new HashSet<>();

    @NotNull(message = "{holder.name.missing}")
    // The regex below should permit only characters, but asterisk is
    // unfortunately also valid.
    @Pattern(regexp = "^[\\p{L}\\p{M}*]*$", message = "{holder.name.invalid-char}")
    @Size(min = 2, max = 30, message = "{holder.name.length}")
    @Column(name = "HLD_NAME")
    private String name;

    @Column(name = "HLD_NO")
    private long holderNo;

    @Version
    @Column(name = "HLD_OPTLOCK_VERSION")
    private int optLockVersion;

    /**
     * Creates a new instance with the specified name. A unique holder number will
     * be set on the newly created instance.
     *
     * @param name The holder's name.
     */
    public Holder(String name) {
        this.name = name;
        holderNo = BeanFactory.getBean(BusinessIdGenerator.class).generateHolderNo();
    }

    /**
     * Required by JPA, should not be used.
     */
    protected Holder() {
    }

    /**
     * Returns a set containing all accounts owned by this account holder.
     */
    @Override
    public Set<AccountDTO> getAccounts() {
        Set<AccountDTO> copyOfAccts = new HashSet<>();
        copyOfAccts.addAll(accounts);
        return copyOfAccts;
    }

    /**
     * Adds the specified account to the set of accounts owned by this holder. There
     * is no limit on the number of accounts that can be owned by the same holder.
     *
     * @param acct The account to add to this holder's accounts.
     */
    public void addAccount(Account acct) {
        accounts.add(acct);
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the account holder's name
     */
    public void setName(String hldName) {
        this.name = hldName;
    }

    @Override
    public long getHolderNo() {
        return holderNo;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(holderNo).hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Holder)) {
            return false;
        }
        Holder other = (Holder) object;
        return this.holderNo == other.holderNo;
    }

    @Override
    public String toString() {
        return Util.toString(this);
    }
}
