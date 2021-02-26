package main.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "global_settings")
public class GlobalSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @NotNull
    @Enumerated(EnumType.STRING)
    private BlogSetting code;
    @NotNull
    @Convert(converter = BlogSettingConverter.class)
    private BlogSetting name;
    @NotNull
    @Enumerated(EnumType.STRING)
    private YesNo value;
}
