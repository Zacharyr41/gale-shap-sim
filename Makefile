# Gale-Shapley Simulator Makefile

.PHONY: help build run test clean run-empty run-asymmetric run-stable

help:
	@echo "Gale-Shapley Simulator - Available commands:"
	@echo ""
	@echo "  make build        - Build the executable JAR"
	@echo "  make run          - Run with default config"
	@echo "  make run FILE=... - Run with custom YAML file"
	@echo "  make test         - Run all tests"
	@echo "  make clean        - Clean build artifacts"
	@echo ""
	@echo "Quick test scenarios:"
	@echo "  make run-empty    - Run empty set preference scenario"
	@echo "  make run-asymmetric - Run asymmetric matching scenario"  
	@echo "  make run-stable   - Run stable matching scenario"
	@echo ""
	@echo "Examples:"
	@echo "  make run FILE=my-config.yaml"
	@echo "  make run FILE=src/test/resources/empty-set-config.yaml"

build:
	@echo "Building Gale-Shapley simulator..."
	@mvn package -q

run:
	@mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="$(FILE)" -q

run-empty:
	@echo "Running empty set preference scenario..."
	@mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="src/test/resources/empty-set-config.yaml" -q

run-asymmetric:
	@echo "Running asymmetric matching scenario..."
	@mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="src/test/resources/asymmetric-matching-config.yaml" -q

run-stable:
	@echo "Running stable matching scenario..."
	@mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="src/test/resources/stable-matching-config.yaml" -q

test:
	@mvn test

clean:
	@mvn clean
	@echo "Clean complete."