#!/bin/bash

echo "======================================"
echo "Correlated Preferences Demo"
echo "======================================"
echo ""
echo "This demo shows how the new correlated preference distribution works."
echo "Popular proposees (w1, w2) have higher weights and appear more frequently"
echo "in the top positions of proposer preference lists."
echo ""
echo "Running simulation with correlated preferences..."
echo ""

# Run the simulation with correlated preferences config
java -cp target/classes:target/test-classes com.galeshapley.Main src/test/resources/correlated-preferences-config.yaml

echo ""
echo "======================================"
echo "Demo Complete"
echo "======================================"
echo ""
echo "Note: With correlated preferences, certain agents (w1, w2) are more likely"
echo "to appear in top positions of preference orderings due to their popularity weights."
echo "This models real-world scenarios where some individuals are universally preferred."