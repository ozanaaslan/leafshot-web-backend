package com.github.ozanaaslan.leafshotweb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "leafshot")
public class LeafShotProperties {

    private String workingDirectory = "workdir";
    private int uploadMaxSizeMb = 100;
    
    private final Resource resource = new Resource();
    private final Reports reports = new Reports();
    private final RateLimit rateLimit = new RateLimit();

    public String getWorkingDirectory() { return workingDirectory; }
    public void setWorkingDirectory(String workingDirectory) { this.workingDirectory = workingDirectory; }
    public int getUploadMaxSizeMb() { return uploadMaxSizeMb; }
    public void setUploadMaxSizeMb(int uploadMaxSizeMb) { this.uploadMaxSizeMb = uploadMaxSizeMb; }
    public Resource getResource() { return resource; }
    public Reports getReports() { return reports; }
    public RateLimit getRateLimit() { return rateLimit; }

    public static class Resource {
        private int lifetimeHours = 168;
        private boolean prolongable = true;
        private int prolongedHoursPerAccess = 24;

        public int getLifetimeHours() { return lifetimeHours; }
        public void setLifetimeHours(int lifetimeHours) { this.lifetimeHours = lifetimeHours; }
        public boolean isProlongable() { return prolongable; }
        public void setProlongable(boolean prolongable) { this.prolongable = prolongable; }
        public int getProlongedHoursPerAccess() { return prolongedHoursPerAccess; }
        public void setProlongedHoursPerAccess(int prolongedHoursPerAccess) { this.prolongedHoursPerAccess = prolongedHoursPerAccess; }
    }

    public static class Reports {
        private int deletionThreshold = 5;
        private int timeWithdrawHours = 24;

        public int getDeletionThreshold() { return deletionThreshold; }
        public void setDeletionThreshold(int deletionThreshold) { this.deletionThreshold = deletionThreshold; }
        public int getTimeWithdrawHours() { return timeWithdrawHours; }
        public void setTimeWithdrawHours(int timeWithdrawHours) { this.timeWithdrawHours = timeWithdrawHours; }
    }

    public static class RateLimit {
        private boolean enabled = true;
        private int requestsPerMinute = 10;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getRequestsPerMinute() { return requestsPerMinute; }
        public void setRequestsPerMinute(int requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
    }
}
