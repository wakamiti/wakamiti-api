/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.example.core;


import es.wakamiti.example.plugin.a.Greeting;

import java.util.Optional;
import java.util.ServiceLoader;


public class App {


    public static void main(String[] args) {
        Optional<Greeting> greeting = load(Greeting.class);
        if (greeting.isEmpty()) {
            throw new RuntimeException("Greeting is not present.");
        }
        String actual = greeting.get().greet();
        String expected = "Hello from Plugin B!";
        if (!expected.equals(actual)) {
            throw new RuntimeException("Expected greet is '%s' but was '%s'".formatted(expected, actual));
        }
    }

    private static <T> Optional<T> load(Class<T> type) {
        App.class.getModule().addUses(type);
        return ServiceLoader.load(ModuleLayer.boot(), type).findFirst();
    }

}
