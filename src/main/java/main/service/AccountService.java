package main.service;

import com.mortennobel.imagescaling.ResampleOp;
import main.api.request.ProfileImageRequest;
import main.api.request.ProfileRequest;
import main.api.response.ResultResponse;
import main.model.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class AccountService {

    private final UserService userService;
    private final ImageService imageService;

    public AccountService(UserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    public ResponseEntity<ResultResponse> editUserProfileResponse(@RequestBody ProfileRequest profileRequest) {
        User currentUser = userService.getCurrentUser();
        Map<String, String> errors = userService.checkUserData(profileRequest, currentUser);
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
        }

        currentUser.setName(profileRequest.getName());
        currentUser.setEmail(profileRequest.getEmail());

        if (profileRequest.getPassword() != null){
            currentUser.setPassword(userService.encodePassword(profileRequest.getPassword()));
        }
        if (profileRequest.getRemovePhoto() == 1){
            currentUser.setPhoto("");
        }

        userService.saveUser(currentUser);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    public ResponseEntity<ResultResponse> editUserProfileWithImage(@ModelAttribute ProfileImageRequest profileRequest) {

        User currentUser = userService.getCurrentUser();
        Map<String, String> errors = userService.checkUserData(profileRequest, currentUser);
        Map<String, String> errorsImage = imageService.checkFile(profileRequest.getPhoto());
        errors.putAll(errorsImage);
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
        }

        String filename = null;
        ResampleOp resamOp = new ResampleOp(36,36);
        try {
            BufferedImage bufferedImage = ImageIO.read(profileRequest.getPhoto().getInputStream());
            BufferedImage modifiedImage = resamOp.filter(bufferedImage, null);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(modifiedImage, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());

            filename = imageService.storeFile(is, profileRequest.getPhoto().getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (filename == null){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        currentUser.setPhoto(filename);
        currentUser.setName(profileRequest.getName());
        currentUser.setEmail(profileRequest.getEmail());

        if (profileRequest.getPassword() != null){
            currentUser.setPassword(userService.encodePassword(profileRequest.getPassword()));
        }

        userService.saveUser(currentUser);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }




}
