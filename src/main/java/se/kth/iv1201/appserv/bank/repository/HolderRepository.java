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
package se.kth.iv1201.appserv.bank.repository;

import java.util.List;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.kth.iv1201.appserv.bank.domain.Holder;

/**
 * Contains all database access concerning holders.
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY) // Applies only to methods explicitly declared in this interface,
// not to those inherited from JpaRepository. This is quite dangerous, there
// will be no error if an inherited method is
// called, but default transaction configuration will be used instead of what is
// declared here.
public interface HolderRepository extends JpaRepository<Holder, Long> {
    // /**
    // * Searches for all holders with the specified name.
    // *
    // * @param name The name of the holder's to search for.
    // * @return A list containing all holders with the specified name. The list
    // * is empty if there are no such holders.
    // */
    // public List<Holder> findHoldersByName(String name);
    //

    /**
     * Returns the holder with the specified holder number, or null if there is no
     * such holder.
     *
     * @param holderNo The number of the holder to search for.
     * @return The account with the specified holder number, or null if there is no
     *         such holder.
     * @throws IncorrectResultSizeDataAccessException If more than one holder with
     *                                                the specified number was
     *                                                found.
     */
    Holder findHolderByHolderNo(long holderNo);

    @Override
    <S extends Holder> S save(S holder);

    @Override
    List<Holder> findAll();
}
