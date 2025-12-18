package com.github.ozanaaslan.leafshotweb.util;

import org.json.JSONObject;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "manifests")
public class Manifest {

    @Id
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

    public Manifest() {}

    public Manifest(String id, int reports, int views, boolean earmarkedForRemoval, boolean deleted, String uploaderIp, long timestampOfCreation, long timestampOfRemoval, long expirationTimestamp, String filePath, String fileName, String originalName, String fileHash, String mimeType, long fileSize, int width, int height) {
        this.id = id;
        this.reports = reports;
        this.views = views;
        this.earmarkedForRemoval = earmarkedForRemoval;
        this.deleted = deleted;
        this.uploaderIp = uploaderIp;
        this.timestampOfCreation = timestampOfCreation;
        this.timestampOfRemoval = timestampOfRemoval;
        this.expirationTimestamp = expirationTimestamp;
        this.filePath = filePath;
        this.fileName = fileName;
        this.originalName = originalName;
        this.fileHash = fileHash;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getReports() { return reports; }
    public void setReports(int reports) { this.reports = reports; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
    public boolean isEarmarkedForRemoval() { return earmarkedForRemoval; }
    public void setEarmarkedForRemoval(boolean earmarkedForRemoval) { this.earmarkedForRemoval = earmarkedForRemoval; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public String getUploaderIp() { return uploaderIp; }
    public void setUploaderIp(String uploaderIp) { this.uploaderIp = uploaderIp; }
    public long getTimestampOfCreation() { return timestampOfCreation; }
    public void setTimestampOfCreation(long timestampOfCreation) { this.timestampOfCreation = timestampOfCreation; }
    public long getTimestampOfRemoval() { return timestampOfRemoval; }
    public void setTimestampOfRemoval(long timestampOfRemoval) { this.timestampOfRemoval = timestampOfRemoval; }
    public long getExpirationTimestamp() { return expirationTimestamp; }
    public void setExpirationTimestamp(long expirationTimestamp) { this.expirationTimestamp = expirationTimestamp; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public static ManifestBuilder builder() {
        return new ManifestBuilder();
    }

    public static class ManifestBuilder {
        private String id;
        private int reports;
        private int views;
        private boolean earmarkedForRemoval;
        private boolean deleted;
        private String uploaderIp;
        private long timestampOfCreation = System.currentTimeMillis();
        private long timestampOfRemoval;
        private long expirationTimestamp;
        private String filePath;
        private String fileName;
        private String originalName;
        private String fileHash;
        private String mimeType = "image/png";
        private long fileSize;
        private int width;
        private int height;

        public ManifestBuilder id(String id) { this.id = id; return this; }
        public ManifestBuilder reports(int reports) { this.reports = reports; return this; }
        public ManifestBuilder views(int views) { this.views = views; return this; }
        public ManifestBuilder earmarkedForRemoval(boolean earmarkedForRemoval) { this.earmarkedForRemoval = earmarkedForRemoval; return this; }
        public ManifestBuilder deleted(boolean deleted) { this.deleted = deleted; return this; }
        public ManifestBuilder uploaderIp(String uploaderIp) { this.uploaderIp = uploaderIp; return this; }
        public ManifestBuilder timestampOfCreation(long timestampOfCreation) { this.timestampOfCreation = timestampOfCreation; return this; }
        public ManifestBuilder timestampOfRemoval(long timestampOfRemoval) { this.timestampOfRemoval = timestampOfRemoval; return this; }
        public ManifestBuilder expirationTimestamp(long expirationTimestamp) { this.expirationTimestamp = expirationTimestamp; return this; }
        public ManifestBuilder filePath(String filePath) { this.filePath = filePath; return this; }
        public ManifestBuilder fileName(String fileName) { this.fileName = fileName; return this; }
        public ManifestBuilder originalName(String originalName) { this.originalName = originalName; return this; }
        public ManifestBuilder fileHash(String fileHash) { this.fileHash = fileHash; return this; }
        public ManifestBuilder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public ManifestBuilder fileSize(long fileSize) { this.fileSize = fileSize; return this; }
        public ManifestBuilder width(int width) { this.width = width; return this; }
        public ManifestBuilder height(int height) { this.height = height; return this; }

        public Manifest build() {
            return new Manifest(id, reports, views, earmarkedForRemoval, deleted, uploaderIp, timestampOfCreation, timestampOfRemoval, expirationTimestamp, filePath, fileName, originalName, fileHash, mimeType, fileSize, width, height);
        }
    }

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