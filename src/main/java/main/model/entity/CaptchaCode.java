package main.model.entity;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@Table(name = "captcha_codes")
public class CaptchaCode {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    private Date time;

    @NotNull
    @Column(columnDefinition="TEXT")
    @Type(type = "text")
    private String code;

    @NotNull
    @Column(name = "secret_code", columnDefinition="TEXT")
    @Type(type = "text")
    private String secretCode;

    public CaptchaCode() {
    }

    public CaptchaCode(@NotNull String code, @NotNull String secretCode) {
        this.code = code;
        this.secretCode = secretCode;
    }
}
