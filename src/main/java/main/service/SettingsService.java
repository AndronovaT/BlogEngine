package main.service;

import main.api.response.SettingsResponse;
import main.model.entity.GlobalSetting;
import main.model.enums.BlogSetting;
import main.model.enums.YesNo;
import main.repository.GlobalSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SettingsService {

    private final GlobalSettingsRepository globalSettingsRepository;

    public SettingsService(GlobalSettingsRepository globalSettingsRepository) {
        this.globalSettingsRepository = globalSettingsRepository;
    }

    public SettingsResponse getGlobalSettings() {
        Iterable<GlobalSetting> globalSettingsIterable = globalSettingsRepository.findAll();

        SettingsResponse settingsResponse = new SettingsResponse();
        globalSettingsIterable.forEach(globalSettings -> {
            boolean value = globalSettings.getValue().equals(YesNo.YES);
            switch (globalSettings.getName()){
                case MULTIUSER_MODE:
                    settingsResponse.setMultiuserMode(value);
                    break;
                case POST_PREMODERATION:
                    settingsResponse.setPostPremoderation(value);
                    break;
                case STATISTICS_IS_PUBLIC:
                    settingsResponse.setStaticIsPublic(value);
            }
        });
        return settingsResponse;
    }

    public void setGlobalSettings(BlogSetting blogSetting, boolean value){
        Iterable<GlobalSetting> globalSettingsIterable = globalSettingsRepository.findAll();
        GlobalSetting globalSetting = null;

        for (GlobalSetting currentGlobalSettings: globalSettingsIterable) {
            if (currentGlobalSettings.getCode().equals(blogSetting)){
                globalSetting = currentGlobalSettings;
                break;
            }
        }

        if (globalSetting == null){
            return;
        }

        globalSetting.setCode(blogSetting);
        globalSetting.setName(blogSetting);
        if (value) {
            globalSetting.setValue(YesNo.YES);
        } else {
            globalSetting.setValue(YesNo.NO);
        }
        save(globalSetting);
    }

    public GlobalSetting save(GlobalSetting globalSetting){
        return globalSettingsRepository.save(globalSetting);
    }

    public void editGlobalSettings(@RequestBody SettingsResponse settingsResponse) {
        setGlobalSettings(BlogSetting.MULTIUSER_MODE, settingsResponse.isMultiuserMode());
        setGlobalSettings(BlogSetting.POST_PREMODERATION, settingsResponse.isPostPremoderation());
        setGlobalSettings(BlogSetting.STATISTICS_IS_PUBLIC, settingsResponse.isStaticIsPublic());
    }
}
