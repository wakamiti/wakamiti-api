/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.config;


import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;


/**
 * A configuration factory is responsible for creating new instances of
 * {@link Configuration} from a variety of alternative sources, such as URIs,
 * classpath resources, maps, and property objects. Every new configuration
 * object should be created using a factory.
 * <p>
 * When building a configuration from an external resource, the builder would
 * automatically detect the resource type (usually by looking at its file
 * extension) and handle the content properly, accepting multiple formats such
 * as JSON, YAML, XML, and .properties files.
 */
public interface ConfigurationFactory {

    /**
     * Retrieves the default instance of the ConfigurationFactory.
     *
     * @return The default instance of ConfigurationFactory.
     * @throws ConfigurationException If an error occurs while retrieving the
     *                                default instance.
     */
    static ConfigurationFactory instance() {
        try {
            return ServiceLoader.load(ConfigurationFactory.class).stream()
                    .findFirst()
                    .orElseThrow()
                    .type()
                    .getConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Create a new configuration composed of two other configurations. When the same
     * property is present in two or more configurations, the value from the
     * delta configuration will prevail (except when it has an empty value)
     */
    Configuration merge(
            Configuration base,
            Configuration delta
    );


    /**
     * Create a new empty configuration
     */
    Configuration empty();


    /**
     * Create a new configuration from a class annotated with
     * {@link AnnotatedConfiguration}
     *
     * @param configuredClass Class annotated with {@link AnnotatedConfiguration}
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration fromAnnotation(
            Class<?> configuredClass
    );


    /**
     * Create a new configuration from a {@link AnnotatedConfiguration} annotation
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration fromAnnotation(
            AnnotatedConfiguration annotation
    );


    /**
     * Create a new configuration from the OS environment properties
     */
    Configuration fromEnvironment();


    /**
     * Create a new configuration from the {@link System} properties
     */
    Configuration fromSystem();


    /**
     * Create a new configuration from the resource of the specified path
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration fromPath(
            Path path,
            Charset charset
    );



    /**
     * Create a new configuration from the specified URI.
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration fromURI(
            URI uri,
            Charset charset
    );


    /**
     * Create a new configuration from the specified classpath resource.
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration fromResource(
            String resource,
            Charset charset,
            ClassLoader classLoader
    );



    /**
     * Create a new configuration from a {@link Properties} object
     */
    Configuration fromProperties(
            Properties properties
    );


    /**
     * Create a new configuration from a {@link Map} object
     */
    Configuration fromMap(
            Map<String, String> propertyMap
    );



    /**
     * Create a new configuration from directly passed strings, using each two entries as a pair of
     * <tt>key,value</tt>.
     * @throws IllegalArgumentException if the number of strings is not even
     */
    default Configuration fromPairs(
            String... pairs
    ) {
        if (pairs.length % 2 == 1) {
            throw new IllegalArgumentException("Number of arguments must be even");
        }
        Map<String,String> map = new LinkedHashMap<>();
        for (int i=0;i<pairs.length;i+=2) {
            map.put(pairs[i],pairs[i+1]);
        }
        return fromMap(map);
    }



    /**
     * Create a new empty configuration according the given property definitions
     * <p>
     * Defined properties will be set to their default value if it exists
     * @see PropertyDefinition
     */
    Configuration accordingDefinitions(
            Collection<PropertyDefinition> definitions
    );



    /**
     * Create a new empty configuration according the property definitions from the given path.
     * <p>
     * Defined properties will be set to their default value if it exists
     * @see PropertyDefinition
     */
    Configuration accordingDefinitionsFromPath(
            Path path,
            Charset charset
    );


    /**
     * Create a new empty configuration according the property definitions from the given URI.
     * <p>
     * You can use the schema <pre>classpath:</pre> to reference classpath resources.
     * <p>
     * Defined properties will be set to their default value if it exists
     * @see PropertyDefinition
     */
    Configuration accordingDefinitionsFromURI(
            URI uri,
            Charset charset
    );


    /**
     * Create a new empty configuration according the property definitions from the given
     * classpath resource.
     * <p>
     * Defined properties will be set to their default value if it exists
     * @see PropertyDefinition
     */
    Configuration accordingDefinitionsFromResource(
            String resource,
            Charset charset,
            ClassLoader classLoader
    );

}
