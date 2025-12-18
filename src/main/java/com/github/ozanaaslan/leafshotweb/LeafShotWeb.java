package com.github.ozanaaslan.leafshotweb;

import com.github.ozanaaslan.leafshotweb.util.Manifest;
import com.github.ozanaaslan.leafshotweb.util.WebConfig;
import com.github.ozanaaslan.leafshotweb.web.Endpoints;
import com.github.ozanaaslan.lwjwl.LWJWL;
import lombok.Getter;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LeafShotWeb {

    @Getter private LWJWL lwjwl;
    @Getter private WebConfig webConfig;
    @Getter private File workingDirectory;

    @Getter private static LeafShotWeb leafShotWeb;

    public LeafShotWeb() {
        init();
    }

    private void init(){
        System.out.println("[DEBUG] Initializing LeafShotWeb...");
        this.webConfig = new WebConfig();
        this.workingDirectory = new File(System.getProperty("user.dir") + "/workdir");
        System.out.println("[DEBUG] Working directory: " + workingDirectory.getAbsolutePath());
        if(!workingDirectory.exists()) {
            boolean created = workingDirectory.mkdirs();
            System.out.println("[DEBUG] Created working directory: " + created);
        }
        this.lwjwl = new LWJWL(8091);
        System.out.println("[DEBUG] LWJWL started on port 8091");
        this.lwjwl.register(Endpoints.class);
        System.out.println("[DEBUG] Registered Endpoints class");
        startPurgeTask();
        System.out.println("[DEBUG] Initialization complete.");
    }

    private void startPurgeTask() {
        System.out.println("[DEBUG] Starting Purge Task (every 1 hour)");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("[DEBUG] Purge Task running...");
            File[] folders = workingDirectory.listFiles(File::isDirectory);
            if (folders == null) {
                System.out.println("[DEBUG] Purge Task: No folders found in " + workingDirectory.getAbsolutePath());
                return;
            }
            int purgedCount = 0;
            for (File folder : folders) {
                File manifestFile = new File(folder, "manifest.json");
                if (!manifestFile.exists()) {
                    System.out.println("[DEBUG] Purge Task: manifest.json missing in folder " + folder.getName());
                    continue;
                }
                try {
                    String content = new String(Files.readAllBytes(manifestFile.toPath()));
                    Manifest manifest = Manifest.fromJSON(content);
                    
                    if (manifest.isDeleted()) continue;

                    boolean expired = false;
                    if (manifest.getExpirationTimestamp() != 0) {
                        expired = System.currentTimeMillis() > manifest.getExpirationTimestamp();
                    } else {
                        long lifeTimeMillis = (long) webConfig.getResourceLifetimeHours() * 60 * 60 * 1000;
                        expired = System.currentTimeMillis() > (manifest.getTimestampOfCreation() + lifeTimeMillis);
                    }

                    if (expired || manifest.isEarmarkedForRemoval()) {
                        System.out.println("[DEBUG] Purge Task: Deleting " + folder.getName() + " (Expired: " + expired + ", Earmarked: " + manifest.isEarmarkedForRemoval() + ")");
                        deleteFolder(folder);
                        purgedCount++;
                    }
                } catch (Exception e) {
                    System.out.println("[DEBUG] Purge Task: Error processing folder " + folder.getName());
                    e.printStackTrace();
                }
            }
            System.out.println("[DEBUG] Purge Task finished. Purged " + purgedCount + " folders.");
        }, 1, 1, TimeUnit.HOURS);
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
        folder.delete();
    }

    public static void main(String[] args) {
        leafShotWeb = new LeafShotWeb();
    }
}
