/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.maven.manifest;


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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Map;


@Mojo(
        name = "jar",
        defaultPhase = LifecyclePhase.PACKAGE,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
public class JarMojo extends AbstractMojo {


    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.outputDirectory}/META-INF/MANIFEST.MF")
    private File manifestDir;


    /**
     * Generates the JAR.
     *
     * @throws MojoExecutionException in case of an error.
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (!project.getPackaging().equalsIgnoreCase("jar")) {
            return;
        }

        try (FileSystem jarFS = FileSystems.newFileSystem(
                URI.create("jar:" + project.getArtifact().getFile().toURI()), Map.of())) {
            try (InputStream is = Files.newInputStream(manifestDir.toPath());
                 OutputStream os = Files.newOutputStream(jarFS.getPath("META-INF", "MANIFEST.MF"))) {
                new Manifest(is).write(os);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error assembling JAR", e);
        }
    }

}
