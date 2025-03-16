/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.maven.manifest;


import es.wakamiti.extension.manifest.PluginManifest;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.jar.Manifest;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

import static es.wakamiti.extension.manifest.PluginManifest.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;


@Mojo(
        name = "manifest",
        defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
        requiresDependencyResolution = ResolutionScope.TEST)
public class ManifestMojo extends AbstractMojo {


    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.outputDirectory}/META-INF/MANIFEST.MF")
    private File manifestDir;

    @Parameter
    private ID parent;

    @Override
    public void execute() throws MojoExecutionException {
        if (!project.getPackaging().equalsIgnoreCase("jar")) {
            return;
        }
        try {
            Manifest manifest = new Manifest();
            populateManifest(manifest);
            manifestDir.getParentFile().mkdirs();
            try (OutputStream os = Files.newOutputStream(manifestDir.toPath(), CREATE, TRUNCATE_EXISTING)) {
                manifest.write(os);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error writing manifest", e);
        }
    }

    private void populateManifest(Manifest manifest) throws IOException {
        Artifact api = getApi();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.putValue("Manifest-Version", "1.0");
        mainAttributes.putValue("Created-By", "Wakamiti Manifest Maven Plugin");
        mainAttributes.putValue("Specification-Vendor", api.getGroupId());
        mainAttributes.putValue("Specification-Title", api.getArtifactId());
        mainAttributes.putValue("Specification-Version", api.getVersion());
        mainAttributes.putValue("Implementation-Vendor", project.getGroupId());
        mainAttributes.putValue("Implementation-Title", project.getArtifactId());
        mainAttributes.putValue("Implementation-Version", project.getVersion());

        Attributes sectionAttributes = new Attributes();
        sectionAttributes.putValue(PLUGIN_ID, project.getGroupId() + ":" + project.getArtifactId());
        sectionAttributes.putValue(PLUGIN_VERSION, project.getVersion());
        sectionAttributes.putValue(PLUGIN_MODULE, moduleName(project.getArtifact()));
        sectionAttributes.putValue(PLUGIN_JAR_FILE, project.getBuild().getFinalName() + "." + project.getPackaging());
        getParent().ifPresent(p -> {
            sectionAttributes.putValue(PLUGIN_PARENT_ARTIFACT, toCoordinates(p));
            sectionAttributes.putValue(PLUGIN_PARENT_MODULE, moduleName(p));
        });
        sectionAttributes.putValue(PLUGIN_NAME, project.getName());
        sectionAttributes.putValue(PLUGIN_DESCRIPTION, project.getDescription());
        sectionAttributes.putValue(PLUGIN_DEPENDENCIES, getDependencies());
        File extensions = new File(project.getBuild().getOutputDirectory(), "/META-INF/extensions");
        if (extensions.exists()) {
            sectionAttributes.putValue(PLUGIN_EXTENSIONS, getExtensions(extensions));
        }
        manifest.getEntries().put(PLUGIN_SECTION, sectionAttributes);
    }

    private String getDependencies() {
        return project.getArtifacts().stream()
                .filter(a -> !a.getScope().equalsIgnoreCase("test"))
                .map(this::toCoordinates)
                .collect(Collectors.joining(PluginManifest.DELIMITER));
    }

    private Artifact getApi() {
        String apiDependency = ":wakamiti-api:";
        return project.getArtifacts().stream()
                .filter(a -> !a.getScope().equalsIgnoreCase("test"))
                .filter(a -> a.getId().contains(apiDependency))
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException("Cannot determine api dependency: %s".formatted(apiDependency)));
    }

    private Optional<Artifact> getParent() {
        return Optional.ofNullable(parent).map(p ->
                project.getArtifacts().stream()
                        .filter(a -> !a.getScope().equalsIgnoreCase("test"))
                        .filter(a -> a.getId().startsWith(p.toString()))
                        .findFirst()
                        .orElseThrow(() ->
                                new NoSuchElementException("Cannot determine parent dependency: %s".formatted(p))));
    }

    private String getExtensions(File extensions) throws IOException {
        String content = String.join(PluginManifest.DELIMITER, FileUtils.readLines(extensions, UTF_8));
        FileUtils.delete(extensions);
        return content;
    }

    private String toCoordinates(Artifact artifact) {
        return "%s:%s:%s".formatted(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
    }

    private String moduleName(Artifact artifact) {
        File file = artifact.getFile();
        NoSuchElementException ex = new NoSuchElementException("Cannot find a Java module in %s".formatted(file));
        var moduleReferences = ModuleFinder.of(file.toPath()).findAll();
        if (moduleReferences == null || moduleReferences.isEmpty()) {
            throw ex;
        }
        return moduleReferences.stream().findFirst().orElseThrow(() -> ex).descriptor().name();
    }

    public static class ID {
        public String groupId;
        public String artifactId;

        public String toString() {
            return groupId + ":" + artifactId;
        }
    }

}
