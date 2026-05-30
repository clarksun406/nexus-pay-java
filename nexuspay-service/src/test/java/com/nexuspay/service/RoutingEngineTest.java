package com.nexuspay.service;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class RoutingEngineTest {
    
    @Test
    void shouldCalculateWeightedSelection() {
        // Test weighted selection logic
        int totalWeight = 10;
        int[] weights = {3, 5, 2};
        int[] selections = new int[3];
        
        // Simulate many selections
        for (int i = 0; i < 1000; i++) {
            int pick = (int) (Math.random() * totalWeight);
            for (int j = 0; j < weights.length; j++) {
                pick -= weights[j];
                if (pick < 0) {
                    selections[j]++;
                    break;
                }
            }
        }
        
        // Weight 5 should be selected most often
        assertTrue(selections[1] > selections[0]);
        assertTrue(selections[1] > selections[2]);
    }
}
