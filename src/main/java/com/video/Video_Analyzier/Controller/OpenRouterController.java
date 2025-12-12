package com.video.Video_Analyzier.Controller;

import com.drew.lang.annotations.NotNull;
import com.video.Video_Analyzier.Service.OpenRouterService;
import org.apache.commons.io.FileUtils;

import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/video")
@Validated
public class OpenRouterController {


    @Autowired
    private OpenRouterService openRouterService;


//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> uploadVideo(@RequestParam("file") @NotNull MultipartFile file) throws IOException, InterruptedException {
//        // Save temp file
//        File temp = File.createTempFile("upload-", ".mp4");
//        FileUtils.copyInputStreamToFile(file.getInputStream(), temp);
//
//
//        // Validate duration (seconds)
//        double durationSeconds = openRouterService.getVideoDurationSeconds(temp);
//        if (durationSeconds < 1) {
//            temp.delete();
//            return ResponseEntity.badRequest().body("Could not determine video duration or file invalid.");
//        }
//        if (durationSeconds > 60) {
//            temp.delete();
//            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("Video too long. Max allowed is 60 seconds.");
//        }
//
//
//        // Send to OpenRouter and get analysis
//        String analysis = openRouterService.analyzeVideoWithOpenRouter(temp);
//
//
//        // cleanup
//        temp.delete();
//
//
//        return ResponseEntity.ok(analysis);
//    }

//    @PostMapping("/ai")
//    public ResponseEntity<?> callResponse(@RequestParam("file") MultipartFile file) {
//        if (file == null) {
//            return ResponseEntity.badRequest().body("file content cannot be empty.");
//        }
//        try {
//            Resource resource = new InputStreamResource(file.getInputStream()) {
//                @Override
//                public String getFilename() {
//                    return file.getOriginalFilename();
//                }
//            };
//
//            Media media = new Media(MimeTypeUtils.parseMimeType("video/mp4"), resource);
//            UserMessage userMessage = UserMessage.builder()
//                    .text("You are a professional video analyst. Give me a rating out of 100 for this video.")
//                    .media(media)
//                    .build();
////            String base=openRouterService.toBase64DataUrl(file);
////            String prompt=" You are a professional video analyst give me a rating out of 100 for this video"+base;
//
//            String response = openRouterService.getResponse(userMessage);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Sorry, an error occurred while communicating with the AI service.");
//        }
//    }

@PostMapping("/ai")
public ResponseEntity<?> analyzeVideo(@RequestParam("file") MultipartFile file) {
    try {
        String response = openRouterService.analyzeVideo(file);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.status(500).body(e.getMessage());
    }
}


}
