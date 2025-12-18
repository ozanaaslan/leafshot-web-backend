package com.github.ozanaaslan.leafshotweb.util;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.json.JSONObject;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manifest {

    private String id; // The random string used for the URL/Folder
    private int reports = 0;
    private int views = 0;
    private boolean earmarkedForRemoval = false;
    private boolean deleted = false;
    private String uploaderIp = "0.0.0.0";
    private long timestampOfCreation = System.currentTimeMillis();
    private long timestampOfRemoval = 0L;
    private long expirationTimestamp = 0L;

    // File Specifics
    private String filePath = "";
    private String fileName = "";
    private String originalName = "";
    private String fileHash = "";
    private String mimeType = "image/png";
    private long fileSize = 0L;
    private int width = 0;
    private int height = 0;


    public static Manifest fromJSON(String json) {
        JSONObject obj = new JSONObject(json);
        return Manifest.builder()
                .id(obj.optString("id"))
                .reports(obj.optInt("reports", 0))
                .views(obj.optInt("views", 0))
                .earmarkedForRemoval(obj.optBoolean("earmarkedForRemoval", false))
                .deleted(obj.optBoolean("deleted", false))
                .uploaderIp(obj.optString("uploaderIp", "0.0.0.0"))
                .timestampOfCreation(obj.optLong("timestampOfCreation", 0L))
                .timestampOfRemoval(obj.optLong("timestampOfRemoval", 0L))
                .expirationTimestamp(obj.optLong("expirationTimestamp", 0L))
                .filePath(obj.optString("filePath"))
                .fileName(obj.optString("fileName"))
                .originalName(obj.optString("originalName"))
                .fileHash(obj.optString("fileHash"))
                .mimeType(obj.optString("mimeType"))
                .fileSize(obj.optLong("fileSize"))
                .width(obj.optInt("width"))
                .height(obj.optInt("height"))
                .build();
    }

    public JSONObject toJSON() {
        return new JSONObject(this);
    }

    public static JSONObject toJSON(Manifest manifest) {
        return manifest.toJSON();
    }
}