/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.log.internal;


import java.util.Properties;


public class DefaultStyles {

    public static Properties asProperties() {
        Properties styles = new Properties();
        styles.put("error","red");
        styles.put("warn","yellow");
        styles.put("uri", "blue,underline");
        styles.put("id","cyan");
        styles.put("important","bold");
        styles.put("highlight","white,bold");
        styles.put("resource", "cyan,underline");
        styles.put("file", "cyan,underline");
        return styles;
    }

}
