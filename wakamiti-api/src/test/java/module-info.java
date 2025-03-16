/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import es.wakamiti.api.Wakamiti;

module wakamiti.api.test {

    requires transitive wakamiti.api;
    requires org.junit.jupiter.params;
    requires org.assertj.core;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.slf4j;
    requires wakamiti.extension;
    requires static lombok;

    exports es.wakamiti.api.test.util;

    opens es.wakamiti.api.test.contributor to org.junit.platform.commons;
    opens es.wakamiti.api.test.log to org.junit.platform.commons;

    uses es.wakamiti.api.Wakamiti;
    uses es.wakamiti.api.contributor.StepProvider;

//    provides Wakamiti.Builder with WakamitiTest.Builder;

    provides es.wakamiti.api.contributor.StepProvider with es.wakamiti.api.test.util.TestStepProvider;

}