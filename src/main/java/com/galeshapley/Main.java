package com.galeshapley;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.config.RuntimeOptions;
import com.galeshapley.observer.ConsoleObserver;
import com.galeshapley.observer.StatisticsObserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
@EnableConfigurationProperties(RuntimeOptions.class)
public class Main {
    
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        
        try {
            RuntimeOptions runtimeOptions = context.getBean(RuntimeOptions.class);
            System.out.println("Runtime Options: " + runtimeOptions);
            
            String configFile = "src/main/resources/example-config.yaml";
            
            // Check for non-Spring arguments (config file path)
            for (String arg : args) {
                if (!arg.startsWith("--")) {
                    configFile = arg;
                    break;
                }
            }
            
            System.out.println("Loading configuration from: " + configFile);
            System.out.println();
            
            SimulationConfigLoader loader = new SimulationConfigLoader();
            SimulationConfig config = loader.loadFromFile(new File(configFile));
            
            GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
                config.getProposerPreferences(),
                config.getProposeePreferences(),
                config.getEmptySetPreferences()
            );
            
            ConsoleObserver consoleObserver = new ConsoleObserver(runtimeOptions.isDetailedLoggingEnabled());
            StatisticsObserver statisticsObserver = new StatisticsObserver();
            
            algorithm.addObserver(consoleObserver);
            algorithm.addObserver(statisticsObserver);
            
            GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute(runtimeOptions);
            
            if (runtimeOptions.isTrackIterationMetrics()) {
                System.out.println("\n=== Statistics ===");
                System.out.println(statisticsObserver.getStatistics());
            }
            
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error running algorithm: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
        }
    }
}