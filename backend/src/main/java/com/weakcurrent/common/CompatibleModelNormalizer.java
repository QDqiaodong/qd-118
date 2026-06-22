package com.weakcurrent.common;

public class CompatibleModelNormalizer {

    private CompatibleModelNormalizer() {
    }

    public static String normalizeGroupName(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    public static String normalizeBrand(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    public static String normalizeModel(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    public static String normalizeSpec(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizeRemark(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizeRequired(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    public static boolean brandsEqual(String brand1, String brand2) {
        String norm1 = normalizeBrand(brand1);
        String norm2 = normalizeBrand(brand2);
        if (norm1 == null && norm2 == null) {
            return true;
        }
        if (norm1 == null || norm2 == null) {
            return false;
        }
        return norm1.equals(norm2);
    }

    public static boolean modelsEqual(String model1, String model2) {
        String norm1 = normalizeModel(model1);
        String norm2 = normalizeModel(model2);
        if (norm1 == null && norm2 == null) {
            return true;
        }
        if (norm1 == null || norm2 == null) {
            return false;
        }
        return norm1.equals(norm2);
    }

    public static boolean groupNamesEqual(String group1, String group2) {
        String norm1 = normalizeGroupName(group1);
        String norm2 = normalizeGroupName(group2);
        if (norm1 == null && norm2 == null) {
            return true;
        }
        if (norm1 == null || norm2 == null) {
            return false;
        }
        return norm1.equals(norm2);
    }
}
