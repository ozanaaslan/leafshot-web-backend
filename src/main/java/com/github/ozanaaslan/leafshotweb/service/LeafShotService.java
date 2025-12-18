package com.github.ozanaaslan.leafshotweb.service;

import com.github.ozanaaslan.leafshotweb.config.LeafShotProperties;
import com.github.ozanaaslan.leafshotweb.repository.ManifestRepository;
import com.github.ozanaaslan.leafshotweb.util.Manifest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LeafShotService {

    @Autowired
    private LeafShotProperties properties;

    @Autowired
    private ManifestRepository repository;

    private File workingDirectory;

    @PostConstruct
    public void init() {
        this.workingDirectory = new File(System.getProperty("user.dir"), properties.getWorkingDirectory());
        if (!workingDirectory.exists()) {
            workingDirectory.mkdirs();
        }
        System.out.println("[DEBUG] LeafShot Service initialized. Working directory: " + workingDirectory.getAbsolutePath());
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public File getResourceDir(String id) {
        return new File(workingDirectory, id);
    }

    public Manifest loadManifest(String id) {
        return repository.findById(id).orElse(null);
    }

    public void saveManifest(Manifest manifest) {
        repository.save(manifest);
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void purgeExpiredResources() {
        System.out.println("[DEBUG] Spring Purge Task running...");
        long now = System.currentTimeMillis();
        
        // Find expired
        List<Manifest> expired = repository.findByDeletedFalseAndExpirationTimestampLessThan(now);
        // Find earmarked
        List<Manifest> earmarked = repository.findByDeletedFalseAndEarmarkedForRemovalTrue();
        
        int purgedCount = 0;
        
        purgedCount += processPurge(expired);
        purgedCount += processPurge(earmarked);

        System.out.println("[DEBUG] Purge Task finished. Purged " + purgedCount + " folders.");
    }

    private int processPurge(List<Manifest> manifests) {
        int count = 0;
        for (Manifest manifest : manifests) {
            try {
                File folder = getResourceDir(manifest.getId());
                if (folder.exists()) {
                    System.out.println("[DEBUG] Purge Task: Deleting " + folder.getName());
                    deleteFolder(folder);
                }
                manifest.setDeleted(true);
                manifest.setTimestampOfRemoval(System.currentTimeMillis());
                repository.save(manifest);
                count++;
            } catch (Exception e) {
                System.err.println("[DEBUG] Error purging manifest " + manifest.getId() + ": " + e.getMessage());
            }
        }
        return count;
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
        folder.delete();
    }
}
