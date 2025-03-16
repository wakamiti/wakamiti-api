package tests;


import es.wakamiti.extension.annotation.Extension;
import es.wakamiti.extension.annotation.Priority;


@Extension(name = "ATest", priority = Priority.HIGHER)
public class MyExtensionTest implements MyExtensionPointTest {

    private final String name;

    public MyExtensionTest(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

}