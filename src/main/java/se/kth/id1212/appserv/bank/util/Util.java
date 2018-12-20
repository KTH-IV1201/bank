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
package se.kth.id1212.appserv.bank.util;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A functional library of utility methods.
 */
public class Util {
    private Util() {}

    /**
     * Creates a string representation of the specified object. The string
     * contains the class name and the names and values of all fields declared
     * in the object's class. Fields in superclasses and implemented interfaces
     * are not included.
     *
     * @param objToRepresent The object to represent as a string.
     * @return A string representation of the specified object.
     */
    public static String toString(Object objToRepresent) {
        StringBuilder builder =
            new StringBuilder(objToRepresent.getClass().getName());
        builder.append("[");
        builder.append(
            Arrays.stream(objToRepresent.getClass().getDeclaredFields())
                  .map((field) -> {
                      field.setAccessible(true);
                      try {
                          return field.getName() + ":" +
                                 field.get(objToRepresent);
                      } catch (Exception e) {
                          return field.getName() + ":" + e.getMessage();
                      }
                  }).collect(Collectors.joining(", ")));
        builder.append("]");
        return builder.toString();
    }
}
