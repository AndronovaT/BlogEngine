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
    @Column(length = 65535, columnDefinition="TEXT")
    @Type(type = "text")
    private String code;
    @NotNull
    @Column(name = "secret_code", length = 65535, columnDefinition="TEXT")
    @Type(type = "text")
    private String secretCode;
}
