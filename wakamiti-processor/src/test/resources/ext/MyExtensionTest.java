package tests;


import es.wakamiti.api.config.Configuration;
import es.wakamiti.api.contributor.ConfigurationProvider;
import es.wakamiti.extension.annotation.Extension;
import es.wakamiti.extension.annotation.Priority;


@Extension(name = "ATest", priority = Priority.HIGHER)
public class MyExtensionTest implements ConfigurationProvider {

    @Override
    public Configuration configuration() {
        return Configuration.factory().empty();
    }

}