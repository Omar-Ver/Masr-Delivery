package com.masrdelivery.pricing;

import com.masrdelivery.exception.PlatformException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DistrictDistanceService {
    private static final Map<DistrictPair, Double> DISTANCE_MATRIX = new HashMap<>();

    static {
        // Pre-configured distances between Cairo districts
        setDistance("Maadi", "Faisal", 12.0); // Requirement worked example
        setDistance("Dokki", "Mohandessin", 3.0);
        setDistance("Dokki", "Faisal", 6.5);
        setDistance("Dokki", "Maadi", 14.0);
        setDistance("Nasr City", "Heliopolis", 5.0);
        setDistance("Nasr City", "Maadi", 18.0);
    }

    private static void setDistance(String d1, String d2, double distanceKm) {
        DISTANCE_MATRIX.put(new DistrictPair(d1, d2), distanceKm);
        DISTANCE_MATRIX.put(new DistrictPair(d2, d1), distanceKm);
    }

    public static double getDistanceKm(String fromDistrict, String toDistrict) {
        if (fromDistrict.equalsIgnoreCase(toDistrict)) {
            return 2.0; // Same-district local distance fallback
        }
        DistrictPair pair = new DistrictPair(fromDistrict, toDistrict);
        Double distance = DISTANCE_MATRIX.get(pair);
        if (distance == null) {
            // Default cross-district estimate if pair is not explicitly mapped
            return 10.0;
        }
        return distance;
    }

    private static class DistrictPair {
        private final String district1;
        private final String district2;

        public DistrictPair(String d1, String d2) {
            this.district1 = d1.trim().toLowerCase();
            this.district2 = d2.trim().toLowerCase();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DistrictPair that = (DistrictPair) o;
            return district1.equals(that.district1) && district2.equals(that.district2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(district1, district2);
        }
    }
}