package org.seng302.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Image;
import org.seng302.persistence.ImageRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@CrossOrigin(origins = "http://localhost:9499")
@RequestMapping(path = "image")

public class ImageController {
    /*private static final Logger logger = LogManager.getLogger(org.seng302.controllers.UserController.class.getName());
  @Autowired
    ImageRepository imageRepository;
    @PostMapping("/upload")
    public BodyBuilder uplaodImage(@RequestParam("file") MultipartFile file) throws IOException {

        System.out.println("Original Image Byte Size - " + file.getBytes().length);
        Image img = new Image(file.getOriginalFilename(), file.getContentType(),
                file.getBytes());
        imageRepository.save(img);

        logger.info("Image");
        logger.info(img);
        return ResponseEntity.status(HttpStatus.OK);
    }
    @GetMapping(path = { "/get/{imageName}" })
    public Image getImage(@PathVariable("imageName") String imageName) throws IOException {
        final Optional<Image> retrievedImage = imageRepository.findByName(imageName);
        Image img = new Image(retrievedImage.get().getName(), retrievedImage.get().getType(), retrievedImage.get().getPicByte());
        return img;
    }*/
}
