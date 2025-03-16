package tests;

import es.wakamiti.extension.annotation.Extension;
import es.wakamiti.extension.annotation.Priority;

@Extension(name = "BTest", priority = Priority.HIGHER)
public class OtherExtensionTest implements MyExtensionPointTest {


}