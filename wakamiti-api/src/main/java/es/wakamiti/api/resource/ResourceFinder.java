package es.wakamiti.api.resource;


import es.wakamiti.api.Wakamiti;
import es.wakamiti.api.lang.WakamitiException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;


/**
 * Utility class for finding and reading resources from the file system.
 * <p>
 * This class provides methods to search for files matching a specific pattern
 * and to read their content. It uses the Java NIO file API to perform file operations.
 */
public class ResourceFinder {

    /**
     * Finds resources in the file system starting from a given path and matching a glob pattern.
     *
     * @param startingPath the starting path to search for resources
     * @param globPattern  the glob pattern to match file names
     * @return a list of resources that match the given pattern
     * @throws WakamitiException if an I/O error occurs while reading the file system
     */
    public List<Resource> findResources(Path startingPath, String globPattern) {
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
        try (var stream = Files.walk(startingPath)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> pathMatcher.matches(path.getFileName()))
                    .map(file -> new Resource(contentType(file), file.toUri(), file, () -> newReader(file)))
                    .toList();
        } catch (IOException e) {
            throw new WakamitiException("Error reading resources from {file}", startingPath, e);
        }
    }

    /**
     * Determines the MIME type of file based on its extension.
     *
     * @param path the path of the file
     * @return the MIME type of the file
     */
    private ContentType contentType(Path path) {
        String name = path.getFileName().toString();
        int index = name.lastIndexOf('.');
        String extension = index > 0 ? name.substring(index + 1) : "txt";
        return Wakamiti.of().contentTypes()
                .filter(c -> c.extension().equalsIgnoreCase(extension))
                .findFirst()
                .get();
    }

    /**
     * Creates a new InputStream to read the content of a file.
     *
     * @param path the path of the file
     * @return an InputStream to read the file content
     * @throws WakamitiException if an I/O error occurs while opening the file
     */
    private InputStream newReader(Path path) {
        Path absolutePath = path.toAbsolutePath();
        try {
            return Files.newInputStream(absolutePath);
        } catch (IOException e) {
            throw new WakamitiException("Cannot read file {file}", absolutePath, e);
        }
    }

}