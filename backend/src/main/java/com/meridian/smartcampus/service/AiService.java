package com.meridian.smartcampus.service;

import com.meridian.smartcampus.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final LlmClient llmClient;
    private final CampusContextService campusContextService;
    private final CampusSemanticEngine campusSemanticEngine;

    public AiService(LlmClient llmClient,
                     CampusContextService campusContextService,
                     CampusSemanticEngine campusSemanticEngine) {
        this.llmClient = llmClient;
        this.campusContextService = campusContextService;
        this.campusSemanticEngine = campusSemanticEngine;
    }

    public Map<String, Object> processChat(String userRole, String username, String message, List<Map<String, String>> history, String requestedModel, String requestedProvider) {
        String cleanRole = userRole != null ? userRole.toLowerCase().trim() : "student";
        Student activeStudent = "student".equals(cleanRole) ? campusContextService.findActiveStudent(username) : null;

        // Update provider & model if user requested in payload
        if (requestedProvider != null && !requestedProvider.isEmpty()) {
            llmClient.setProvider(requestedProvider);
        }
        if (requestedModel != null && !requestedModel.isEmpty()) {
            llmClient.setModel(requestedModel);
        }

        String lower = message != null ? message.toLowerCase().trim() : "";

        // 1. Check if the prompt triggers an Autonomous Agent Action or Mathematical Simulation:
        // We handle direct actions and computations deterministically against the MongoDB database.
        boolean isActionOrDeterministic =
                (lower.contains("register") && (lower.contains("event") || lower.contains("symposium") || lower.contains("hackathon") || lower.contains("workshop") || lower.contains("fest")))
                || ((lower.contains("complaint") || lower.contains("ticket") || lower.contains("report")) && (lower.contains("file") || lower.contains("raise") || lower.contains("submit") || lower.contains("issue") || lower.contains("broken") || lower.contains("not working") || lower.contains("problem") || lower.contains("down")))
                || (lower.contains("alert") && (lower.contains("student") || lower.contains("pending") || lower.contains("remind")))
                || (lower.contains("timetable") || lower.contains("schedule") || lower.contains("routine") || lower.contains("period") || lower.contains("class today"))
                || (lower.contains("bunk") || lower.contains("safe to miss") || (lower.contains("how many classes") && lower.contains("skip")))
                || (lower.contains("simulate") && (lower.contains("gpa") || lower.contains("cgpa") || lower.contains("score")))
                || (lower.contains("mentor") || lower.contains("advisor") || lower.contains("counselor"));

        if (isActionOrDeterministic) {
            log.info("Executing Autonomous Campus Action / Deterministic reasoning for user: {}", username);
            return campusSemanticEngine.generateGroundedResponse(cleanRole, username, message, activeStudent);
        }

        // 2. If an external LLM (Gemini / OpenAI) is configured, use it with real-time Grounding Context
        if (llmClient.isConfigured()) {
            try {
                log.info("Processing chat query with LLM provider: {} (model: {})", llmClient.getProvider(), llmClient.getModel());
                String campusContext = campusContextService.buildGroundingContext(cleanRole, username, message);

                String systemPrompt = """
                        You are the Meridian Smart Campus AI Assistant, an authoritative, helpful, and friendly campus advisor for students, faculty, and administrators.
                        You have real-time access to the live university MongoDB database context provided below.

                        RULES:
                        1. Always ground your responses in the actual provided database records (names, scores, attendance %, deadlines, events).
                        2. Never hallucinate fake courses or conflicting information. If information is not in the database, clearly explain that and direct the user to the appropriate department.
                        3. Use clean Markdown formatting: headers, bold text, bullet points, and tables where suitable.
                        4. When answering attendance questions, refer to the 75% minimum university requirement.
                        5. At the end of your response, provide 3 suggested follow-up questions formatted as:
                        SUGGESTIONS: [Question 1] | [Question 2] | [Question 3]

                        CURRENT LIVE CAMPUS DATABASE CONTEXT:
                        """ + campusContext;

                String llmAnswer = llmClient.generate(systemPrompt, history, message);

                // Extract suggestions if present
                List<String> suggestions = new ArrayList<>();
                String finalAnswer = llmAnswer;
                if (llmAnswer.contains("SUGGESTIONS:")) {
                    int idx = llmAnswer.indexOf("SUGGESTIONS:");
                    finalAnswer = llmAnswer.substring(0, idx).trim();
                    String suggStr = llmAnswer.substring(idx + "SUGGESTIONS:".length()).trim();
                    String[] parts = suggStr.split("\\|");
                    for (String p : parts) {
                        String clean = p.replaceAll("[\\[\\]]", "").trim();
                        if (!clean.isEmpty()) suggestions.add(clean);
                    }
                }

                if (suggestions.isEmpty()) {
                    suggestions.add("How is my attendance trending?");
                    suggestions.add("What assignments are due?");
                    suggestions.add("Breakdown of my SGPA and marks");
                }

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("answer", finalAnswer);
                resp.put("source", "llm");
                resp.put("provider", llmClient.getProvider());
                resp.put("model", llmClient.getModel());
                resp.put("suggestions", suggestions);
                return resp;

            } catch (Exception e) {
                log.warn("LLM generation failed, falling back to Campus Semantic Engine: {}", e.getMessage());
            }
        }

        // 3. Fallback: Intelligent Grounded Campus Engine v3.0
        log.info("Processing chat query via Meridian Grounded Campus Engine v3.0.");
        return campusSemanticEngine.generateGroundedResponse(cleanRole, username, message, activeStudent);
    }
}
