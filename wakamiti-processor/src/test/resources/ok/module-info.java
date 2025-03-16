
module wakamiti.test {

    exports tests;

    requires transitive wakamiti.extension;

    opens tests to wakamiti.extension;

    uses tests.MyExtensionPointTest;

    provides tests.MyExtensionPointTest with
            tests.OtherExtensionTest,
            tests.MyExtensionTest;

}