import es.wakamiti.api.contributor.ConfigurationProvider;

module wakamiti.test {

    exports tests;

    requires transitive wakamiti.extension;
    requires transitive wakamiti.api;

    opens tests to wakamiti.extension;

    uses ConfigurationProvider;

    provides ConfigurationProvider with
            tests.MyExtensionTest;

}