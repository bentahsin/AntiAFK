package com.bentahsin.antiafk.behavior.util;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;

/**
 * İki hareket yörüngesinin benzerliğini karşılaştırmak için statik yardımcı metotlar içerir.
 * Bu sınıf, davranış analizinin matematiksel temelini oluşturur.
 */
public final class TrajectoryComparator {

    /** Karşılaştırma için tüm yörüngelerin indirgeneceği standart nokta sayısı. */
    private static final int NORMALIZED_POINT_COUNT = 20;

    private TrajectoryComparator() {}

    /**
     * İki yörüngenin, verilen toleranslar dahilinde benzer olup olmadığını kontrol eder.
     *
     * @param ref                 Referans yörünge.
     * @param current             Karşılaştırılacak mevcut yörünge.
     * @param locationTolerance   Konumlar arasındaki maksimum kabul edilebilir mesafe (blok cinsinden).
     * @param directionTolerance  Bakış açıları arasındaki maksimum kabul edilebilir fark (derece cinsinden).
     * @param similarityThreshold Benzer sayılmak için gereken minimum eşleşme oranı (0.0 ile 1.0 arası).
     * @return Yörüngeler benzerse true, değilse false.
     */
    public static boolean areSimilar(List<Location> ref, List<Location> current, double locationTolerance, double directionTolerance, double similarityThreshold) {
        if (ref == null || ref.isEmpty() || current.isEmpty()) {
            return false;
        }

        List<Location> normalizedRef = normalize(ref);
        List<Location> normalizedCurrent = normalize(current);

        final double locationToleranceSquared = locationTolerance * locationTolerance;

        int matchCount = 0;
        for (int i = 0; i < NORMALIZED_POINT_COUNT; i++) {
            Location refPoint = normalizedRef.get(i);
            Location currentPoint = normalizedCurrent.get(i);

            double distanceSq = refPoint.distanceSquared(currentPoint);
            double angleDiff = getAngleDifference(refPoint.getYaw(), currentPoint.getYaw());

            if (distanceSq <= locationToleranceSquared && angleDiff <= directionTolerance) {
                matchCount++;
            }
        }

        double similarityScore = (double) matchCount / NORMALIZED_POINT_COUNT;
        return similarityScore >= similarityThreshold;
    }

    /**
     * Bir yörüngeyi standart sayıda (NORMALIZED_POINT_COUNT) eşit aralıklı noktaya indirger.
     * Bu, farklı uzunluktaki ve hızdaki yörüngeleri adil bir şekilde karşılaştırmayı sağlar.
     */
    private static List<Location> normalize(List<Location> original) {
        List<Location> normalized = new ArrayList<>();
        int originalSize = original.size();
        if (originalSize <= 1) {

            return new ArrayList<>(original);
        }

        for (int i = 0; i < NORMALIZED_POINT_COUNT; i++) {

            double index = (double) i / (NORMALIZED_POINT_COUNT - 1) * (originalSize - 1);
            int floor = (int) Math.floor(index);
            int ceil = (int) Math.ceil(index);

            if (floor == ceil) {

                normalized.add(original.get(floor));
            } else {

                double fraction = index - floor;
                Location loc1 = original.get(floor);
                Location loc2 = original.get(ceil);
                normalized.add(interpolate(loc1, loc2, fraction));
            }
        }
        return normalized;
    }

    /**
     * İki konum arasında, verilen orana göre yeni bir konum oluşturur (Lineer İnterpolasyon).
     */
    private static Location interpolate(Location loc1, Location loc2, double fraction) {
        double x = loc1.getX() + (loc2.getX() - loc1.getX()) * fraction;
        double y = loc1.getY() + (loc2.getY() - loc1.getY()) * fraction;
        double z = loc1.getZ() + (loc2.getZ() - loc1.getZ()) * fraction;

        float yaw = (float) (loc1.getYaw() + getAngleDifference(loc1.getYaw(), loc2.getYaw()) * fraction);
        float pitch = (float) (loc1.getPitch() + (loc2.getPitch() - loc1.getPitch()) * fraction);
        return new Location(loc1.getWorld(), x, y, z, yaw, pitch);
    }

    /**
     * İki açı arasındaki en kısa farkı (-180 ile +180 arasında) hesaplar.
     * Örneğin 350 ve 10 derece arasındaki fark 20 derecedir, 340 değil.
     */
    private static double getAngleDifference(double angle1, double angle2) {
        double diff = (angle2 - angle1 + 180) % 360 - 180;
        return diff < -180 ? diff + 360 : diff;
    }
}