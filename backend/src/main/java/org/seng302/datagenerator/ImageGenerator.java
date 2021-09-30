package org.seng302.datagenerator;

import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class ImageGenerator {
    private final Path imageUploadsRoot;
    private final Connection conn;
    private Random random = new Random();

    public ImageGenerator(Connection conn) {
        this.conn = conn;

        imageUploadsRoot = Paths.get(ExampleDataFileReader.readPropertiesFile("/application.properties").get("storage-directory"));
        try {
            var loader = ImageGenerator.class.getClassLoader();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
            ImageEntityType.BUSINESS.setImages(resolver.getResources("classpath*:org/seng302/datagenerator/example-business-images/**"));
            ImageEntityType.USER.setImages(resolver.getResources("classpath*:org/seng302/datagenerator/example-user-images/**"));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Could not initialize example images folder for businesses and users.");
        }
    }

    /**
     * Represents one of two entity types to generate images for
     */
    @Getter
    public enum ImageEntityType {
        BUSINESS("business_id"),
        USER("user_id");

        public void setImages(Resource[] images) {
            this.imageList = images;
        }

        private String field;
        private Resource[] imageList;

        ImageEntityType(String field) {
            this.field = field;
        }
    }

    /**
     * Generates images for each entity provided
     * @param Ids The set of businesses to generate images for
     * @param lowerBound The minimum number of images to generate for each entity
     * @param upperBound The maximum number of images to generate for each entity
     * @param entityType The type of entity to generate images for
     * @return List of generated image IDs
     */
    public List<Long> generateEntityImages(List<Long> Ids, int lowerBound, int upperBound, ImageEntityType entityType) {
        List<Long> generatedIds = new ArrayList<>();
        for (var id : Ids) {
            generatedIds.addAll(generateImagesForEntity(id, lowerBound, upperBound, entityType));
        }
        return generatedIds;
    }

    /**
     * Generates images for a single provided business
     * @param businessId The ID of the business to generate images for
     * @param lowerBound The minimum number of images to generate for this business
     * @param upperBound The maximum number of images to generate for this business
     * @param entityType The type of entity to generate images for
     * @return List of generated image IDs
     */
    private List<Long> generateImagesForEntity(Long businessId, int lowerBound, int upperBound, ImageEntityType entityType) {
        List<Long> generatedIds = new ArrayList<>();
        var numberOfImagesToGenerate = random.nextInt(upperBound-lowerBound+1) + lowerBound;
        for (int imageOrder = 0; imageOrder < numberOfImagesToGenerate; imageOrder++) {
            var image = findRandomImage(entityType);
            String filename = UUID.randomUUID().toString();
            String fileType = getExtension(image.getFilename()).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not locate file type for:" + image.getFilename()));
            filename += "." + fileType;

            store(image, filename);
            generatedIds.add(createInsertImageSQL(businessId, filename, imageOrder, entityType));
        }
        return generatedIds;
    }

    /**
     * Returns a random image from the set of available images for the given type
     * @return Resource pointing at an image
     */
    private Resource findRandomImage(ImageEntityType entity) {
        var maxValue = entity.getImageList().length;
        Resource image;
        do image = entity.getImageList()[random.nextInt(maxValue)]; while (image.getFilename() != null && image.getFilename().isEmpty());
        return image;
    }


    /**
     * Inserts a record into the image table to add an image to a business or user
     * @param Id ID of the entity to add to
     * @param filename Name of the filename to save
     * @param order The order attribute for the image.
     * @return The ID of the generated image
     * @return The ID of the generated image
     */
    private long createInsertImageSQL(Long Id, String filename, int order, ImageEntityType entityType) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO image (filename, " + entityType.getField() + ", image_order, filename_thumbnail, created) " +
                        "VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
            stmt.setObject(1, filename);
            stmt.setObject(2, Id);
            stmt.setObject(3, order);
            stmt.setObject(4, filename);
            stmt.setObject(5, Instant.now());

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            return keys.getLong(1);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * This is copied and modified from storageServiceImpl
     * Saves a given file to the system
     * @param file The file to save
     * @param filename The name of the new file.
     */
    public void store(Resource file, String filename) {
        try {
            Files.copy( file.getInputStream(), this.imageUploadsRoot.resolve(filename));

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
    }

    /**
     * Gets the file type from a given file
     * @param filename name of the file
     * @return File type
     */
    private Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
}
