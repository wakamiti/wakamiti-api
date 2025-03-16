/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension.processor.test;


import es.wakamiti.api.contributor.ConfigurationProvider;
import es.wakamiti.extension.processor.ExtensionProcessor;
import io.toolisticon.cute.Cute;
import io.toolisticon.cute.CuteApi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ExtensionProcessorIT {

    private static CuteApi.BlackBoxTestSourceFilesAndProcessorInterface givenProcessor() {
        return Cute.blackBoxTest().given()
                .processor(ExtensionProcessor.class);
    }

    @Test
    public void testProcessorWhenAnnotationsExistWithSuccess() {
        String extensionClass = "tests.MyExtensionTest";

        givenProcessor()
                .andSourceFilesFromFolders("/ok")
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedClass(extensionClass)
                .testedSuccessfullyBy((clazz, cuteClassLoader) -> {
                    var unit = clazz.getConstructor().newInstance();
                    assertThat(unit).hasToString(extensionClass);
                })
                .andThat()
                .generatedResourceFile("", "META-INF/extensions")
                .matches(file -> {
                    String content = file.getCharContent(true).toString();
                    return content.contains("tests.MyExtensionPointTest=tests.OtherExtensionTest,tests.MyExtensionTest")
                            || content.contains("tests.MyExtensionPointTest=tests.MyExtensionTest,tests.OtherExtensionTest");
                })
                .andThat()
                .compilerMessage().ofKindNote()
                .contains("[ExtensionProcessor] :: Generated extensions declaration file '/META-INF/extensions'")
                .executeTest();
    }

    @Test
    public void testProcessorWhenExternalExtensionPointWithSuccess() {
        String extensionClass = "tests.MyExtensionTest";

        givenProcessor()
                .andSourceFilesFromFolders("/ext")
                .andUseModules("wakamiti.extension", "wakamiti.api")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedClass(extensionClass)
                .exists()
                .andThat()
                .generatedResourceFile("", "META-INF/extensions")
                .matches(file -> {
                    String content = file.getCharContent(true).toString();
                    return content.contains("%s=tests.MyExtensionTest".formatted(ConfigurationProvider.class.getName()));
                })
                .andThat()
                .compilerMessage().ofKindNote()
                .contains("[ExtensionProcessor] :: Generated extensions declaration file '/META-INF/extensions'")
                .executeTest();
    }

    @Test
    public void testProcessorWhenMultipleExtensionPointImplementedWithSuccess() {
        String extensionClass = "tests.MyExtensionTest";

        givenProcessor()
                .andSourceFilesFromFolders("/impl2")
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedClass(extensionClass)
                .exists()
                .andThat()
                .generatedResourceFile("", "META-INF/extensions")
                .matches(file -> {
                    String content = file.getCharContent(true).toString();
                    return content.contains("tests.MyExtensionPointTest=tests.MyExtensionTest")
                            && content.contains("tests.OtherExtensionPointTest=tests.MyExtensionTest")
                            && content.contains("tests.ExtraExtensionPointTest=tests.MyExtensionTest");
                })
                .andThat()
                .compilerMessage().ofKindNote()
                .contains("[ExtensionProcessor] :: Generated extensions declaration file '/META-INF/extensions'")
                .executeTest();
    }

    @Test
    public void testProcessorWhenAnnotationsNotExistWithSuccess() {
        givenProcessor()
                .andSourceFilesFromFolders("/ok2")
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedResourceFile("", "META-INF/extensions")
                .doesntExist()
                .executeTest();
    }

    @Test
    public void testProcessorWhenEnumExtensionPointWithError() {
        givenProcessor()
                .andSourceFilesFromFolders("/enum")
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage().ofKindError()
                .contains("[ExtensionProcessor] at tests.MyExtensionPointTest :: ",
                        "@ExtensionPoint not valid (only processed for interfaces)")
                .executeTest();
    }

    @Test
    public void testProcessorWhenEnumExtensionWithError() {
        givenProcessor()
                .andSourceFilesFromFolders("/enum2")
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage().ofKindError()
                .contains("[ExtensionProcessor] at tests.MyExtensionTest :: ",
                        "@Extension not valid (only processed for classes)")
                .executeTest();
    }

    @Test
    public void testProcessorWhenAbstractExtensionWithError() {
        givenProcessor()
                .andSourceFilesFromFolders("/abstract")
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage().ofKindError()
                .contains("[ExtensionProcessor] at tests.MyExtensionTest :: ",
                        "@Extension cannot be applied to an abstract class")
                .executeTest();
    }

    @Test
    public void testProcessorWhenNoModuleInfoExistsWithError() {
        givenProcessor()
                .andSourceFiles("/ok/MyExtensionPointTest.java",
                        "/ok/MyExtensionTestAbs.java",
                        "/ok/MyExtensionTest.java")
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage().ofKindError()
                .contains("[ExtensionProcessor] :: ",
                        "Cannot find module definition. Ensure module-info.java exists")
                .executeTest();
    }

    @Test
    public void testProcessorWhenNoExtensionPointImplementedWithError() {
        givenProcessor()
                .andSourceFilesFromFolders("/impl")
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage().ofKindError()
                .contains("[ExtensionProcessor] at tests.MyExtensionTest :: ",
                        "@Extension class must implement or extend some extension point")
                .executeTest();
    }

    @Test
    public void testProcessorWhenNoOpenedWithError() {
        givenProcessor()
                .andSourceFiles("/ok/MyExtensionPointTest.java",
                        "/ok/MyExtensionTestAbs.java",
                        "/ok/MyExtensionTest.java")
                .andSourceFile("module-info", """
                        module wakamiti.test {
                            exports tests;
                            requires transitive wakamiti.extension;
                            uses tests.MyExtensionPointTest;
                            provides tests.MyExtensionPointTest with
                                    tests.MyExtensionTest;
                        }
                        """)
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage().ofKindError()
                .contains("[ExtensionProcessor] at wakamiti.test :: ",
                        "The package 'tests' must be opened to wakamiti.extension in the 'module-info.java' file.",
                        "Try add:",
                        "opens tests to wakamiti.extension;")
                .executeTest();
    }

    @Test
    public void testProcessorWhenNoUsedWithError() {
        givenProcessor()
                .andSourceFiles("/ok/MyExtensionPointTest.java",
                        "/ok/MyExtensionTestAbs.java",
                        "/ok/MyExtensionTest.java")
                .andSourceFile("module-info", """
                        module wakamiti.test {
                            exports tests;
                            requires transitive wakamiti.extension;
                            opens tests to wakamiti.extension;
                            provides tests.MyExtensionPointTest with
                                    tests.MyExtensionTest;
                        }
                        """)
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage().ofKindError()
                .contains("[ExtensionProcessor] at wakamiti.test :: ",
                        "The usage of extension point 'tests.MyExtensionPointTest' must be declared " +
                                "in the 'module-info.java' file.",
                        "Try add:",
                        "uses tests.MyExtensionPointTest;")
                .executeTest();
    }

    @Test
    public void testProcessorWhenNoProvidedWithError() {
        givenProcessor()
                .andSourceFiles("/ok/MyExtensionPointTest.java",
                        "/ok/MyExtensionTestAbs.java",
                        "/ok/MyExtensionTest.java")
                .andSourceFile("module-info", """
                        module wakamiti.test {
                            exports tests;
                            requires transitive wakamiti.extension;
                            opens tests to wakamiti.extension;
                            uses tests.MyExtensionPointTest;
                        }
                        """)
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage().ofKindError()
                .contains("[ExtensionProcessor] at tests.MyExtensionPointTest<T> :: ",
                        "The provision of extension point 'tests.MyExtensionPointTest' must be declared " +
                                "in the 'module-info.java' file.",
                        "Try add:",
                        "provides tests.MyExtensionPointTest with",
                        "\ttests.MyExtensionTest;")
                .executeTest();
    }

    @Test
    public void testProcessorWhenNoProvidedWithExtensionWithError() {
        givenProcessor()
                .andSourceFiles("/ok/MyExtensionPointTest.java",
                        "/ok/MyExtensionTestAbs.java",
                        "/ok/OtherExtensionTest.java",
                        "/ok/MyExtensionTest.java")
                .andSourceFile("module-info", """
                        module wakamiti.test {
                            exports tests;
                            requires transitive wakamiti.extension;
                            opens tests to wakamiti.extension;
                            uses tests.MyExtensionPointTest;
                            provides tests.MyExtensionPointTest with
                                    tests.MyExtensionTest;
                        }
                        """)
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage().ofKindError()
                .contains("[ExtensionProcessor] at tests.MyExtensionPointTest<T> :: ",
                        "The provision of extensions [tests.OtherExtensionTest] must be declared " +
                                "in the 'module-info.java' file.",
                        "Try add:",
                        "provides tests.MyExtensionPointTest with",
                        "\ttests.OtherExtensionTest",
                        "\ttests.MyExtensionTest")
                .executeTest();
    }

    @Test
    public void testProcessorWhenNoExportedWithError() {
        givenProcessor()
                .andSourceFiles("/ok/MyExtensionPointTest.java",
                        "/ok/MyExtensionTestAbs.java",
                        "/ok/MyExtensionTest.java")
                .andSourceFile("module-info", """
                        module wakamiti.test {
                            opens tests;
                            requires transitive wakamiti.extension;
                            uses tests.MyExtensionPointTest;
                            provides tests.MyExtensionPointTest with
                                    tests.MyExtensionTest;
                        }
                        """)
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage().ofKindError()
                .contains("[ExtensionProcessor] at wakamiti.test :: ",
                        "The package 'tests' must be exported in the 'module-info.java' file.",
                        "Try add:",
                        "exports tests;")
                .executeTest();
    }

    @Test
    public void testProcessorWhenExtensionDontHaveNoArgsConstructorWithSuccess() {
        givenProcessor()
                .andSourceFilesFromFolders("/constructor")
                .andUseModules("wakamiti.extension")
                .whenCompiled()
                .thenExpectThat()
                .compilationFails()
                .andThat()
                .compilerMessage().ofKindError()
                .contains("[ExtensionProcessor] at tests.MyExtensionTest :: ",
                        "@Extension class must have a no-arguments constructor")
                .executeTest();
    }


}
