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

class InterestRecalculationCompoundingMethodTest {

    @Test
    void testFromInt() {
        assertEquals(InterestRecalculationCompoundingMethod.NONE, InterestRecalculationCompoundingMethod.fromInt(0));
        assertEquals(InterestRecalculationCompoundingMethod.INTEREST, InterestRecalculationCompoundingMethod.fromInt(1));
        assertEquals(InterestRecalculationCompoundingMethod.FEE, InterestRecalculationCompoundingMethod.fromInt(2));
        assertEquals(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE, InterestRecalculationCompoundingMethod.fromInt(3));
        assertNull(InterestRecalculationCompoundingMethod.fromInt(99));
    }

    @Test
    void testCompoundingEnabled() {
        assertFalse(InterestRecalculationCompoundingMethod.NONE.isCompoundingEnabled());
        assertTrue(InterestRecalculationCompoundingMethod.INTEREST.isCompoundingEnabled());
        assertTrue(InterestRecalculationCompoundingMethod.FEE.isCompoundingEnabled());
        assertTrue(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE.isCompoundingEnabled());
    }

    @Test
    void testFeeCompoundingEnabled() {
        assertFalse(InterestRecalculationCompoundingMethod.NONE.isFeeCompoundingEnabled());
        assertFalse(InterestRecalculationCompoundingMethod.INTEREST.isFeeCompoundingEnabled());
        assertTrue(InterestRecalculationCompoundingMethod.FEE.isFeeCompoundingEnabled());
        assertTrue(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE.isFeeCompoundingEnabled());
    }

    @Test
    void testInterestCompoundingEnabled() {
        assertFalse(InterestRecalculationCompoundingMethod.NONE.isInterestCompoundingEnabled());
        assertTrue(InterestRecalculationCompoundingMethod.INTEREST.isInterestCompoundingEnabled());
        assertFalse(InterestRecalculationCompoundingMethod.FEE.isInterestCompoundingEnabled());
        assertTrue(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE.isInterestCompoundingEnabled());
    }
}
