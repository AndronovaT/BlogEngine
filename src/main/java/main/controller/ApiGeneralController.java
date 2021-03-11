package main.controller;

import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.api.response.tags.AllTagsResponse;
import main.service.SettingsService;
import main.service.TagToPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

    private final InitResponse initResponse;
    private final SettingsService settingsService;
    private final TagToPostService tagToPostService;

    public ApiGeneralController(InitResponse initResponse, SettingsService settingsService, TagToPostService tagToPostService) {
        this.initResponse = initResponse;
        this.settingsService = settingsService;
        this.tagToPostService = tagToPostService;
    }

    @GetMapping("/init")
    private ResponseEntity<InitResponse> init(){
        return new ResponseEntity<>(initResponse, HttpStatus.OK);
    }

    @GetMapping("/settings")
    private ResponseEntity<SettingsResponse> settings(){
        return new ResponseEntity<>(settingsService.getGlobalSettings(), HttpStatus.OK);
    }

    @GetMapping("/tag")
    private ResponseEntity<AllTagsResponse> tags(@RequestParam(required = false, defaultValue = "", name = "query") String query) {
        return new ResponseEntity(tagToPostService.getAllTag(query), HttpStatus.OK);
    }
}
