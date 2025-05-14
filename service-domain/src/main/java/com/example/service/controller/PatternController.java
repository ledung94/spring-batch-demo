package com.example.service.controller;

import com.example.service.service.PatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("pattern/v1.0/")
public class PatternController {
    private final PatternService patternService;

    @PostMapping("/singleton")
    public String singleton() {
        return patternService.executeSingleton();
    }

    @PostMapping("/observer")
    public void observer() {
        patternService.executeObserver();
    }

    @PostMapping("/transform")
    public String transformToUpperCase(@RequestBody String input) {
        return patternService.transform(input);
    }
}
