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
     * Uploads a BufferedImage to the LeafShot server using Multipart form-data.
     * @param image The BufferedImage to upload.
     * @return The direct link to the uploaded resource, or null if the upload failed.
     */
    public String uploadImage(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return uploadMultipart(baos.toByteArray(), "image.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Uploads raw bytes to the LeafShot server using Multipart form-data.
     * @param imageBytes The image bytes to upload.
     * @param fileName The filename to use in the multipart request.
     * @return The direct link to the uploaded resource, or null if the upload failed.
     */
    public String uploadMultipart(byte[] imageBytes, String fileName) {
        String boundary = "LeafShotBoundary" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";

        try {
            URL url = new URL(baseUrl + "/api/v1/image");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {
                
                writer.append("--").append(boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);
                writer.append("Content-Type: image/png").append(LINE_FEED);
                writer.append(LINE_FEED).flush();

                os.write(imageBytes);
                os.flush();

                writer.append(LINE_FEED);
                writer.append("--").append(boundary).append("--").append(LINE_FEED).flush();
            }

            return handleResponse(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Uploads raw bytes using Base64 encoding.
     * @param imageBytes The image bytes to upload.
     * @return The direct link to the uploaded resource, or null if the upload failed.
     */
    public String uploadBytesBase64(byte[] imageBytes) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            URL url = new URL(baseUrl + "/api/v1/image");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/plain");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(base64Image.getBytes());
                os.flush();
            }

            return handleResponse(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String handleResponse(HttpURLConnection connection) throws IOException {
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
                return baseUrl + "/api/v1/image?id=" + id;
            }
        } else {
            System.err.println("Request failed with HTTP response code: " + responseCode);
            InputStream es = connection.getErrorStream();
            if (es != null) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(es))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.err.println(line);
                    }
                }
            }
        }
        return null;
    }
}
