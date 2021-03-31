package main.service;

import main.api.response.SettingsResponse;
import main.model.entity.GlobalSetting;
import main.model.enums.YesNo;
import main.repository.GlobalSettingsRepository;
import org.springframework.stereotype.Service;

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
}
