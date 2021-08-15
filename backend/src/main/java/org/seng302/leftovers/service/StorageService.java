package org.seng302.leftovers.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

	void init();

	void store(InputStream file, String filename);

	Stream<Path> loadAll();

	Resource load(String filename);

	void deleteAll();

	void deleteOne(String filename);

}