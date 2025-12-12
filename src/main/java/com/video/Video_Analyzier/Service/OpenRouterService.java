package com.video.Video_Analyzier.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenRouterService {
    //    private final WebClient openRouterClient;
//
//    public OpenRouterService(WebClient webClient) {
//        this.openRouterClient = webClient.mutate()
//                .baseUrl("https://api.openrouter.ai/v1")
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .build();
//    }
    @Value("${openrouter.api.key}")
    private String apiKey;



    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://openrouter.ai/api/v1")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("HTTP-Referer", "http://localhost")
            .defaultHeader("X-Title", "MyApp")
            .build();

    //    private String apiKey="";
//    // Choose a model that supports video; example: experimental free Gemini
//    private final String MODEL = "";
    private final ObjectMapper objectMapper = new ObjectMapper();


//    public double getVideoDurationSeconds(File file) throws IOException {
//        try (IsoFile isoFile = new IsoFile(file.getAbsolutePath())) {
//            double lengthInSeconds = (double) isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
//                    isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
//            return lengthInSeconds;
//        }
//    }
//
//    public String analyzeVideoWithOpenRouter(File file) throws IOException {
//        byte[] bytes = FileUtils.readFileToByteArray(file);
//        String base64 = Base64.getEncoder().encodeToString(bytes);
//        String dataUrl = "data:video/mp4;base64," + base64;
//
//
//        // Build request body following OpenRouter chat completions with messages array
//        Map<String, Object> videoUrlContent = Map.of(
//                "type", "video_url",
//                "videoUrl", Map.of("url", dataUrl)
//        );
//
//
//        Map<String, Object> promptText = Map.of(
//                "type", "text",
//                "text", "You are a social media video analyst. Provide a brief report about whether this short vertical social media video (30-60s) will perform well. Give an overall score out of 100, list top 3 strengths, top 3 flaws, and 3 actionable suggestions to improve engagement and retention. Keep response concise and structured as JSON with keys: score, strengths, flaws, suggestions, summary."
//        );
//
//
//        Map<String, Object> message = Map.of(
//                "role", "user",
//                "content", new Object[]{promptText, videoUrlContent}
//        );
//
//
//        Map<String, Object> body = Map.of(
//                "model", MODEL,
//                "messages", new Object[]{message},
//                "stream", false
//        );
//
//
//        String resp = openRouterClient.post()
//                .uri("/chat/completions")
//                .header("Authorization", "Bearer " + apiKey)
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//
//        // Parse result: depends on provider. Try to extract text from choices[0].message.content etc.
//        JsonNode root = objectMapper.readTree(resp);
//        // Best-effort extraction — structure may vary across providers
//        try {
//            JsonNode choices = root.path("choices");
//            if (choices.isArray() && choices.size() > 0) {
//                JsonNode first = choices.get(0);
//                JsonNode messageNode = first.path("message");
//                // Some providers return message.content as array or text
//                JsonNode content = messageNode.path("content");
//                if (content.isArray()) {
//                    for (JsonNode item : content) {
//                        if (item.path("type").asText().equals("text")) {
//                            return item.path("text").asText();
//                        }
//                    }
//                } else if (content.isTextual()) {
//                    return content.asText();
//                }
//                // fallback: try 'text'
//                if (first.has("text")) return first.path("text").asText();
//            }
//        } catch (Exception e) {
//        // ignore
//        }
//        // fallback: return raw response
//        return resp;
//    }


    //2nd Attempt

//    public String getResponse(String request) {
//        try {
//            return this.chatModel.call(request);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
//        }
//    }
//
//
//    public String toBase64DataUrl(MultipartFile file) throws IOException {
//        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
//        return "data:" + file.getContentType() + ";base64," + base64;
//    }


    public String analyzeVideo(MultipartFile file)
    {
        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(buildMultipartRequest(file).build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private MultipartBodyBuilder buildMultipartRequest(MultipartFile file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        // SYSTEM MESSAGE
        builder.part("messages[0][role]", "system");
        builder.part("messages[0][content]", "You are a video analysis expert. Rate the video out of 100.");

        // USER MESSAGE WITH VIDEO
        builder.part("messages[1][role]", "user");
        builder.part("messages[1][content][0][type]", "input_video");

        builder.part("messages[1][content][0][video]", file.getResource())
                .filename(file.getOriginalFilename())
                .contentType(MediaType.parseMediaType(file.getContentType()));

        // MODEL NAME
        builder.part("model", "google/gemini-2.0-flash-exp:free");

        return builder;
    }

}
