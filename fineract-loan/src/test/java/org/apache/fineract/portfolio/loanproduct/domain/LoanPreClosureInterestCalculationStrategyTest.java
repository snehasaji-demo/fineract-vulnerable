/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanproduct.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoanPreClosureInterestCalculationStrategyTest {

    @Test
    void testFromInt() {
        assertEquals(LoanPreClosureInterestCalculationStrategy.NONE, LoanPreClosureInterestCalculationStrategy.fromInt(0));
        assertEquals(LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE, LoanPreClosureInterestCalculationStrategy.fromInt(1));
        assertEquals(LoanPreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE, LoanPreClosureInterestCalculationStrategy.fromInt(2));
        assertNull(LoanPreClosureInterestCalculationStrategy.fromInt(99));
    }

    @Test
    void testCalculateTillRestFrequencyEnabled() {
        assertTrue(LoanPreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE.calculateTillRestFrequencyEnabled());
        assertFalse(LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.calculateTillRestFrequencyEnabled());
        assertFalse(LoanPreClosureInterestCalculationStrategy.NONE.calculateTillRestFrequencyEnabled());
    }

    @Test
    void testCalculateTillPreClosureDateEnabled() {
        assertTrue(LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.calculateTillPreClosureDateEnabled());
        assertFalse(LoanPreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE.calculateTillPreClosureDateEnabled());
        assertFalse(LoanPreClosureInterestCalculationStrategy.NONE.calculateTillPreClosureDateEnabled());
    }
}
