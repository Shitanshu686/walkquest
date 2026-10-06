package com.shitanshu.walkquest.controller;

import com.shitanshu.walkquest.dto.QuestRequest;
import com.shitanshu.walkquest.dto.QuestResponse;
import com.shitanshu.walkquest.service.QuestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quest")
@CrossOrigin
public class QuestController {

    private final QuestService questService;

    public QuestController(QuestService questService) {
        this.questService = questService;
    }

    @PostMapping
    public ResponseEntity<QuestResponse> generateQuest(
            @RequestBody QuestRequest request
    ) {

        if (request == null
                || request.getRequest() == null
                || request.getRequest().isBlank()) {

            return ResponseEntity.badRequest().build();
        }

        QuestResponse response =
                questService.generateQuest(request.getRequest());

        return ResponseEntity.ok(response);
    }
}
