/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.extension.processor;


import com.sun.source.util.Trees;
import es.wakamiti.extension.annotation.Extension;
import es.wakamiti.extension.annotation.ExtensionPoint;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class ProcessorHelper {

    private final Filer filer;
    private final Elements elementUtils;
    private final Types typeUtils;
    private final RoundEnvironment roundEnv;
    private final Trees trees;
//    private final TreeMaker treeMaker;
//    private final Names names;



    ProcessorHelper(
            ProcessingEnvironment processingEnv,
            RoundEnvironment roundEnv
    ) {
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.roundEnv = roundEnv;
        this.trees = Trees.instance(processingEnv);
//        com.sun.tools.javac.processing.JavacProcessingEnvironment javacEnv =
//                (com.sun.tools.javac.processing.JavacProcessingEnvironment) processingEnv;
//        this.treeMaker = TreeMaker.instance(javacEnv.getContext());
//        this.names = Names.instance(javacEnv.getContext());
    }

    final boolean moduleInfoExist() {
        TypeElement element = getElementAnnotatedWith(Extension.class)
                .or(() -> getElementAnnotatedWith(ExtensionPoint.class))
                .orElseThrow();
        var module = elementUtils.getModuleOf(element);
        return module != null && !module.isUnnamed();
    }

    Set<TypeElement> getElementsAnnotatedWith(
            Class<? extends Annotation> type
    ) {
        return roundEnv.getElementsAnnotatedWith(type).stream()
                .map(TypeElement.class::cast)
                .collect(Collectors.toSet());
    }

    Optional<TypeElement> getElementAnnotatedWith(
            Class<? extends Annotation> type
    ) {
        return getElementsAnnotatedWith(type).stream().findFirst();
    }

    /**
     * Retrieves the annotation mirror for the given annotation from the element.
     *
     * @param element    the element to retrieve the annotation mirror from
     * @param annotation the annotation name
     * @return the annotation mirror
     */
    Optional<AnnotationMirror> getAnnotationMirror(
            Element element,
            Class<? extends Annotation> annotation
    ) {
        return element.getAnnotationMirrors().stream()
                .filter(a ->
                        a.getAnnotationType().asElement().getSimpleName().toString().equals(annotation.getSimpleName()))
                .map(AnnotationMirror.class::cast)
                .findFirst();
    }

    Set<TypeElement> findParentsAnnotatedWith(
            TypeElement element,
            Class<? extends Annotation> annotation
    ) {
        List<TypeElement> results = new LinkedList<>(element.getInterfaces().stream()
                .map(this::asElement)
                .map(TypeElement.class::cast)
                .filter(it -> getAnnotationMirror(it, annotation).isPresent())
                .toList());

        results.addAll(
                results.stream()
                    .flatMap(it -> findParentsAnnotatedWith(it, annotation).stream())
                    .toList()
        );

        Optional.ofNullable(element.getSuperclass())
                .map(this::asElement)
                .map(TypeElement.class::cast)
                .ifPresent(it -> results.addAll(findParentsAnnotatedWith(it, annotation)));

        return new HashSet<>(results);
    }

    ModuleElement getModuleOf(TypeElement element) {
        return elementUtils.getModuleOf(element);
    }

    <T extends ModuleElement.Directive> Stream<T> findDirective(Class<T> type) {
        return findDirective(getModuleOf(getElementAnnotatedWith(Extension.class).orElseThrow()), type);
    }

    <T extends ModuleElement.Directive> Stream<T> findDirective(ModuleElement module, Class<T> type) {
        return module.getDirectives().stream().filter(type::isInstance).map(type::cast);
    }

    boolean hasDefaultConstructor(TypeElement typeElement) {
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) enclosed;
                if (constructor.getParameters().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    void addDefaultConstructor(TypeElement typeElement) {
//        JCTree tree = (JCTree) trees.getTree(typeElement);
//        if (tree != null) {
//            JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) tree;
//            JCTree.JCMethodDecl noArgsConstructor = treeMaker.MethodDef(
//                    treeMaker.Modifiers(1L),        // public
//                    names.fromString("<init>"),       // Constructor name
//                    null,                                // Void return
//                    com.sun.tools.javac.util.List.nil(), // Without generic parameters
//                    com.sun.tools.javac.util.List.nil(), // Without parameters
//                    com.sun.tools.javac.util.List.nil(), // Without throws
//                    treeMaker.Block(0L, com.sun.tools.javac.util.List.nil()), // Empty
//                    null                                 // Without default value
//            );
//            classDecl.defs = classDecl.defs.append(noArgsConstructor);
//        }
    }


    PackageElement getPackageOf(
            TypeElement element
    ) {
        return elementUtils.getPackageOf(element);
    }

    Element asElement(
            TypeMirror type
    ) {
        return typeUtils.asElement(type);
    }


    String writeMetaInfFile(
            String filename,
            List<String> lines
    ) throws IOException {
        if (lines.isEmpty()) {
            return "";
        }
        FileObject resourceFile = filer.createResource(StandardLocation.CLASS_OUTPUT,
                "", "META-INF/" + filename);

        try (BufferedWriter writer = new BufferedWriter(resourceFile.openWriter())) {
            for (String line : lines) {
                writer.append(line);
                writer.newLine();
            }
        }
        return resourceFile.getName();
    }

}
