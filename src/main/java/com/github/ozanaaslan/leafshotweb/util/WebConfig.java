package com.github.ozanaaslan.leafshotweb.util;

public class WebConfig extends Config {

    public WebConfig() {
        super("webconfig");
        setDefaults();
    }

    private void setDefaults() {
        if (!existing("ratelimit.per.minute")) set("ratelimit.per.minute", "10");
        if (!existing("upload.max.size.mb")) set("upload.max.size.mb", "10");
        if (!existing("resource.lifetime.hours")) set("resource.lifetime.hours", "168"); // 1 week
        if (!existing("resource.prolongable")) set("resource.prolongable", "true");
        if (!existing("resource.prolonged.hours.per.access")) set("resource.prolonged.hours.per.access", "24");
        if (!existing("reports.deletion.threshold")) set("reports.deletion.threshold", "5");
        if (!existing("reports.time.withdraw.hours")) set("reports.time.withdraw.hours", "24");
    }

    public int getRateLimitPerMinute() {
        return Integer.parseInt((String) get("ratelimit.per.minute", "10"));
    }

    public int getMaxFileSizeMB() {
        return Integer.parseInt((String) get("upload.max.size.mb", "10"));
    }

    public int getResourceLifetimeHours() {
        return Integer.parseInt((String) get("resource.lifetime.hours", "168"));
    }

    public boolean isProlongable() {
        return Boolean.parseBoolean((String) get("resource.prolongable", "true"));
    }

    public int getResourceProlongedHoursPerAccess() {
        return Integer.parseInt((String) get("resource.prolonged.hours.per.access", "24"));
    }

    public int getReportsUponDeletion() {
        return Integer.parseInt((String) get("reports.deletion.threshold", "5"));
    }

    public int getTimeWithdrawInHoursPerReport() {
        return Integer.parseInt((String) get("reports.time.withdraw.hours", "24"));
    }
}