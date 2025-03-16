package tests;

import java.io.Serializable;


public class MyExtensionTestAbs implements Serializable, MyExtensionPointTest<String> {

    public String toString() {
        return getClass().getName();
    }

}