package com.github.ozanaaslan.leafshotweb.web;

import com.github.ozanaaslan.leafshotweb.config.LeafShotProperties;
import com.github.ozanaaslan.leafshotweb.service.LeafShotService;
import com.github.ozanaaslan.leafshotweb.util.Manifest;
import com.github.ozanaaslan.leafshotweb.util.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1")
public class ImageController {

    @Autowired
    private LeafShotService service;

    @Autowired
    private LeafShotProperties properties;

    private final Map<String, RateLimitData> rateLimits = new ConcurrentHashMap<>();

    private static class RateLimitData {
        AtomicInteger count = new AtomicInteger(0);
        AtomicLong lastReset = new AtomicLong(System.currentTimeMillis());
    }

    private boolean isRateLimited(String ip) {
        if (!properties.getRateLimit().isEnabled()) return false;
        
        RateLimitData data = rateLimits.computeIfAbsent(ip, k -> new RateLimitData());
        long now = System.currentTimeMillis();
        if (now - data.lastReset.get() > 60000) {
            data.count.set(0);
            data.lastReset.set(now);
        }
        
        return data.count.incrementAndGet() > properties.getRateLimit().getRequestsPerMinute();
    }

    private String getClientIp(String forwardedFor, HttpServletRequest request) {
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // X-Forwarded-For may contain multiple IPs
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr(); // always non-null
    }

    @GetMapping("/manifest")
    public ResponseEntity<String> getManifest(  @RequestParam String id,
                                                @RequestHeader(value = "X-Forwarded-For", required = false) String forwardIp,
                                                HttpServletRequest request) {
        if (isRateLimited(getClientIp(forwardIp, request)))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new JSONObject().put("error", "Rate limit exceeded").toString());
        if (id == null || !id.matches("^[a-zA-Z0-9]+$")) {
            return ResponseEntity.badRequest().body(new JSONObject().put("error", "Invalid ID").toString());
        }

        Manifest manifest = service.loadManifest(id);
        if (manifest == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSONObject().put("message", "Resource not found").toString());
        return ResponseEntity.ok(new JSONObject(manifest.toJSON().toString()).put("message", "resource found!").toString());
    }



    @GetMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImage(@RequestParam String id,
                                           @RequestHeader(value = "X-Forwarded-For", required = false) String forwardIp,
                                           HttpServletRequest request) {
        if (isRateLimited(getClientIp(forwardIp, request)))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .build();

        if (id == null || !id.matches("^[a-zA-Z0-9]+$")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Manifest manifest = service.loadManifest(id);
            if (manifest == null || manifest.isDeleted() || manifest.isEarmarkedForRemoval()) return ResponseEntity.status(HttpStatus.GONE).build();

            if (System.currentTimeMillis() > manifest.getExpirationTimestamp()) return ResponseEntity.status(HttpStatus.GONE).build();

            File resourceDir = service.getResourceDir(id);
            File[] files = resourceDir.listFiles((dir, name) -> name.endsWith(".png"));
            if (files == null || files.length == 0) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            // Update stats
            manifest.setViews(manifest.getViews() + 1);
            if (properties.getResource().isProlongable()) {
                long prolongedMillis = (long) properties.getResource().getProlongedHoursPerAccess() * 60 * 60 * 1000;
                manifest.setExpirationTimestamp(manifest.getExpirationTimestamp() + prolongedMillis);
            }
            service.saveManifest(manifest);

            return ResponseEntity.ok(Files.readAllBytes(files[0].toPath()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(
            @RequestParam(required = false) MultipartFile image,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardIp,
            HttpServletRequest request) {

        if (isRateLimited(getClientIp(forwardIp, request)))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new JSONObject().put("error", "Rate limit exceeded").toString());
        try {
            byte[] imageData = null;

            if (image != null && !image.isEmpty()) {
                String contentType = image.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().body(new JSONObject().put("error", "Only images are allowed").toString());
                }
                imageData = image.getBytes();
            }

            if (imageData == null || imageData.length == 0) {
                return ResponseEntity.badRequest().body(new JSONObject().put("error", "No image data").toString());
            }

            // Basic image validation
            BufferedImage img;
            try {
                img = ImageIO.read(new ByteArrayInputStream(imageData));
                if (img == null) {
                    return ResponseEntity.badRequest().body(new JSONObject().put("error", "Invalid image format").toString());
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new JSONObject().put("error", "Invalid image data").toString());
            }

            String id = Utils.alphanumericStringGeneratorAZ09(8);
            File resourceDir = service.getResourceDir(id);
            resourceDir.mkdirs();

            File imageFile = new File(resourceDir, id + ".png");
            Files.write(imageFile.toPath(), imageData);

            Manifest manifest = Manifest.builder()
                    .id(id)
                    .timestampOfCreation(System.currentTimeMillis())
                    .expirationTimestamp(System.currentTimeMillis() + (long) properties.getResource().getLifetimeHours() * 60 * 60 * 1000)
                    .fileName(id + ".png")
                    .filePath(imageFile.getAbsolutePath())
                    .mimeType("image/png")
                    .fileSize(imageData.length)
                    .uploaderIp(getClientIp(forwardIp, request))
                    .width(img.getWidth())
                    .height(img.getHeight())
                    .build();

            service.saveManifest(manifest);

            return ResponseEntity.ok(new JSONObject().put("id", id).put("message", "Upload successful").toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JSONObject().put("error", "Internal error").toString());
        }
    }

    @PatchMapping("/report")
    public ResponseEntity<String> report(@RequestParam String id,
                                         @RequestHeader(value = "X-Forwarded-For", required = false) String forwardIp,
                                         HttpServletRequest request) {
        if (isRateLimited(getClientIp(forwardIp, request)))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new JSONObject().put("error", "Rate limit exceeded").toString());

        if (id == null || !id.matches("^[a-zA-Z0-9]+$")) {
            return ResponseEntity.badRequest().body(new JSONObject().put("error", "Invalid ID").toString());
        }

        try {
            Manifest manifest = service.loadManifest(id);
            if (manifest == null || manifest.isDeleted()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            manifest.setReports(manifest.getReports() + 1);
            long withdrawMillis = (long) properties.getReports().getTimeWithdrawHours() * 60 * 60 * 1000;
            manifest.setExpirationTimestamp(manifest.getExpirationTimestamp() - withdrawMillis);

            if (manifest.getReports() >= properties.getReports().getDeletionThreshold()) {
                manifest.setEarmarkedForRemoval(true);
                manifest.setTimestampOfRemoval(System.currentTimeMillis());
            }

            service.saveManifest(manifest);
            return ResponseEntity.ok(new JSONObject().put("message", "Resource reported").toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
