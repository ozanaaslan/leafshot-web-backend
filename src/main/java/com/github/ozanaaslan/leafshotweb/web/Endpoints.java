package com.github.ozanaaslan.leafshotweb.web;

import com.github.ozanaaslan.leafshotweb.LeafShotWeb;
import com.github.ozanaaslan.leafshotweb.util.Manifest;
import com.github.ozanaaslan.leafshotweb.util.Utils;
import com.github.ozanaaslan.leafshotweb.util.WebConfig;
import com.github.ozanaaslan.lwjwl.web.endpoint.EndpointController;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.Endpoint;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.Param;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.method.GET;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.method.PATCH;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.method.POST;
import com.github.ozanaaslan.lwjwl.web.endpoint.response.*;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Endpoints {

    @Endpoint("/api/v1/manifest") @GET
    public static Response getManifest(EndpointController endpointController, @Param("id") String id) {
        System.out.println("[DEBUG] getManifest called for ID: " + id);
        File resourceDir = new File(LeafShotWeb.getLeafShotWeb().getWorkingDirectory(), id);
        File manifestFile = new File(resourceDir, "manifest.json");

        if (!resourceDir.exists() || !manifestFile.exists()) {
            System.out.println("[DEBUG] Resource not found for ID: " + id);
            return new Response(404, ContentType.APPLICATION_JSON,
                    new JSONObject().put("message", "Resource not found").toString());
        }

        try {
            String content = new String(java.nio.file.Files.readAllBytes(manifestFile.toPath()));
            Manifest manifest = Manifest.fromJSON(content);
            System.out.println("[DEBUG] Successfully fetched manifest for ID: " + id);
            return new Response(200, ContentType.APPLICATION_JSON,
                    new JSONObject(manifest.toJSON().toString()).put("message", "resource found!").toString());
        } catch (Exception e) {
            System.out.println("[DEBUG] Error fetching manifest for ID: " + id + " - " + e.getMessage());
            e.printStackTrace();
            return new Response(500, ContentType.APPLICATION_JSON,
                    new JSONObject().put("message", "something went wrong fetching the requested resource").toString());
        }
    }

    @Endpoint("/api/v1/image") @GET
    public static Response getImage(EndpointController endpointController, @Param("id") String id) {
        System.out.println("[DEBUG] getImage called for ID: " + id);
        File resourceDir = new File(LeafShotWeb.getLeafShotWeb().getWorkingDirectory(), id);
        File manifestFile = new File(resourceDir, "manifest.json");

        if (!resourceDir.exists() || !manifestFile.exists()) {
            System.out.println("[DEBUG] Resource not found for ID: " + id);
            return new Response(404, ContentType.APPLICATION_JSON, new JSONObject().put("error", "Not found").toString());
        }

        try {
            // 1. Load and Validate Manifest
            String content = new String(java.nio.file.Files.readAllBytes(manifestFile.toPath()));
            Manifest manifest = Manifest.fromJSON(content);

            if (manifest.isDeleted() || manifest.isEarmarkedForRemoval()) {
                System.out.println("[DEBUG] Resource is deleted or earmarked for removal for ID: " + id);
                return new Response(410, ContentType.APPLICATION_JSON, new JSONObject().put("error", "Resource no longer available").toString());
            }

            // Check if resource is still "alive" based on lifetime
            if (manifest.getExpirationTimestamp() == 0) {
                long lifeTimeMillis = (long) LeafShotWeb.getLeafShotWeb().getWebConfig().getResourceLifetimeHours() * 60 * 60 * 1000;
                manifest.setExpirationTimestamp(manifest.getTimestampOfCreation() + lifeTimeMillis);
            }

            if (System.currentTimeMillis() > manifest.getExpirationTimestamp()) {
                System.out.println("[DEBUG] Resource expired for ID: " + id);
                return new Response(410, ContentType.APPLICATION_JSON, new JSONObject().put("error", "Resource expired").toString());
            }

            // 2. Locate the Image File
            File[] files = resourceDir.listFiles((dir, name) -> name.endsWith(".png"));
            if (files == null || files.length == 0) {
                System.out.println("[DEBUG] Image file missing for ID: " + id);
                return new Response(404, ContentType.APPLICATION_JSON, new JSONObject().put("error", "Image file missing").toString());
            }

            // 3. Update Statistics (Views) and Prolong lifetime
            manifest.setViews(manifest.getViews() + 1);

            WebConfig config = LeafShotWeb.getLeafShotWeb().getWebConfig();
            if (config.isProlongable()) {
                long prolongedMillis = (long) config.getResourceProlongedHoursPerAccess() * 60 * 60 * 1000;
                manifest.setExpirationTimestamp(manifest.getExpirationTimestamp() + prolongedMillis);
                System.out.println("[DEBUG] Prolonged lifetime for ID: " + id);
            }

            java.nio.file.Files.write(manifestFile.toPath(), manifest.toJSON().toString().getBytes());

            // 4. Return Image
            byte[] imageBytes = java.nio.file.Files.readAllBytes(files[0].toPath());
            System.out.println("[DEBUG] Successfully returning image for ID: " + id + " (Size: " + imageBytes.length + " bytes)");
            Response response = new Response(Status.OK, ContentType.IMAGE_PNG, imageBytes);
            return response;

        } catch (Exception e) {
            System.out.println("[DEBUG] Error in getImage for ID: " + id + " - " + e.getMessage());
            e.printStackTrace();
            return new Response(500, ContentType.APPLICATION_JSON, new JSONObject().put("error", "Internal error").toString());
        }
    }

    @Endpoint("/api/v1/report") @PATCH
    public static Response report(EndpointController endpointController, @Param("id") String id) {
        System.out.println("[DEBUG] report called for ID: " + id);
        File resourceDir = new File(LeafShotWeb.getLeafShotWeb().getWorkingDirectory(), id);
        File manifestFile = new File(resourceDir, "manifest.json");

        if (!resourceDir.exists() || !manifestFile.exists()) {
            System.out.println("[DEBUG] Resource not found for ID: " + id);
            return new Response(404, ContentType.APPLICATION_JSON, new JSONObject().put("error", "Not found").toString());
        }

        try {
            String content = new String(java.nio.file.Files.readAllBytes(manifestFile.toPath()));
            Manifest manifest = Manifest.fromJSON(content);

            if (manifest.isDeleted() || manifest.isEarmarkedForRemoval()) {
                System.out.println("[DEBUG] Resource already deleted or earmarked for ID: " + id);
                return new Response(410, ContentType.APPLICATION_JSON, new JSONObject().put("error", "Resource no longer available").toString());
            }

            WebConfig config = LeafShotWeb.getLeafShotWeb().getWebConfig();

            // 1. Update Statistics (Reports)
            manifest.setReports(manifest.getReports() + 1);
            System.out.println("[DEBUG] Report count incremented for ID: " + id + ". New count: " + manifest.getReports());

            // 2. Reduce lifetime
            if (manifest.getExpirationTimestamp() == 0) {
                long lifeTimeMillis = (long) config.getResourceLifetimeHours() * 60 * 60 * 1000;
                manifest.setExpirationTimestamp(manifest.getTimestampOfCreation() + lifeTimeMillis);
            }
            long withdrawMillis = (long) config.getTimeWithdrawInHoursPerReport() * 60 * 60 * 1000;
            manifest.setExpirationTimestamp(manifest.getExpirationTimestamp() - withdrawMillis);
            System.out.println("[DEBUG] Reduced lifetime for ID: " + id + " due to report.");

            // 3. Check for threshold
            if (manifest.getReports() >= config.getReportsUponDeletion()) {
                manifest.setEarmarkedForRemoval(true);
                manifest.setTimestampOfRemoval(System.currentTimeMillis());
                System.out.println("[DEBUG] Resource ID: " + id + " earmarked for removal (threshold reached).");
            }

            java.nio.file.Files.write(manifestFile.toPath(), manifest.toJSON().toString().getBytes());

            return new Response(200, ContentType.APPLICATION_JSON, new JSONObject().put("message", "Resource reported").toString());

        } catch (Exception e) {
            System.out.println("[DEBUG] Error in report for ID: " + id + " - " + e.getMessage());
            e.printStackTrace();
            return new Response(500, ContentType.APPLICATION_JSON, new JSONObject().put("error", "Internal error").toString());
        }
    }

    @Endpoint("/api/v1/image") @POST
    public static Response uploadImage(EndpointController endpointController) {
        System.out.println("[DEBUG] uploadImage called from IP: " + endpointController.getRequesterAddress());
        byte[] image = null;
        String contentType = endpointController.getExchange().getRequestHeaders().getFirst("Content-Type");
        System.out.println("[DEBUG] Content-Type: " + contentType);

        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            System.out.println("[DEBUG] Parsing multipart request");
            image = parseMultipart(endpointController.getRequestBody(), contentType);
        } else {
            System.out.println("[DEBUG] Handling Base64 body upload");
            try {
                String body = new String(endpointController.getRequestBody());
                if (body.contains(",")) {
                    // Handle Data URL (data:image/png;base64,...)
                    body = body.split(",")[1];
                }
                image = Base64.getDecoder().decode(body.trim());
            } catch (Exception e) {
                System.out.println("[DEBUG] Failed to decode Base64, falling back to raw bytes");
                image = endpointController.getRequestBody();
            }
        }

        if (image == null || image.length == 0) {
            System.out.println("[DEBUG] Upload failed: No image data");
            return new Response(400, ContentType.APPLICATION_JSON, new JSONObject().put("error", "No image data").toString());
        }

        System.out.println("[DEBUG] Received image data size: " + image.length + " bytes");

        String id = Utils.alphanumericStringGeneratorAZ09(8);
        System.out.println("[DEBUG] Generated ID for upload: " + id);
        File resourceDir = new File(LeafShotWeb.getLeafShotWeb().getWorkingDirectory(), id);
        if (!resourceDir.exists()) resourceDir.mkdirs();

        File manifestFile = new File(resourceDir, "manifest.json");
        File imageFile = new File(resourceDir, id + ".png");

        try {
            Files.write(imageFile.toPath(), image);
            System.out.println("[DEBUG] Image file saved: " + imageFile.getAbsolutePath());

            WebConfig config = LeafShotWeb.getLeafShotWeb().getWebConfig();
            long lifeTimeMillis = (long) config.getResourceLifetimeHours() * 60 * 60 * 1000;

            Manifest manifest = Manifest.builder()
                    .id(id)
                    .timestampOfCreation(System.currentTimeMillis())
                    .expirationTimestamp(System.currentTimeMillis() + lifeTimeMillis)
                    .fileName(id + ".png")
                    .filePath(imageFile.getAbsolutePath())
                    .mimeType("image/png")
                    .fileSize(image.length)
                    .uploaderIp(endpointController.getRequesterAddress())
                    .build();

            // Try to get image dimensions
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(image));
                if (img != null) {
                    manifest.setWidth(img.getWidth());
                    manifest.setHeight(img.getHeight());
                    System.out.println("[DEBUG] Image dimensions: " + img.getWidth() + "x" + img.getHeight());
                } else {
                    System.out.println("[DEBUG] Could not read image dimensions (ImageIO.read returned null)");
                }
            } catch (Exception e) {
                System.out.println("[DEBUG] Error reading image dimensions: " + e.getMessage());
            }

            Files.write(manifestFile.toPath(), manifest.toJSON().toString().getBytes());
            System.out.println("[DEBUG] Manifest file saved: " + manifestFile.getAbsolutePath());

            System.out.println("[DEBUG] Upload successful for ID: " + id);
            return new Response(200, ContentType.APPLICATION_JSON,
                    new JSONObject().put("id", id).put("message", "Upload successful").toString());

        } catch (Exception e) {
            System.out.println("[DEBUG] Error in uploadImage: " + e.getMessage());
            e.printStackTrace();
            return new Response(500, ContentType.APPLICATION_JSON, new JSONObject().put("error", "Internal error").toString());
        }
    }

    private static byte[] parseMultipart(byte[] body, String contentType) {
        try {
            String boundary = "";
            for (String s : contentType.split(";")) {
                if (s.trim().startsWith("boundary=")) {
                    boundary = s.split("=")[1].trim();
                }
            }

            if (boundary.isEmpty()) return null;

            byte[] boundaryBytes = ("--" + boundary).getBytes();
            List<Integer> boundaryPositions = new ArrayList<Integer>();

            // Find all boundaries
            for (int i = 0; i < body.length - boundaryBytes.length; i++) {
                boolean match = true;
                for (int j = 0; j < boundaryBytes.length; j++) {
                    if (body[i + j] != boundaryBytes[j]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    boundaryPositions.add(Integer.valueOf(i));
                }
            }

            for (int i = 0; i < boundaryPositions.size() - 1; i++) {
                int start = boundaryPositions.get(i).intValue() + boundaryBytes.length;
                int end = boundaryPositions.get(i + 1).intValue();

                // Find header-body separator
                int dataStart = -1;
                for (int j = start; j < end - 4; j++) {
                    if (body[j] == '\r' && body[j + 1] == '\n' && body[j + 2] == '\r' && body[j + 3] == '\n') {
                        dataStart = j + 4;
                        break;
                    }
                }

                if (dataStart == -1) continue;

                String headers = new String(body, start, dataStart - start);
                if (headers.contains("name=\"image\"") || boundaryPositions.size() == 2) {
                    // Extract data (minus the trailing CRLF before boundary)
                    int actualEnd = end;
                    if (body[actualEnd - 2] == '\r' && body[actualEnd - 1] == '\n') {
                        actualEnd -= 2;
                    }

                    byte[] result = new byte[actualEnd - dataStart];
                    System.arraycopy(body, dataStart, result, 0, result.length);
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
