package com.video.Video_Analyzier.Service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileUploadService
{
    @Value("${gemini.api.key}")
    private String apiKey;
    private final HttpClient httpClient= HttpClient.newHttpClient();


    public String uploadVideoToGemini(String filePath) throws IOException, InterruptedException { // function to upload video to google File IO
        Path path= Path.of(filePath);
        long numBytes= Files.size(path);
        String mimeType= "video/mp4";

        // A. Start Resumable Upload Session
        HttpRequest initialRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/upload/v1beta/files?key=" + apiKey))
                .header("X-Goog-Upload-Protocol", "resumable")
                .header("X-Goog-Upload-Command", "start")
                .header("X-Goog-Upload-Header-Content-Length", String.valueOf(numBytes))
                .header("X-Goog-Upload-Header-Content-Type", mimeType)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"file\": {\"display_name\": \"My Video\"}}"))
                .build();

        HttpResponse<String> initialResponse = httpClient.send(initialRequest,HttpResponse.BodyHandlers.ofString());

        // Get the upload URL from headers
        String uploadUrl = initialResponse.headers().firstValue("x-goog-upload-url")
                .orElseThrow(() -> new RuntimeException("Failed to get upload URL"));


        // B. Upload the Actual Bytes
        HttpRequest uploadRequest = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
//                .header("Content-Length", String.valueOf(numBytes))
                .header("X-Goog-Upload-Offset", "0")
                .header("X-Goog-Upload-Command", "upload, finalize")
                .PUT(HttpRequest.BodyPublishers.ofFile(path))
                .build();

        HttpResponse<String> uploadResponse = httpClient.send(uploadRequest,HttpResponse.BodyHandlers.ofString());

        // Parse JSON to get the file URI
        JSONObject json = new JSONObject(uploadResponse.body());
        String fileUri = json.getJSONObject("file").getString("uri");
        System.out.println("file upload url: "+fileUri);
        waitForVideoProcessing(json.getJSONObject("file").getString("name"));
        return fileUri;
    }


    // Helper to wait until video is ready
    private void waitForVideoProcessing(String filename) throws InterruptedException, IOException {
        String state ="PROCESSING";
        while (state.equals("PROCESSING"))
        {
            System.out.println("Waiting for video processing...");
            Thread.sleep(2000);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/" + filename + "?key=" + apiKey))
                    .GET()
                    .build();

            HttpResponse<String> response =httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            state=json.getString("state");

            if(state.equals("FAILED")) throw new RuntimeException("Failed to upload video");
        }
        System.out.println("Video processing completed");
    }

    // 2. Chat using the URI
    public String askAboutVideo(String fileUri, String question) throws IOException, InterruptedException {
        String jsonBody = String.format("""
            {
              "contents": [{
                "parts": [
                  {"text": "%s"},
                  {"file_data": {"mime_type": "video/mp4", "file_uri": "%s"}}
                ]
              }]
            }
            """, question, fileUri);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
