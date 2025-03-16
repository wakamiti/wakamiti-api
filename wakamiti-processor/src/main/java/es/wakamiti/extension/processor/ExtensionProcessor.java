/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension.processor;


import es.wakamiti.extension.annotation.Extension;
import es.wakamiti.extension.annotation.ExtensionPoint;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;


@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ExtensionProcessor extends AbstractProcessor {

    private static final Map<Class<? extends Annotation>, ElementKind> KIND = Map.of(
            ExtensionPoint.class, INTERFACE,
            Extension.class, CLASS
    );
    private static final String MODULE_INFO = "module-info.java";

    /**
     * Maps the class names of service provider interfaces to the
     * class names of the concrete classes which implement them.
     */
    private final Map<TypeElement, List<TypeElement>> providers = new LinkedHashMap<>();
    private ProcessorHelper helper;
    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                ExtensionPoint.class.getName(),
                Extension.class.getName()
        );
    }

    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv
    ) {
        this.helper = new ProcessorHelper(processingEnv, roundEnv);
        this.messager = messager();

        try {
            if (roundEnv.processingOver()) {
                writeOutputFile();
            } else {
                processAnnotations();
                if (messager.hasErrors()) {
                    messager.showErrors();
                }
            }
        } catch (Exception e) {
            messager.fatal(e);
        }
        return false;
    }

    private Messager messager() {
        if (messager == null) {
            messager = new Messager(processingEnv);
        }
        return messager;
    }

    private void writeOutputFile() throws IOException {
        String file = helper.writeMetaInfFile(
                "extensions",
                providers.entrySet().stream().map(
                        e -> "%s=%s".formatted(
                                e.getKey().getQualifiedName(),
                                e.getValue().stream().map(TypeElement::getQualifiedName)
                                        .collect(Collectors.joining(","))
                        )
                ).toList()
        );
        if (!file.isEmpty()) {
            messager.info("Generated extensions declaration file '{}'", file);
        }
    }

    private void processAnnotations() {
        var extensionPoints = getElementsAnnotatedWith(ExtensionPoint.class);
        var extensions = getElementsAnnotatedWith(Extension.class);
        if (messager.hasErrors()) return;

        if (!helper.moduleInfoExist()) {
            messager.error("Cannot find module definition. Ensure module-info.java exists");
            return;
        }

        for (TypeElement extensionElement : extensions) {
            var elementExtensionPoints = computeExtensionPoints(extensionElement);

            elementExtensionPoints.forEach(ep ->
                    providers.computeIfAbsent(ep, x -> new ArrayList<>()).add(extensionElement)
            );
        }
        if (messager.hasErrors()) return;

        extensionPoints.forEach(this::validateModule);
        providers.forEach(this::validateModule);
    }


    private Set<? extends TypeElement> getElementsAnnotatedWith(Class<? extends Annotation> annotation) {
        var elements = helper.getElementsAnnotatedWith(annotation);
        for (Element element : elements) {
            ElementKind validKind = KIND.get(annotation);
            if (validKind != element.getKind()) {
                messager.error(element, "@{} not valid (only processed for {})",
                        annotation.getSimpleName(),
                        validKind.name().toLowerCase().replaceAll("^(.+?)e?+$", "$1es"));
                continue;
            }
            if (validKind == CLASS) {
                AnnotationMirror annotationMirror = helper.getAnnotationMirror(element, annotation).orElseThrow();
                if (element.getModifiers().contains(Modifier.ABSTRACT)) {
                    messager.error(element, annotationMirror,
                            "@{} cannot be applied to an abstract class",
                            annotationMirror.getAnnotationType().asElement().getSimpleName());
                }
            }
        }
        return elements;
    }

    private Set<? extends TypeElement> computeExtensionPoints(
            TypeElement extensionElement
    ) {
        var results = helper.findParentsAnnotatedWith(extensionElement, ExtensionPoint.class);
        if (results.isEmpty()) {
            messager.error(extensionElement, "@{} class must implement or extend some extension point",
                    Extension.class.getSimpleName()
            );
        } else if (!helper.hasDefaultConstructor(extensionElement)) {
            messager.error(extensionElement, "@{} class must have a no-arguments constructor",
                    Extension.class.getSimpleName());
        }
        return results;
    }

//    private void addConstructor(TypeElement typeElement) {
//        if (!helper.hasDefaultConstructor(typeElement)) {
//            helper.addDefaultConstructor(typeElement);
//        }
//    }

    private void validateModule(
            TypeElement extensionPoint
    ) {
        validateExports(extensionPoint);

        // find <extension point> uses directive
        ModuleElement module = helper.getModuleOf(extensionPoint);
        helper.findDirective(module, ModuleElement.UsesDirective.class)
                .filter(it -> it.getService().getQualifiedName().equals(extensionPoint.getQualifiedName()))
                .findFirst().or(() -> {
                    messager.error(module,
                            """
                                    The usage of extension point '{1}' must be declared in the '{2}' file.
                                    Try add:
                                    uses {1};
                                    """, extensionPoint.getQualifiedName(), MODULE_INFO);
                    return Optional.empty();
                });
    }

    private void validateModule(
            TypeElement extensionPoint,
            List<TypeElement> extensions
    ) {
        extensions.forEach(this::validateExports);

        // find <extension point> provides with <extension> directive
        Supplier<String> fix = () -> """
                provides %s with
                \t%s;
                """
                .formatted(
                        extensionPoint.getQualifiedName(),
                        extensions.stream().map(TypeElement::getQualifiedName).collect(Collectors.joining(",\n\t"))
                );
        helper.findDirective(ModuleElement.ProvidesDirective.class)
                .filter(it -> it.getService().getQualifiedName().equals(extensionPoint.getQualifiedName()))
                .findFirst()
                .or(() -> {
                    messager.error(extensionPoint,
                            """
                                    The provision of extension point '{}' must be declared in the '{}' file.
                                    Try add:
                                    {}
                                    """, extensionPoint.getQualifiedName(), MODULE_INFO, fix.get());
                    return Optional.empty();
                })
                .map(it -> notIn(extensions, it.getImplementations()))
                .filter(it -> !it.isEmpty())
                .ifPresent(it -> {
                    messager.error(extensionPoint,
                            """
                                    The provision of extensions {} must be declared in the '{}' file.
                                    Try add:
                                    {}
                                    """, it, MODULE_INFO, fix.get());
                });

    }

    private void validateExports(
            TypeElement element
    ) {
        PackageElement elementPackage = helper.getPackageOf(element);
        ModuleElement module = helper.getModuleOf(element);

        // find <package> exports directive
        helper.findDirective(module, ModuleElement.ExportsDirective.class)
                .filter(it -> it.getPackage().equals(elementPackage))
                .findFirst().or(() -> {
                    messager.error(module,
                            """
                                    The package '{1}' must be exported in the '{2}' file.
                                    Try add:
                                    exports {1};
                                    """, elementPackage, MODULE_INFO);
                    return Optional.empty();
                });

        // find <package> opens directive
        helper.findDirective(module, ModuleElement.OpensDirective.class)
                .filter(it -> it.getPackage().equals(elementPackage))
                .findFirst().or(() -> {
                    messager.error(module,
                            """
                                    The package '{1}' must be opened to {2} in the '{3}' file.
                                    Try add:
                                    opens {1} to {2};
                                    """, elementPackage, "wakamiti.extension", MODULE_INFO);
                    return Optional.empty();
                });
    }


    private List<String> notIn(List<TypeElement> list1, List<? extends TypeElement> list2) {
        return list1.stream().map(TypeElement::getQualifiedName).map(Object::toString)
                .filter(it -> !list2.stream().map(TypeElement::getQualifiedName).map(Object::toString).toList()
                        .contains(it))
                .toList();
    }
}
