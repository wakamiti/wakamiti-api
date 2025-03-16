
module wakamiti.test {

    exports tests;

    requires transitive wakamiti.extension;

    opens tests to wakamiti.extension;

    uses tests.MyExtensionPointTest;
    uses tests.OtherExtensionPointTest;
    uses tests.ExtraExtensionPointTest;

    provides tests.MyExtensionPointTest with
            tests.MyExtensionTest;
    provides tests.OtherExtensionPointTest with
            tests.MyExtensionTest;
    provides tests.ExtraExtensionPointTest with
            tests.MyExtensionTest;
}