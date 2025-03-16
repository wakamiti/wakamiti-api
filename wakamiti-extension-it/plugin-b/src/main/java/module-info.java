/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
module plugin.b {

    exports es.wakamiti.example.plugin.b;

    requires wakamiti.extension;
    requires plugin.a;

    opens es.wakamiti.example.plugin.b to wakamiti.extension;

    provides es.wakamiti.example.plugin.a.Greeting with
            es.wakamiti.example.plugin.b.GreeterB;

}