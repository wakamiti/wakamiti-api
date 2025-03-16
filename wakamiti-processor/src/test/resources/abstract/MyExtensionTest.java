package tests;

import es.wakamiti.extension.annotation.Extension;
import es.wakamiti.extension.annotation.Priority;

@Extension(name = "AbTest", priority = Priority.HIGHER)
public abstract class MyExtensionTest implements MyExtensionPointTest<String> {

}