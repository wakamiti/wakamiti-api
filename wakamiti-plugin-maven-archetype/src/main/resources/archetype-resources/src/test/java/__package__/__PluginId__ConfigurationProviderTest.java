/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package ${package};


//import es.wakamiti.api.WakamitiConfiguration;
//import es.wakamiti.junit.WakamitiJUnitRunner;
import es.wakamiti.api.config.AnnotatedConfiguration;
import es.wakamiti.api.config.Configuration;
import es.wakamiti.api.config.Property;
import org.junit.jupiter.api.Test;


//@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration({
        @Property(key = ${PluginId}ConfigurationProvider.PROPERTY1, value = "something")
})
public class ${PluginId}ConfigurationProviderTest {

    @Test
    public void testSomething() {

    }

}