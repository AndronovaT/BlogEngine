package main.service;


import main.api.response.authorization.CaptchaResponse;
import main.model.entity.CaptchaCode;
import main.repository.CaptchaCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;


@Service
public class CaptchaCodeService {

    private final CaptchaCodeRepository captchaCodeRepository;

    private final static int CAPTCHA_LENGTH = 5;
    private final static int SECRET_LENGTH = 22;
    private final static int IMAGE_WIDTH = 100;
    private final static int IMAGE_HEIGHT = 35;
    private final static int TEXT_SIZE = 16;
    private final static String FONT_FAMILY_NAME = "Verdana";
    private final static String HEADING = "data:image/png;base64, ";

    @Value("${blog.minuteLifeCaptcha}")
    private String minuteLifeCaptchaStr;

    public CaptchaCodeService(CaptchaCodeRepository captchaCodeRepository) {
        this.captchaCodeRepository = captchaCodeRepository;
    }

    public void deleteOldCaptcha(){
        int minuteLifeCaptcha = Integer.parseInt(minuteLifeCaptchaStr);
        List<CaptchaCode> oldCaptcha = captchaCodeRepository.findOldCaptcha(minuteLifeCaptcha);
        oldCaptcha.forEach(captchaCodeRepository::delete);
    }

    public List<CaptchaCode> findCaptchaBySecret(String secret){
        return captchaCodeRepository.findBySecret(secret);
    }

    public ResponseEntity<CaptchaResponse> createResponseCaptcha() {

        deleteOldCaptcha();

        String word = generateCaptchaText(CAPTCHA_LENGTH);
        BufferedImage captchaImage = createImage(word);
        String secret = generateCaptchaText(SECRET_LENGTH);
        try {
            byte[] byteArray = toByteArray(captchaImage, "png");
            String encodedString = Base64.getEncoder().encodeToString(byteArray);

            CaptchaCode captchaCode = new CaptchaCode(word, secret);
            captchaCode.setTime(new Date());
            captchaCodeRepository.save(captchaCode);

            return new ResponseEntity<>(new CaptchaResponse(secret, HEADING + encodedString), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static String generateCaptchaText(int captchaLength) {
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        StringBuffer captchaStrBuffer = new StringBuffer();
        java.util.Random rnd = new java.util.Random();

        while (captchaStrBuffer.length() < captchaLength) {
            int index = (int) (rnd.nextFloat() * saltChars.length());
            captchaStrBuffer.append(saltChars.substring(index, index + 1));
        }

        return captchaStrBuffer.toString();
    }

    private static BufferedImage createImage(String word) {
        BufferedImage bImg;

        bImg = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT,
                BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2 = bImg.createGraphics();

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        Font font = new Font(FONT_FAMILY_NAME, Font.BOLD, TEXT_SIZE);
        g2.setFont(font);
        g2.setColor(Color.WHITE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        char[] chars = word.toCharArray();
        int x = 10;
        int y = IMAGE_HEIGHT / 2 + TEXT_SIZE / 2;

        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            g2.drawString(String.valueOf(ch), x + font.getSize() * i, y
                    + (int) Math.pow(-1, i) * (TEXT_SIZE / 6));
        }

        g2.dispose();

        return bImg;
    }

    private static byte[] toByteArray(BufferedImage image, String type) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()){
            ImageIO.write(image, type, out);
            return out.toByteArray();
        }
    }


}
