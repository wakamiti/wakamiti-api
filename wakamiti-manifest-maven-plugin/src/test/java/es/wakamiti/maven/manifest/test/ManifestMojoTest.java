/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.maven.manifest.test;


import es.wakamiti.extension.manifest.PluginManifest;
import io.takari.maven.testing.TestResources5;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenPluginTest;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.*;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static io.takari.maven.testing.AbstractTestResources.assertFilesNotPresent;
import static io.takari.maven.testing.AbstractTestResources.assertFilesPresent;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;


@MavenVersions({"3.9.9"})
public class ManifestMojoTest {

    @RegisterExtension
    final TestResources5 resources = new TestResources5() {
        @Override
        public File getBasedir(String project) throws IOException {
            File baseDir = super.getBasedir(project);
            replaceVariables(baseDir.listFiles(File::isFile));
            return baseDir;
        }
    };

    private final MavenRuntime maven;


    ManifestMojoTest(MavenRuntime.MavenRuntimeBuilder mavenBuilder) throws Exception {
        this.maven = mavenBuilder.build();
    }

    @MavenPluginTest
    void testWhenDefaultWithSuccess() throws Exception {
        test("default");
    }

    @MavenPluginTest
    void test2() throws Exception {
        test("parent");
    }

    private void test(String type) throws Exception {
        // Prepare
        String manifestPath = "target/classes/META-INF/MANIFEST.MF";
        File basedir = resources.getBasedir(type);

        // Act
        maven.forProject(basedir)
                .execute("package")
                .assertErrorFreeLog();

        // Check
        assertFilesNotPresent(basedir, "target/classes/META-INF/extensions");
        assertFilesPresent(basedir, manifestPath);

        Manifest expectedManifest = manifest("MANIFEST-%s.MF".formatted(type));
        Manifest resultManifest = manifest(new File(basedir, manifestPath));
        Manifest jarManifest = jarManifest(new File(basedir, "target/example-wakamiti-plugin-0.0.1.jar"));

        compare(expectedManifest, resultManifest);
        compare(expectedManifest, jarManifest);
    }

    private void compare(Manifest expected, Manifest result) {
        assertThat(result.getMainAttributes())
                .containsAllEntriesOf(expected.getMainAttributes());

        PluginManifest expectedPluginManifest = PluginManifest.of(expected);
        PluginManifest resultPluginManifest = PluginManifest.of(result);

        assertThat(resultPluginManifest.asMap().keySet())
                .containsExactly(expectedPluginManifest.asMap().keySet().toArray(new String[0]));

        assertThat(resultPluginManifest).extracting(PluginManifest::id)
                .isNotNull()
                .isEqualTo(expectedPluginManifest.id());
        assertThat(resultPluginManifest).extracting(PluginManifest::version)
                .isNotNull()
                .isEqualTo(expectedPluginManifest.version());
        assertThat(resultPluginManifest).extracting(PluginManifest::module)
                .isNotNull()
                .isEqualTo(expectedPluginManifest.module());
        assertThat(resultPluginManifest).extracting(PluginManifest::pluginJarFile)
                .isNotNull()
                .isEqualTo(expectedPluginManifest.pluginJarFile());
        assertThat(resultPluginManifest).extracting(PluginManifest::name)
                .isNotNull()
                .isEqualTo(expectedPluginManifest.name());
        assertThat(resultPluginManifest).extracting(PluginManifest::description)
                .isNotNull()
                .isEqualTo(expectedPluginManifest.description());
        assertThat(resultPluginManifest).extracting(PluginManifest::parentArtifact)
                .matches(p -> (isNull(p) && isNull(expectedPluginManifest.parentArtifact()))
                        || p.equals(expectedPluginManifest.parentArtifact()));
        assertThat(resultPluginManifest).extracting(PluginManifest::parentModule)
                .matches(p -> (isNull(p) && isNull(expectedPluginManifest.parentModule()))
                        || p.equals(expectedPluginManifest.parentModule()));
        assertThat(resultPluginManifest).extracting(PluginManifest::dependencies, as(InstanceOfAssertFactories.LIST))
                .isNotNull()
                .containsAll(expectedPluginManifest.dependencies());
        assertThat(resultPluginManifest).extracting(PluginManifest::extensions, as(InstanceOfAssertFactories.MAP))
                .isNotNull()
                .containsExactlyEntriesOf(expectedPluginManifest.extensions());
    }

    private Properties properties() throws IOException {
        Properties props = new Properties();
        props.load(ClassLoader.getSystemClassLoader().getResourceAsStream("test.properties"));
        return props;
    }

    private void replaceVariables(File... files) throws IOException {
        if (files == null) return;
        for (File file : files) {
            String content = replaceVariables(FileUtils.readFileToString(file, UTF_8));
            FileUtils.write(file, content, UTF_8);
        }
    }

    private String replaceVariables(String str) throws IOException {
        Properties props = properties();
        AtomicReference<String> content = new AtomicReference<>(str);
        props.forEach((k, v) ->
                content.updateAndGet(x -> x.replace("@%s@".formatted(k), v.toString())));
        return content.get();
    }

    private Manifest manifest(File file) throws IOException {
        replaceVariables(file);
        return new Manifest(new FileInputStream(file));
    }

    private Manifest manifest(String path) throws IOException {
        try (InputStream stream = resource(path)) {
            String content = replaceVariables(new String(stream.readAllBytes()));
            return new Manifest(new ByteArrayInputStream(content.getBytes()));
        }
    }

    private Manifest jarManifest(File file) throws IOException {
        try(JarFile jar = new JarFile(file)) {
            return jar.getManifest();
        }
    }

    private InputStream resource(String path) {
        return Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(path));
    }

}
