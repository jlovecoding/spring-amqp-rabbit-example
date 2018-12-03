package com.example.producer.controller;

import com.example.producer.model.MatchResult;
import com.example.producer.service.MatchResultService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/matchResult")
public class ProducerController {

    private MatchResultService matchResultService;

    public ProducerController(final MatchResultService matchResultService) {
        this.matchResultService = matchResultService;
    }

    @PostMapping
    public MatchResult createMatchResult(@RequestBody final MatchResult matchResult) {
        return matchResultService.createMatchResult(matchResult);
    }

}
