/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
module wakamiti.api {

    // compile-only
    requires static lombok;
    requires transitive wakamiti.extension;

    requires org.fusesource.jansi;
    requires org.slf4j;
    requires java.naming;

    exports es.wakamiti.api;
    exports es.wakamiti.api.annotation;
    exports es.wakamiti.api.config;
    exports es.wakamiti.api.contributor;
    exports es.wakamiti.api.data;
    exports es.wakamiti.api.cli;
//    exports es.wakamiti.api.expression;
    exports es.wakamiti.api.lang;
    exports es.wakamiti.api.log;
    exports es.wakamiti.api.plan;
    exports es.wakamiti.api.repository;
    exports es.wakamiti.api.resource;

    opens es.wakamiti.api.contributor to wakamiti.extension;
    opens es.wakamiti.api to wakamiti.extension;

    uses es.wakamiti.api.Wakamiti.Builder;

}