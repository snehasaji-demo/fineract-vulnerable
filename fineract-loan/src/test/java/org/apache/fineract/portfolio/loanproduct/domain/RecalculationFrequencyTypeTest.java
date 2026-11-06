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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.Test;

class RecalculationFrequencyTypeTest {

    @Test
    void testFromInt() {
        assertEquals(RecalculationFrequencyType.INVALID, RecalculationFrequencyType.fromInt(null));
        assertEquals(RecalculationFrequencyType.INVALID, RecalculationFrequencyType.fromInt(0));
        assertEquals(RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD, RecalculationFrequencyType.fromInt(1));
        assertEquals(RecalculationFrequencyType.DAILY, RecalculationFrequencyType.fromInt(2));
        assertEquals(RecalculationFrequencyType.WEEKLY, RecalculationFrequencyType.fromInt(3));
        assertEquals(RecalculationFrequencyType.MONTHLY, RecalculationFrequencyType.fromInt(4));
    }

    @Test
    void testIsSameFrequency() {
        assertTrue(RecalculationFrequencyType.DAILY.isSameFrequency(PeriodFrequencyType.DAYS));
        assertFalse(RecalculationFrequencyType.DAILY.isSameFrequency(PeriodFrequencyType.WEEKS));

        assertTrue(RecalculationFrequencyType.WEEKLY.isSameFrequency(PeriodFrequencyType.WEEKS));
        assertFalse(RecalculationFrequencyType.WEEKLY.isSameFrequency(PeriodFrequencyType.MONTHS));

        assertTrue(RecalculationFrequencyType.MONTHLY.isSameFrequency(PeriodFrequencyType.MONTHS));
        assertFalse(RecalculationFrequencyType.MONTHLY.isSameFrequency(PeriodFrequencyType.DAYS));

        assertFalse(RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD.isSameFrequency(PeriodFrequencyType.DAYS));
        assertFalse(RecalculationFrequencyType.INVALID.isSameFrequency(PeriodFrequencyType.DAYS));
    }
}
