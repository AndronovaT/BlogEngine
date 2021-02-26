package main.model;

public enum BlogSetting {
    MULTIUSER_MODE("Многопользовательский режим"),
    POST_PREMODERATION("Премодерация постов"),
    STATISTICS_IS_PUBLIC("Показывать всем статистику блога")
    ;
    private String code;

    BlogSetting(String code) {
    }

    public String getCode() {
        return code;
    }

    public static BlogSetting fromCode(String code) {
        for (BlogSetting blogSetting :BlogSetting.values()){
            if (blogSetting.getCode().equals(code)){
                return blogSetting;
            }
        }
        throw new UnsupportedOperationException(
                "The code " + code + " is not supported!");
    }
}
