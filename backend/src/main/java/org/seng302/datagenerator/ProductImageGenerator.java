package org.seng302.datagenerator;

import org.seng302.leftovers.service.StorageService;
import org.seng302.leftovers.service.StorageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class ProductImageGenerator {
    private final Path root = Paths.get("exampleImages");
    @Autowired
    private StorageService storageService;
    private Connection conn;

    public void ProductGenerator(Connection conn) {
        try {
            Files.createDirectory(root);
        } catch (FileAlreadyExistsException existsException) {
            // don't do anything if directory exists
        } catch (IOException ioException) {
            throw new RuntimeException("Could not initialize example images folder.");
        }

        this.conn = conn;
    }

    /**
     * Given a product ID and a products name,
     * attempts to create a copy of the image to the images directory
     * and saves the record to the database
     * @param productId The ID of the product
     * @param productName The name of the demo image
     * @throws SQLException
     */
    public void addImageToProduct(Long productId, String productName) throws SQLException {
        String filename = UUID.randomUUID().toString();
        if (saveImageToSystem(productName + ".png", filename)) {
            createInsertImageSQL(productId, filename);
        } else {
            System.out.println("File '" + productName + "' Could not be found");
        }
    }

    /**
     * Given a noun, attempts to find the image associated with that noun.
     * @param noun The product to find
     * @return Optional of type File.
     * @throws IOException
     */
    private Optional<File> findImage(String noun) throws IOException {
        Optional<Path> foundFile = Files.walk(this.root, 1).filter(path -> path.relativize(this.root).toString().contains(noun)).findFirst();
        return foundFile.map(Path::toFile);
    }

    /**
     * Given an example image, copies the image into the images directory.
     * @param demoImageName The name of the demo image
     * @param fileName The name of the file to save
     * @return true if file successfully saved.
     */
    private boolean saveImageToSystem(String demoImageName, String fileName) {
        File originalImage = root.resolve(demoImageName).toFile();
        if (originalImage.exists()) {
            storageService.store((MultipartFile) originalImage, fileName);
            return true;
        }
        return false;
    }

    /**
     * Creates and inserts an image record into the database, given an image filename and product id.
     * @param productId The ID of the product related to the new image
     * @param filename The filename of the new image
     * @return The ID of the new image record.
     * @throws SQLException
     */
    private long createInsertImageSQL(Long productId, String filename) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO image (filename, image_id, image_order) " +
                "VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        );
        stmt.setObject(1, filename);
        stmt.setObject(2, productId);
        stmt.setObject(3, 1);

        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        keys.next();
        return keys.getLong(1);
    }
}
