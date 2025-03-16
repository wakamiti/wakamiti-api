/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.example.plugin.b;


import es.wakamiti.extension.annotation.Extension;
import es.wakamiti.example.plugin.a.Greeting;


@Extension
public class GreeterB implements Greeting {

    @Override
    public String greet() {
        return "Hello from Plugin B!";
    }

}
