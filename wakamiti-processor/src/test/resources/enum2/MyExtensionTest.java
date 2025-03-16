package tests;

import es.wakamiti.extension.annotation.Extension;
import es.wakamiti.extension.annotation.Priority;

@Extension(name = "ETest", priority = Priority.HIGHER)
public enum MyExtensionTest implements MyExtensionPointTest {

}