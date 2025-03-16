/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.data;


import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

public interface DataType {

    String name();

    Class<?> javaType();

    String regex(
            Locale locale
    );

    List<String> hints(
            Locale locale
    );

    Object parse(
            Locale locale,
            String value
    );

    Matcher matcher(
            Locale locale,
            String value
    );

}
