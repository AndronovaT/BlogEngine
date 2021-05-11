package main.api.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileImageRequest {
    private MultipartFile photo;
    private String email;
    private String password;
    private String name;
    private byte removePhoto;
}
