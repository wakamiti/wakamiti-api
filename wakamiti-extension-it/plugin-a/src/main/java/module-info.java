/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
module plugin.a {

    exports es.wakamiti.example.plugin.a;

    requires wakamiti.extension;

    opens es.wakamiti.example.plugin.a to wakamiti.extension;

    uses es.wakamiti.example.plugin.a.Greeting;

}