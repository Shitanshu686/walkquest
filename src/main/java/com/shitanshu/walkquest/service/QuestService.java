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

                CATEGORY:
                Choose exactly ONE category:
                NATURE, WALK, MINDFULNESS, EXPLORATION, GARDENING, OBSERVATION.

                DIFFICULTY:
                Choose exactly ONE difficulty:
                EASY or MODERATE.

                CATEGORY MEANING:
                NATURE = nature-focused observation or interaction.
                WALK = walking-focused activity.
                MINDFULNESS = relaxing, breathing or quiet outdoor activity.
                EXPLORATION = discovering or noticing things outdoors.
                GARDENING = safe gardening-related activity without picking or damaging plants.
                OBSERVATION = observing sounds, colors, birds, clouds or surroundings.

                DIFFICULTY RULE:
                EASY = gentle walking, sitting, observing or relaxing.
                MODERATE = slightly more active but still safe outdoor activities.

                LANGUAGE:
                Match the user's language style.

                If USER is Hindi/Hinglish:
                - Write in natural SIMPLE ROMAN HINDI/HINGLISH.
                - Use everyday conversational words that a normal Indian
                  speaker would actually use.
                - Mixing common English words is allowed.
                - Do NOT translate Hindi word-by-word.
                - Do NOT invent Hindi words.
                - Do NOT create unnatural phrases.
                - Do NOT use poetic or literary language.

                GOOD HINGLISH PHRASES:
                "walk karo"
                "thodi der baitho"
                "aas-paas dekho"
                "fresh air lo"
                "birds ki awaaz suno"
                "surroundings notice karo"
                "slowly walk karo"
                "3 interesting cheezein notice karo"

                BAD PHRASES:
                "aage se step karo"
                "khaas-khaas dekho"
                "aage ko saaf karo"
                "khainch dinriye"
                "chirping their hearts out"

                IMPORTANT LANGUAGE RULE:
                Every activity must sound like a real person giving a simple
                outdoor instruction to a friend.

                Example USER:
                "Mere paas 30 minute hain, mood fresh karna hai"

                GOOD:
                "10 minute halka walk karo aur aas-paas ki 3 interesting
                cheezein notice karo."

                GOOD:
                "Kisi safe outdoor jagah par 5 minute baitho aur fresh air lo."

                GOOD:
                "Thoda aur walk karo aur birds ya surroundings ki awaaz suno."

                BAD:
                "Garden me khaas-khaas dekho."

                BAD:
                "Path se walk karo, aage ko saaf karo."

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
                  "category": "WALK",
                  "difficulty": "EASY",
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

            int requestedDuration = extractDuration(userRequest);

            if (requestedDuration != 20 || !userRequest.isBlank()) {
                duration = requestedDuration;
            }

            String category = node.path("category")
                    .asText("NATURE")
                    .toUpperCase();

            String requestedCategory =
                    detectRequestedCategory(userRequest);

            if (requestedCategory != null) {
                category = requestedCategory;
            }

            String difficulty = node.path("difficulty")
                    .asText("EASY")
                    .toUpperCase();

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

            for (String activity : activities) {
                if (!isActivityValid(activity)
                        || (isEnglishRequest(userRequest) && !isEnglishActivity(activity))) {
                    throw new RuntimeException(
                            "Gemma returned a low-quality activity: " + activity
                    );
                }
            }

            return new QuestResponse(
                    title,
                    duration,
                    category,
                    difficulty,
                    activities
            );

        } catch (Exception e) {

            System.out.println("===== GEMMA ERROR =====");
            System.out.println(e.getMessage());
            System.out.println("======================");

            return fallbackQuest(userRequest);
        }
    }

    private boolean isActivityValid(String activity) {
        String text = activity.trim().toLowerCase();

        if (text.length() < 15 || text.split("\\s+").length < 4) {
            return false;
        }

        String[] suspiciousPhrases = {
                "aage se step karo",
                "khaas-khaas dekho",
                "aage ko saaf karo",
                "khainch dinriye",
                "chirping their hearts out"
        };

        for (String phrase : suspiciousPhrases) {
            if (text.contains(phrase)) {
                return false;
            }
        }

        return true;
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

    private boolean isEnglishActivity(String activity) {
        String[] words = activity.toLowerCase().split("[^a-z]+");

        String[] hindiMarkers = {
                "karo", "hai", "hoon", "hain", "mein",
                "baithkar", "shaant", "awaaz", "karna",
                "paudha", "paudhe", "lo", "ko"
        };

        for (String word : words) {
            for (String marker : hindiMarkers) {
                if (word.equals(marker)) {
                    return false;
                }
            }
        }

        return true;
    }

    private QuestResponse fallbackQuest(String userRequest) {

        int duration = extractDuration(userRequest);

        String category = detectRequestedCategory(userRequest);

        if (category == null) {
            category = "NATURE";
        }

        if (isEnglishRequest(userRequest)) {
            return switch (category) {
                case "GARDENING" -> new QuestResponse(
                        "Garden Reset", duration, "GARDENING", "EASY",
                        List.of(
                                "Observe three different leaf shapes in the garden.",
                                "Water garden plants gently if it is safe and permitted.",
                                "Sit in the garden for five minutes and enjoy the fresh air."
                        )
                );
                case "WALK" -> new QuestResponse(
                        "Fresh Air Walk", duration, "WALK", "EASY",
                        List.of(
                                "Walk at a comfortable pace for a few minutes.",
                                "Notice three interesting things around you as you walk.",
                                "Pause in a safe outdoor spot and enjoy the fresh air."
                        )
                );
                case "MINDFULNESS" -> new QuestResponse(
                        "Outdoor Reset", duration, "MINDFULNESS", "EASY",
                        List.of(
                                "Walk slowly for a few minutes and breathe comfortably.",
                                "Sit somewhere safe and listen to the sounds around you.",
                                "Spend five quiet minutes enjoying the fresh air."
                        )
                );
                case "OBSERVATION" -> new QuestResponse(
                        "Notice More", duration, "OBSERVATION", "EASY",
                        List.of(
                                "Notice three interesting details in your surroundings.",
                                "Listen carefully and identify two different natural sounds.",
                                "Sit somewhere safe and observe your surroundings for five minutes."
                        )
                );
                case "EXPLORATION" -> new QuestResponse(
                        "Outdoor Explore", duration, "EXPLORATION", "EASY",
                        List.of(
                                "Explore a familiar outdoor area at a comfortable pace.",
                                "Look for three interesting details you have not noticed before.",
                                "Pause at a safe spot and take in the surroundings."
                        )
                );
                default -> new QuestResponse(
                        "Fresh Air Quest", duration, "NATURE", "EASY",
                        List.of(
                                "Take a gentle walk and notice the nature around you.",
                                "Find three interesting things in your surroundings.",
                                "Sit somewhere safe and enjoy the fresh air for five minutes."
                        )
                );
            };
        }

        return switch (category) {

            case "GARDENING" -> new QuestResponse(
                    "Garden Reset",
                    duration,
                    "GARDENING",
                    "EASY",
                    List.of(
                            "5 minute plants ko observe karo aur different leaf shapes notice karo.",
                            "Safe ho to garden ke plants ko thoda paani do.",
                            "5 minute garden mein baithkar fresh air lo."
                    )
            );

            case "WALK" -> new QuestResponse(
                    "Fresh Air Walk",
                    duration,
                    "WALK",
                    "EASY",
                    List.of(
                            "10 minute comfortable pace par walk karo.",
                            "Walk karte waqt aas-paas ki 3 interesting cheezein notice karo.",
                            "5 minute kisi safe outdoor jagah par baithkar fresh air lo."
                    )
            );

            case "MINDFULNESS" -> new QuestResponse(
                    "Outdoor Reset",
                    duration,
                    "MINDFULNESS",
                    "EASY",
                    List.of(
                            "5 minute slowly walk karo aur deep breaths lo.",
                            "Kisi safe outdoor spot par baithkar surroundings ki sounds suno.",
                            "5 minute shaant hokar fresh air enjoy karo."
                    )
            );

            case "OBSERVATION" -> new QuestResponse(
                    "Notice More",
                    duration,
                    "OBSERVATION",
                    "EASY",
                    List.of(
                            "Aas-paas ki 3 interesting cheezein observe karo.",
                            "2 different natural sounds identify karo.",
                            "Kisi safe jagah par baithkar 5 minute surroundings dekho."
                    )
            );

            case "EXPLORATION" -> new QuestResponse(
                    "Outdoor Explore",
                    duration,
                    "EXPLORATION",
                    "EASY",
                    List.of(
                            "Safe outdoor area mein slowly walk karo.",
                            "Raste mein 3 interesting details notice karo.",
                            "5 minute kisi safe spot par rukkar surroundings explore karo."
                    )
            );

            default -> new QuestResponse(
                    "Fresh Air Quest",
                    duration,
                    "NATURE",
                    "EASY",
                    List.of(
                            "10 minute halka walk karo aur aas-paas ki cheezein notice karo.",
                            "Kisi safe outdoor spot par 3 interesting sounds identify karo.",
                            "5 minute shaant jagah par baithkar fresh air enjoy karo."
                    )
            );
        };
    }

    private String detectRequestedCategory(String request) {

        String lower = request.toLowerCase();

        if (lower.contains("garden")
                || lower.contains("gardening")
                || lower.contains("plant")
                || lower.contains("plants")
                || lower.contains("paudha")
                || lower.contains("paudhe")) {

            return "GARDENING";
        }

        if (lower.contains("walk")
                || lower.contains("walking")
                || lower.contains("stroll")
                || lower.contains("chalna")
                || lower.contains("walk kar")) {

            return "WALK";
        }

        if (lower.contains("stress")
                || lower.contains("stressed")
                || lower.contains("relax")
                || lower.contains("relaxing")
                || lower.contains("calm")
                || lower.contains("peace")
                || lower.contains("mind")
                || lower.contains("fresh mind")
                || lower.contains("clear my head")
                || lower.contains("clear my mind")) {

            return "MINDFULNESS";
        }

        if (lower.contains("bird")
                || lower.contains("birds")
                || lower.contains("sound")
                || lower.contains("sounds")
                || lower.contains("awaaz")
                || lower.contains("observe")
                || lower.contains("observation")) {

            return "OBSERVATION";
        }

        if (lower.contains("explore")
                || lower.contains("exploring")
                || lower.contains("discover")
                || lower.contains("exploration")
                || lower.contains("adventure")) {

            return "EXPLORATION";
        }

        if (lower.contains("nature")
                || lower.contains("nature walk")
                || lower.contains("outdoors")
                || lower.contains("outdoor")) {

            return "NATURE";
        }

        return null;
    }

    private boolean isEnglishRequest(String request) {
        String lower = request.toLowerCase();

        String[] hindiHints = {
                "mujhe", "mere", "hoon", "hai", "hain", "karna",
                "karo", "bahar", "thoda", "thodi", "paas", "mein",
                "minute hain", "fresh karna", "pasand hai"
        };

        for (String hint : hindiHints) {
            if (lower.contains(hint)) {
                return false;
            }
        }

        return true;
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
