/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.log.internal;


import es.wakamiti.api.log.WakamitiLogger;
import org.fusesource.jansi.Ansi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class AnsiSupport {

    public static final AnsiSupport instance = new AnsiSupport();
    private static final Pattern globalStylePattern = Pattern.compile("^\\{!([^}]*?)}.*+");
    private static final Pattern stylePattern = Pattern.compile("\\{([^}]*?)}");

    private Map<String,String> styles;

    private AnsiSupport() {
        WakamitiLogger.addConfigurationChangeObserver(this::invalidateStyles);
    }

    private String replaceStyles(
            String globalStyle,
            String message
    ) {
        if (styles == null) {
            styles = new HashMap<>();
            WakamitiLogger.styles().forEach((key, value) -> styles.put(key.toString(), value.toString()));
        }
        String auxStyle = null;
        Matcher globalStyleMatcher = globalStylePattern.matcher(message);
        if (globalStyleMatcher.matches()) {
            auxStyle = styles.getOrDefault(globalStyleMatcher.group(1), null);
            message = message.substring(message.indexOf('}')+1).trim();
        }
        globalStyle = styles.getOrDefault(globalStyle, null);
        if (auxStyle != null) {
            globalStyle = (globalStyle != null ? globalStyle + "," : "") + auxStyle;
        }
        Matcher styleMatcher = stylePattern.matcher(message);
        while (styleMatcher.find()) {
            String foundStyle = styleMatcher.group(1);
            String style = styles.getOrDefault(foundStyle,"");
            if (style.isEmpty()) continue;
            style = Optional.ofNullable(globalStyle)
                    .map(v -> v+",").orElse("") + style;
            message = message.replace("{"+foundStyle+"}","@|"+style+" {}|@");
        }

        if (globalStyle != null) {
            message = "$|"+message.replace("|@","|@$|").replace("@|","|$@|") + "|$";
            message = message.replace("$|","@|"+globalStyle+" ").replace("|$","|@");
        }
        return message;
    }

    private void invalidateStyles() {
        this.styles = null;
    }

    public String ansi(
            String level,
            String message
    ) {
        return message == null ? null : Ansi.ansi().render(replaceStyles(level, message)).toString();
    }

}
