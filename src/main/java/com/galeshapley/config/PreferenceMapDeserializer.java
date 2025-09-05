package com.galeshapley.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom deserializer that handles both old format (direct lists) and new format (PreferenceConfig objects).
 * This provides backward compatibility while supporting the new nested preference structure.
 */
public class PreferenceMapDeserializer extends JsonDeserializer<Map<String, PreferenceConfig>> {
    
    @Override
    public Map<String, PreferenceConfig> deserialize(JsonParser parser, DeserializationContext context) 
            throws IOException {
        
        Map<String, PreferenceConfig> result = new HashMap<>();
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);
        
        var fields = node.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            String agentId = entry.getKey();
            JsonNode valueNode = entry.getValue();
            
            PreferenceConfig preferenceConfig;
            
            if (valueNode.isArray()) {
                // Old format: direct array of preferences
                List<String> preferences = mapper.convertValue(valueNode, 
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                preferenceConfig = PreferenceConfig.explicit(preferences);
            } else if (valueNode.isObject()) {
                // New format: PreferenceConfig object
                preferenceConfig = mapper.convertValue(valueNode, PreferenceConfig.class);
            } else {
                throw new IllegalArgumentException(
                    "Preference value for agent " + agentId + " must be either an array or an object");
            }
            
            result.put(agentId, preferenceConfig);
        }
        
        return result;
    }
}