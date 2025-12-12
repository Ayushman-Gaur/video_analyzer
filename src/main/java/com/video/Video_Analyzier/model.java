package com.video.Video_Analyzier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class model {
    public static void main(String[] args) {
        String apiKey = "AIzaSyCr_9w_OElOTz05y52JzLqo2EHPkuKPoQI"; // Put your key here

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Available Models:\n" + response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
