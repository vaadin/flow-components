package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.data.provider.ListDataProvider;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;

/**
 * Helper class a replacement for Vaadin 7 FilesystemContainer Doesn't support
 * hierarchy.
 */
class FileDataProvider {
    /**
     *
     * @param root
     *            root folder
     * @param filter
     *            Regexp for filtering files.
     * @return a ListDataProvider of File
     */
    public static ListDataProvider<File> create(URI root, String filter,
            Logger logger) {
        return new ListDataProvider<>(getFiles(root, filter, logger));
    }

    /**
     *
     * @param root
     *            root folder
     * @param filter
     *            Regexp for filtering files.
     * @return a List of Files
     */
    public static List<File> getFiles(URI root, String filter, Logger logger) {
        List<File> pathFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(root), 1)) {
            paths.forEach(filePath -> {
                if (!Files.isDirectory(filePath) && (filePath.getFileName()
                        .toString().matches(filter))) {
                    pathFiles.add(filePath.toFile());
                }
            });
        } catch (IOException e) {
            logger.warn("Could not test Excel sheet " + e.getMessage());
        }
        pathFiles.sort(
                (File f1, File f2) -> f1.getName().compareTo(f2.getName()));
        return pathFiles;
    }

}
