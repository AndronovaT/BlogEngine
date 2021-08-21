package main.service;

import main.api.response.ResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageService {

    private static final long MAX_IMAGE = 5;
    private static final String SEPARATOR = File.separator;

    @Autowired
    private HttpServletRequest request;

    public ResponseEntity<ResultResponse> uploadImageResponse(@RequestPart("image") MultipartFile image) {

        Map<String, String> errors = checkFile(image);
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        String filename = null;
        try {
            filename = storeFile(image.getInputStream(), image.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (filename == null){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(filename, HttpStatus.OK);
    }

    public String storeFile(InputStream image, String originalFilename ) {

        String generatedName = newFileName();
        String uploadDir = SEPARATOR + "upload" + SEPARATOR + generatedName.substring(0,2) + SEPARATOR + generatedName.substring(2,4);
        String fileName = SEPARATOR + generatedName.substring(5) + "_"  + originalFilename;

        String realPath = request.getServletContext().getRealPath(uploadDir);
        Path fileStorageLocation = Paths.get(realPath).toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStorageLocation);
            Path targetLocation = Paths.get(fileStorageLocation + fileName);
            Files.copy(image, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return uploadDir + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String newFileName() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public Map<String, String> checkFile(MultipartFile image) {
        Map<String, String> errors = new HashMap<>();

        String contentType = image.getContentType();
        if (!contentType.equals("image/png") && !contentType.equals("image/jpeg")) {
            errors.put("image", "Формат файла не jpg или png");
        }

        long bytes = image.getSize();

        if ((bytes / 1000000) > MAX_IMAGE){
            errors.put("image", "Размер файла превышает допустимый размер");
        }
        return errors;
    }

}
