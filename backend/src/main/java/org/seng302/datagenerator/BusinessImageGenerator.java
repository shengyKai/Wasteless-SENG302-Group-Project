package org.seng302.datagenerator;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class BusinessImageGenerator {
    private final Path imageUploadsRoot;
    private final Connection conn;
    private Resource[] demoImages;
    private Random random = new Random();

    public BusinessImageGenerator(Connection conn) {
        this.conn = conn;

        imageUploadsRoot = Paths.get(ExampleDataFileReader.readPropertiesFile("/application.properties").get("storage-directory"));
        try {
            var loader = BusinessImageGenerator.class.getClassLoader();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
            this.demoImages = resolver.getResources("classpath*:org/seng302/datagenerator/example-business-images/**");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Could not initialize example images folder for businesses.");
        }
    }

    /**
     * Generates images for each business provided in businessIds
     * @param businessIds The set of businesses to generate images for
     * @param lowerBound The minimum number of images to generate for each business
     * @param upperBound The maximum number of images to generate for each business
     * @return List of generated image IDs
     */
    public List<Long> generateBusinessImages(List<Long> businessIds, int lowerBound, int upperBound) {
        List<Long> generatedIds = new ArrayList<>();
        for (var businessId : businessIds) {
            generatedIds.addAll(generateImagesForBusiness(businessId, lowerBound, upperBound));
        }
        return generatedIds;
    }

    /**
     * Generates images for a single provided business
     * @param businessId The ID of the business to generate images for
     * @param lowerBound The minimum number of images to generate for this business
     * @param upperBound The maximum number of images to generate for this business
     * @return List of generated image IDs
     */
    private List<Long> generateImagesForBusiness(Long businessId, int lowerBound, int upperBound) {
        List<Long> generatedIds = new ArrayList<>();
        var numberOfImagesToGenerate = random.nextInt(upperBound-lowerBound+1) + lowerBound;
        for (int imageOrder = 0; imageOrder < numberOfImagesToGenerate; imageOrder++) {
            var image = findRandomImage();
            String filename = UUID.randomUUID().toString();
            String fileType = getExtension(image.getFilename()).orElseThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not locate file type for:" + image.getFilename());
            filename += fileType;

            store(image, filename);
            generatedIds.add(createInsertImageSQL(businessId, filename, imageOrder));
        }
        return generatedIds;
    }

    /**
     * Returns a random image from the set of available images
     * @return Resource pointing at an image
     */
    private Resource findRandomImage() {
        var maxValue = demoImages.length;
        return demoImages[random.nextInt(maxValue)];
    }

    /**
     * Inserts a record into the image table to add an image to a business
     * @param businessId ID of the business to add to
     * @param filename Name of the filename to save
     * @param order The order attribute for the image.
     * @return The ID of the generated image
     * @return The ID of the generated image
     */
    private long createInsertImageSQL(Long businessId, String filename, int order) {
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO image (filename, business_id, image_order) " +
                            "VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            stmt.setObject(1, filename);
            stmt.setObject(2, businessId);
            stmt.setObject(3, order);

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
    public Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
}
