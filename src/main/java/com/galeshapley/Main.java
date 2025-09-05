package com.galeshapley;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.observer.ConsoleObserver;
import com.galeshapley.observer.StatisticsObserver;

import java.io.File;
import java.io.IOException;

public class Main {
    
    public static void main(String[] args) {
        try {
            String configFile = "src/main/resources/example-config.yaml";
            
            if (args.length > 0) {
                configFile = args[0];
            }
            
            System.out.println("Loading configuration from: " + configFile);
            System.out.println();
            
            SimulationConfigLoader loader = new SimulationConfigLoader();
            SimulationConfig config = loader.loadFromFile(new File(configFile));
            
            GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
                config.getProposerPreferences(),
                config.getProposeePreferences()
            );
            
            ConsoleObserver consoleObserver = new ConsoleObserver(true);
            StatisticsObserver statisticsObserver = new StatisticsObserver();
            
            algorithm.addObserver(consoleObserver);
            algorithm.addObserver(statisticsObserver);
            
            GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
            
            System.out.println("\n=== Statistics ===");
            System.out.println(statisticsObserver.getStatistics());
            
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error running algorithm: " + e.getMessage());
            e.printStackTrace();
        }
    }
}