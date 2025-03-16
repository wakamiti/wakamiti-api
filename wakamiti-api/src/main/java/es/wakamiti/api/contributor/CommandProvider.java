/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.contributor;


import es.wakamiti.api.cli.CommandLine;
import es.wakamiti.api.cli.Options;
import es.wakamiti.api.cli.internal.CommandLineParser;
import es.wakamiti.api.cli.HelpFormatter;
import es.wakamiti.api.lang.WakamitiException;
import es.wakamiti.extension.annotation.ExtensionPoint;


@ExtensionPoint
public interface CommandProvider {

    String key();

    String description();

    Options options();

    default Options defaultOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Print usage");
        options.addOptions(options());
        return options;
    }

    void launch(CommandLine command) throws WakamitiException;

    default void launch(
            String... args
    ) {
        CommandLineParser parser = new CommandLineParser();
        CommandLine cmd = parser.parse(defaultOptions(), args);
        if (cmd.hasOption("help")) {
            new HelpFormatter().printHelp("wakamiti %s [OPTIONS]".formatted(key()), defaultOptions());
            return;
        }
        launch(cmd);
    }

}
