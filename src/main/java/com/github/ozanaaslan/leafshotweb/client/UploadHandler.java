package com.github.ozanaaslan.leafshotweb.client;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class UploadHandler {

    private final String baseUrl;

    public UploadHandler(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            this.baseUrl = baseUrl;
        }
    }

    /**
     * Uploads a BufferedImage to the LeafShot server.
     * @param image The BufferedImage to upload.
     * @return The direct link to the uploaded resource, or null if the upload failed.
     */
    public String uploadImage(BufferedImage image) {
        try {
            // Convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Prepare connection
            URL url = new URL(baseUrl + "/api/v1/image");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestProperty("Content-Length", String.valueOf(base64Image.length()));

            // Write Base64 string to request body
            try (OutputStream os = connection.getOutputStream()) {
                os.write(base64Image.getBytes());
                os.flush();
            }

            // Read response
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String id = jsonResponse.getString("id");
                    
                    // Return the link to the resource
                    return baseUrl + "/api/v1/image?id=" + id;
                }
            } else {
                System.err.println("Upload failed with HTTP response code: " + responseCode);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.err.println(line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
