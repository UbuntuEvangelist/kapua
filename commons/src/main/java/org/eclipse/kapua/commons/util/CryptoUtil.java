/*******************************************************************************
 * Copyright (c) 2016, 2022 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.commons.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

/**
 * Encrypt/Decrypt and encode/decode utilities
 *
 * @since 1.0
 *
 */
public class CryptoUtil {

    private CryptoUtil() {
    }

    /**
     * Evaluate the sha1 hash for the provided String
     *
     * @param s
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String sha1Hash(String s) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] encodedBytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printBase64Binary(encodedBytes);
    }

    /**
     * Encode a base 64 String
     *
     * @param stringValue
     * @return
     */
    public static String encodeBase64(String stringValue) {
        byte[] bytesValue = stringValue.getBytes(StandardCharsets.UTF_8);
        return DatatypeConverter.printBase64Binary(bytesValue);
    }

    /**
     * Decode a base 64 String
     *
     * @param encodedValue
     * @return
     */
    public static String decodeBase64(String encodedValue) {
        byte[] decodedBytes = DatatypeConverter.parseBase64Binary(encodedValue);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
