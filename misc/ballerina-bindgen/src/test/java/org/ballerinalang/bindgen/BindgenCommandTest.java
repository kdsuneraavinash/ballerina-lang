/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.bindgen;

import org.ballerinalang.bindgen.command.BindgenCommand;
import org.ballerinalang.maven.MavenResolver;
import org.ballerinalang.maven.exceptions.MavenResolverException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * Test the ballerina bindgen command options and functionality.
 *
 * @since 1.2
 */
public class BindgenCommandTest extends CommandTest {

    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("mvn-test-resources")).toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new BindgenCommandTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test whether the bindgen tool loads the existing platform libraries " +
            "specified in the Ballerina.toml file")
    public void testExistingPlatformLibraries() throws IOException, MavenResolverException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String[] args = {"-o=" + projectDir, "java.lang.Object", "org.apache.log4j.Logger"};

        // Platform libraries specified through maven dependencies should be automatically resolved.
        // Explicitly add a jar to test the platform libraries specified as a path.
        MavenResolver mavenResolver = new MavenResolver(projectDir);
        mavenResolver.resolve("log4j", "log4j", "1.2.17", false);

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Ballerina project detected at:"));
        Assert.assertTrue(output.contains("Following jars were added to the classpath:"));
        Assert.assertTrue(output.contains("snakeyaml-1.25.jar"));
        Assert.assertTrue(output.contains("commons-logging-1.1.1.jar"));
        Assert.assertTrue(output.contains("log4j-1.2.17.jar"));
        Assert.assertFalse(output.contains("Failed to add the following to classpath:"));
        Assert.assertFalse(output.contains("class could not be generated."));
    }

    @Test(description = "Test if the correct error is given for incorrect classpaths")
    public void testIncorrectClasspath() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String incorrectJarPath = Paths.get("./incorrect.jar").toString();
        String invalidDirPath = Paths.get("/User/invalidDir").toString();
        String[] args = {"-cp=" + incorrectJarPath + ", test.txt, " + invalidDirPath, "-o=" +
                projectDir, "java.lang.Object"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Failed to add the following to classpath:"));
        Assert.assertTrue(output.contains("test.txt"));
        Assert.assertTrue(output.contains(invalidDirPath));
        Assert.assertTrue(output.contains(incorrectJarPath));
    }

    @Test(description = "Test if the correct error is given for incorrect maven option value")
    public void testIncorrectMavenLibrary() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String[] args = {"-mvn=org.yaml.snakeyaml.1.25", "-o=" + projectDir, "java.lang.Object"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Error in the maven dependency provided."));
    }

    @Test(description = "Test if the correct error is given for an incorrect output path")
    public void testOutputPath() throws IOException {
        String incorrectPath = Paths.get("./incorrect").toString();
        String[] args = {"-o=" + incorrectPath, "java.lang.Object"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Error while generating Ballerina bindings:"));
        Assert.assertTrue(output.contains("Output path provided"));
    }

    @AfterClass
    public void cleanup() throws IOException {
        super.cleanup();
    }

    static class Copy extends SimpleFileVisitor<Path> {

        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;

        private Copy(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        private Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            Files.copy(file, toPath.resolve(fromPath.relativize(file).toString()), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }
}
