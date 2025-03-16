/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.test.util;


import es.wakamiti.api.contributor.StepProvider;
import es.wakamiti.extension.annotation.Extension;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Extension
public class TestStepProvider implements StepProvider {

    private String testName;

    public TestStepProvider(String testName) {
        this.testName = testName;
    }

    @Override
    public String info() {
        return testName;
    }

}
