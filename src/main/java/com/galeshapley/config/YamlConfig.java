package com.galeshapley.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class YamlConfig {
    
    @JsonProperty("simulation")
    private SimulationData simulation;
    
    public SimulationData getSimulation() {
        return simulation;
    }
    
    public void setSimulation(SimulationData simulation) {
        this.simulation = simulation;
    }
    
    public static class SimulationData {
        @JsonProperty("proposers")
        private List<AgentData> proposers;
        
        @JsonProperty("proposees")
        private List<AgentData> proposees;
        
        @JsonProperty("proposerPreferences")
        @JsonDeserialize(using = PreferenceMapDeserializer.class)
        private Map<String, PreferenceConfig> proposerPreferences;
        
        @JsonProperty("proposeePreferences")
        @JsonDeserialize(using = PreferenceMapDeserializer.class)
        private Map<String, PreferenceConfig> proposeePreferences;
        
        public List<AgentData> getProposers() {
            return proposers;
        }
        
        public void setProposers(List<AgentData> proposers) {
            this.proposers = proposers;
        }
        
        public List<AgentData> getProposees() {
            return proposees;
        }
        
        public void setProposees(List<AgentData> proposees) {
            this.proposees = proposees;
        }
        
        public Map<String, PreferenceConfig> getProposerPreferences() {
            return proposerPreferences;
        }
        
        public void setProposerPreferences(Map<String, PreferenceConfig> proposerPreferences) {
            this.proposerPreferences = proposerPreferences;
        }
        
        public Map<String, PreferenceConfig> getProposeePreferences() {
            return proposeePreferences;
        }
        
        public void setProposeePreferences(Map<String, PreferenceConfig> proposeePreferences) {
            this.proposeePreferences = proposeePreferences;
        }
    }
    
    public static class AgentData {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static YamlConfig loadFromFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(filePath), YamlConfig.class);
    }
    
    public static YamlConfig loadFromFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(file, YamlConfig.class);
    }
    
    public static YamlConfig loadFromStream(InputStream stream) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(stream, YamlConfig.class);
    }
    
    public static YamlConfig loadFromString(String yamlContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(yamlContent, YamlConfig.class);
    }
}