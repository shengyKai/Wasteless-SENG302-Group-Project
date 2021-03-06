package org.seng302.leftovers.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.exceptions.InternalErrorResponseException;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class StorageService {
    private static final Logger logger = LogManager.getLogger(StorageService.class);

    @Value("${storage-directory}")
    private Path root;

    public void init() {
        logger.warn("Initialising StorageService");
        try {
            Files.createDirectory(root);
        } catch (FileAlreadyExistsException ignored) {
            // It is alright if the /uploads folder already exists.
        } catch (IOException e) {
            throw new InternalErrorResponseException("Could not initialize folder for upload!");
        }
    }

    public void store(InputStream file, String filename) {
        logger.info(() -> String.format("Storing image with filename=%s", filename));
        try {
            Files.copy(file, this.root.resolve(filename));
            
        } catch (Exception e) {
            logger.error(e);
            throw new InternalErrorResponseException("Failed to store file", e);
        }
    }

    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new InternalErrorResponseException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new InternalErrorResponseException("Error: " + e.getMessage());
        }
    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new InternalErrorResponseException("Could not load the files!");
        }
    }

    // Took out deleteAll as product images should not be deleted all at once
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    /**
     * Deletes a single file from the disk
     * @param filename Filename to delete
     */
    public void deleteOne(String filename) {
        if (filename.isEmpty() || filename.isBlank()) {
            throw new ValidationResponseException("Filename not given for deletion");
        }
        Path path = root.resolve(filename);

        try {
            Files.delete(path);
        } catch (IOException e) {
            logger.warn("Failed to delete: \"{}\" due to {}", filename, e);
        }
    }
}
