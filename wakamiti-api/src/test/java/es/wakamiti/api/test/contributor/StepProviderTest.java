/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.test.contributor;


import es.wakamiti.api.contributor.StepProvider;
import org.junit.jupiter.api.Test;


import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

public class StepProviderTest {

    private static StepProvider implement() {
        return ServiceLoader.load(StepProvider.class).findFirst().get();
    }

    @Test
    public void testStepProvider() {
        StepProvider provider = implement();
        assertThat(provider).isNotNull()
                .extracting(StepProvider::info)
                .isNull();
    }

}
