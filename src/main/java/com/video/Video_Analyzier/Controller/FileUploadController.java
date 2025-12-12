package com.video.Video_Analyzier.Controller;

import com.video.Video_Analyzier.Service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/google")
@RequiredArgsConstructor
public class FileUploadController
{
    private final FileUploadService fileUploadService;

    @PostMapping("/last")
    public ResponseEntity<String> analyzeVideo(@RequestParam("file") MultipartFile file) // here is the video file not path
    {
        Path tempfile=null;
        try{
            tempfile= File.createTempFile("gemini-upload-",".mp4").toPath(); // here we create a temp file and get its path

            file.transferTo(tempfile.toFile());  // merging that video file to this temp file
            String question="You are expert in video analysis you have to give me a rating of this video from 0-100"; // random question

            String fileUri = fileUploadService.uploadVideoToGemini(tempfile.toString()); // upload that temp file to google so that gemini can read the video from it
            String answer=fileUploadService.askAboutVideo(fileUri,question);
            return ResponseEntity.ok(answer);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }finally {
            if(tempfile!=null){
                try{
                    Files.deleteIfExists(tempfile);
                    System.out.println("Temp file has been deleted"+tempfile);
                }catch (Exception e)
                {
                    System.out.println("Failed to deletet temp file"+e.getMessage());
                }
            }
        }
    }
}
