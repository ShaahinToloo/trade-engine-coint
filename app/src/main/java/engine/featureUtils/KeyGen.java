package engine.featureUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class KeyGen {

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> components = new LinkedHashMap<>();

        public Builder type(String type) {
            components.put("type", type);
            return this;
        }

        public Builder source(String source) {
            components.put("source", source);
            return this;
        }

        public Builder source(Builder nestedBuilder) {
            components.put("source", nestedBuilder.build());
            return this;
        }

        public Builder target(String source) {
            components.put("target", source);
            return this;
        }

        public Builder target(Builder nestedBuilder) {
            components.put("target", nestedBuilder.build());
            return this;
        }

        public Builder period1(Number period) {
            components.put("period1", String.valueOf(period));
            return this;
        }

        public Builder period2(Number period) {
            components.put("period2", String.valueOf(period));
            return this;
        }

        public Builder period3(Number period) {
            components.put("period3", String.valueOf(period));
            return this;
        }

        public Builder period(Number period) {
            components.put("period", String.valueOf(period));
            return this;
        }

        public Builder param(String key, Object value) {
            if (value instanceof Builder) {
                components.put(key, ((Builder) value).build());
            } else {
                components.put(key, String.valueOf(value));
            }
            return this;
        }

        public String build() {
            return components.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.joining("|"));
        }
    }

    // Utility method to parse keys back into components (optional)
    public static Map<String, String> parseKey(String key) {
        Map<String, String> result = new LinkedHashMap<>();
        String[] pairs = key.split("\\|");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result;
    }
}
