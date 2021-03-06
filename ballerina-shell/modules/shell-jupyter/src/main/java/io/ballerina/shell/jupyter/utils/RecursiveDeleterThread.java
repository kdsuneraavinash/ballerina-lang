package io.ballerina.shell.jupyter.utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A thread that will delete all the files in a given path recursively.
 *
 * @since 2.0.0
 */
public class RecursiveDeleterThread extends Thread {
    private final Path path;

    public RecursiveDeleterThread(Path path) {
        this.path = path;
    }

    @Override
    public void run() {
        try {
            Files.walkFileTree(this.path, new RecursiveDeleter());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
