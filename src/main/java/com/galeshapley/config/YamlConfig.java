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
        
        @JsonProperty("proposerConfig")
        private GlobalAgentConfig proposerConfig;
        
        @JsonProperty("proposeeConfig")
        private GlobalAgentConfig proposeeConfig;
        
        @JsonProperty("proposerOverrides")
        private List<AgentOverride> proposerOverrides;
        
        @JsonProperty("proposeeOverrides")
        private List<AgentOverride> proposeeOverrides;
        
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
        
        public GlobalAgentConfig getProposerConfig() {
            return proposerConfig;
        }
        
        public void setProposerConfig(GlobalAgentConfig proposerConfig) {
            this.proposerConfig = proposerConfig;
        }
        
        public GlobalAgentConfig getProposeeConfig() {
            return proposeeConfig;
        }
        
        public void setProposeeConfig(GlobalAgentConfig proposeeConfig) {
            this.proposeeConfig = proposeeConfig;
        }
        
        public List<AgentOverride> getProposerOverrides() {
            return proposerOverrides;
        }
        
        public void setProposerOverrides(List<AgentOverride> proposerOverrides) {
            this.proposerOverrides = proposerOverrides;
        }
        
        public List<AgentOverride> getProposeeOverrides() {
            return proposeeOverrides;
        }
        
        public void setProposeeOverrides(List<AgentOverride> proposeeOverrides) {
            this.proposeeOverrides = proposeeOverrides;
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
    
    public static class GlobalAgentConfig {
        @JsonProperty("count")
        private int count;
        
        @JsonProperty("generator")
        private GeneratorConfig generator;
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public GeneratorConfig getGenerator() {
            return generator;
        }
        
        public void setGenerator(GeneratorConfig generator) {
            this.generator = generator;
        }
    }
    
    public static class GeneratorConfig {
        @JsonProperty("distribution")
        private com.galeshapley.config.distribution.DistributionConfig distribution;
        
        public com.galeshapley.config.distribution.DistributionConfig getDistribution() {
            return distribution;
        }
        
        public void setDistribution(com.galeshapley.config.distribution.DistributionConfig distribution) {
            this.distribution = distribution;
        }
        
        // For backwards compatibility during migration
        public double getEmptySetProbability() {
            return distribution != null ? distribution.getEmptySetProbability() : 0.0;
        }
    }
    
    public static class AgentOverride {
        @JsonProperty("index")
        private Integer index;
        
        @JsonProperty("range")
        private RangeConfig range;
        
        @JsonProperty("generator")
        private GeneratorConfig generator;
        
        public Integer getIndex() {
            return index;
        }
        
        public void setIndex(Integer index) {
            this.index = index;
        }
        
        public RangeConfig getRange() {
            return range;
        }
        
        public void setRange(RangeConfig range) {
            this.range = range;
        }
        
        public GeneratorConfig getGenerator() {
            return generator;
        }
        
        public void setGenerator(GeneratorConfig generator) {
            this.generator = generator;
        }
    }
    
    public static class RangeConfig {
        @JsonProperty("start")
        private int start;
        
        @JsonProperty("end")
        private int end;
        
        public int getStart() {
            return start;
        }
        
        public void setStart(int start) {
            this.start = start;
        }
        
        public int getEnd() {
            return end;
        }
        
        public void setEnd(int end) {
            this.end = end;
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