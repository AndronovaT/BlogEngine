package main.api.request;

import lombok.Data;

@Data
public class ProfileRequest {
    private String email;
    private String password;
    private String name;
    private byte removePhoto = 0;
}
