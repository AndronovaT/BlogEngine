package main.model.converterEnum;

import main.model.enums.BlogSetting;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BlogSettingConverter  implements AttributeConverter<BlogSetting, String> {

    public String convertToDatabaseColumn(BlogSetting value) {
        if ( value == null ) {
            return null;
        }
        return value.getCode();
    }

    public BlogSetting convertToEntityAttribute(String value) {
        if ( value == null ) {
            return null;
        }
        return BlogSetting.fromCode(value);
    }
}
