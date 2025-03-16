/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.resource;

import es.wakamiti.api.lang.WakamitiException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


@Getter
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode
public class Hash implements Comparable<Hash> {

    private static final Base64.Encoder encoder = Base64.getEncoder();

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new WakamitiException("Error obtaining hash algorithm.", e);
        }
    }

    public static Hash of(
            String content
    ) {
        var bytes = newDigest().digest(content.getBytes(StandardCharsets.UTF_8));
        return new Hash(encoder.encodeToString(bytes));
    }

    public static Hash of(
            URI uri
    ) {
        try (var stream = new DigestInputStream(uri.toURL().openStream(),newDigest())) {
            return new Hash(encoder.encodeToString(stream.getMessageDigest().digest()));
        } catch (IOException e) {
            throw new WakamitiException("Cannot calculate hash of {resource}.", uri, e);
        }
    }

    private final String value;

    @Override
    public int compareTo(
            Hash other
    ) {
        return value.compareTo(other.value);
    }

}
