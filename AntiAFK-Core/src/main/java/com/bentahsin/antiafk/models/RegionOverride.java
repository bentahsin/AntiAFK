package com.bentahsin.antiafk.models;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RegionOverride {
    private final String regionName;
    private final long maxAfkTime;
    private final List<Map<String, String>> actions;

    public RegionOverride(String regionName, long maxAfkTime, List<Map<String, String>> actions) {
        this.regionName = regionName;
        this.maxAfkTime = maxAfkTime;
        this.actions = actions;
    }

    public String getRegionName() {
        return regionName;
    }

    public long getMaxAfkTime() {
        return maxAfkTime;
    }

    public List<Map<String, String>> getActions() {
        return actions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionOverride that = (RegionOverride) o;
        return maxAfkTime == that.maxAfkTime &&
                Objects.equals(regionName, that.regionName) &&
                Objects.equals(actions, that.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regionName, maxAfkTime, actions);
    }

    @Override
    public String toString() {
        return "RegionOverride{" +
                "regionName='" + regionName + '\'' +
                ", maxAfkTime=" + maxAfkTime +
                ", actions=" + actions +
                '}';
    }
}