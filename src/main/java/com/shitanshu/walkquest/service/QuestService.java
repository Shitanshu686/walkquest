package com.shitanshu.walkquest.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.shitanshu.walkquest.dto.QuestResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private static final String OLLAMA_URL = "http://localhost:11434";
    private static final String MODEL = "gemma3:1b";

    public QuestService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl(OLLAMA_URL)
                .build();
    }

    public QuestResponse generateQuest(String userRequest) {

        String prompt = """
                You are WalkQuest AI.

                USER:
                %s

                Create exactly 3 simple outdoor activities.

                LANGUAGE:
                If USER is Hindi/Hinglish, write in SIMPLE ROMAN HINDI/HINGLISH.
                Do NOT translate into English.
                Do NOT invent Hindi words.
                Use normal everyday words such as:
                "walk karo", "baitho", "aas-paas dekho",
                "fresh air lo", "birds ki awaaz suno".

                Example:
                User: Mere paas 30 minute hain, mood fresh karna hai

                Good:
                "10 minute halka walk karo aur aas-paas ki 3 interesting
                cheezein notice karo."

                Good:
                "Kisi safe outdoor jagah par 5 minute baitho aur fresh air lo."

                Good:
                "Thoda aur walk karo aur birds ya surroundings ki awaaz suno."

                Bad:
                "Tumhi aani achhia aage se khainch dinriye."

                Bad:
                "chirping their hearts out"

                IMPORTANT:
                - Use simple conversational words.
                - No poetry.
                - No complicated Hindi.
                - No strange or invented words.
                - Keep each activity one short sentence.
                - Make activities realistic.
                - Exactly 3 activities.
                - If user gives a duration, use that duration.
                - Safe activities only.
                - Never pick or damage plants.
                - No climbing, fire, dangerous traffic, unsafe swimming,
                  private areas, or restricted areas.
                - Return ONLY valid JSON.

                JSON:
                {
                  "title": "short simple title",
                  "duration": 30,
                  "activities": [
                    "activity 1",
                    "activity 2",
                    "activity 3"
                  ]
                }
                """.formatted(userRequest);

        try {

            OllamaRequest request = new OllamaRequest(
                    MODEL,
                    prompt,
                    "json",
                    false
            );

            OllamaResponse response = restClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaResponse.class);

            if (response == null || response.response() == null) {
                throw new RuntimeException("Empty response from Ollama");
            }

            System.out.println("===== GEMMA RAW RESPONSE =====");
            System.out.println(response.response());
            System.out.println("==============================");

            String json = cleanJson(response.response());

            JsonNode node = objectMapper.readTree(json);

            String title = node.path("title")
                    .asText("Outdoor Quest");

            int duration = node.path("duration")
                    .asInt(20);

            List<String> activities = new ArrayList<>();

            JsonNode activityNode = node.path("activities");

            if (activityNode.isArray()) {

                for (JsonNode activity : activityNode) {

                    String activityText =
                            activity.asText().trim();

                    if (!activityText.isEmpty()) {
                        activities.add(activityText);
                    }
                }
            }

            if (activities.size() != 3) {
                throw new RuntimeException(
                        "Gemma returned " +
                        activities.size() +
                        " activities instead of exactly 3"
                );
            }

            return new QuestResponse(
                    title,
                    duration,
                    activities
            );

        } catch (Exception e) {

            System.out.println("===== GEMMA ERROR =====");
            System.out.println(e.getMessage());
            System.out.println("======================");

            return fallbackQuest(userRequest);
        }
    }

    private String cleanJson(String response) {

        String cleaned = response.trim();

        if (cleaned.startsWith("```")) {

            cleaned = cleaned
                    .replaceFirst("^```json\\s*", "")
                    .replaceFirst("^```\\s*", "");

            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(
                        0,
                        cleaned.length() - 3
                );
            }
        }

        return cleaned.trim();
    }

    private QuestResponse fallbackQuest(String userRequest) {

        int duration = extractDuration(userRequest);

        List<String> activities = List.of(
                "10 minute halka walk karo aur aas-paas ki cheezein notice karo.",
                "Kisi safe outdoor spot par 3 interesting sounds identify karo.",
                "5 minute shaant jagah par baithkar fresh air enjoy karo."
        );

        return new QuestResponse(
                "Fresh Air Quest",
                duration,
                activities
        );
    }

    private int extractDuration(String request) {

        String lower = request.toLowerCase();

        String[] words = lower.split("\\s+");

        for (int i = 0; i < words.length - 1; i++) {

            if (words[i].matches("\\d+")) {

                try {

                    int value = Integer.parseInt(words[i]);

                    String next = words[i + 1];

                    if (next.contains("minute")
                            || next.contains("min")) {

                        return value;
                    }

                } catch (NumberFormatException ignored) {
                }
            }
        }

        return 20;
    }

    private record OllamaRequest(
            String model,
            String prompt,
            String format,
            boolean stream
    ) {
    }

    private record OllamaResponse(
            String response
    ) {
    }
}
