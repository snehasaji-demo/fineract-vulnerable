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
package org.apache.fineract.portfolio.loanproduct.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;
import org.junit.jupiter.api.Test;

class LoanEnumerationsTest {

    @Test
    void testLoanEnumeration() {
        EnumOptionData data = LoanEnumerations.loanEnumeration("loanTermFrequencyType", 0);
        assertNotNull(data);
        assertEquals(0L, data.getId());

        data = LoanEnumerations.loanEnumeration("termFrequencyType", 1);
        assertNotNull(data);
        assertEquals(1L, data.getId());

        data = LoanEnumerations.loanEnumeration("repaymentStartDateType", 1);
        assertNotNull(data);
        assertEquals(1L, data.getId());

        assertNull(LoanEnumerations.loanEnumeration("nonExistentType", 1));
    }

    @Test
    void testRepaymentStartDateType() {
        EnumOptionData data = LoanEnumerations.repaymentStartDateType(1);
        assertNotNull(data);
        assertEquals(1L, data.getId());
        assertEquals("repaymentStartDateType.disbursementDate", data.getCode());

        EnumOptionData dataFromEnum = LoanEnumerations.repaymentStartDateType(RepaymentStartDateType.SUBMITTED_ON_DATE);
        assertNotNull(dataFromEnum);
        assertEquals(2L, dataFromEnum.getId());
        assertEquals("repaymentStartDateType.submittedOnDate", dataFromEnum.getCode());

        EnumOptionData invalidData = LoanEnumerations.repaymentStartDateType(RepaymentStartDateType.INVALID);
        assertNotNull(invalidData);
        assertEquals(0L, invalidData.getId());
        assertEquals("repaymentStartDateType.invalid", invalidData.getCode());
    }

    @Test
    void testLoanTermFrequencyType() {
        EnumOptionData data = LoanEnumerations.loanTermFrequencyType(0);
        assertNotNull(data);
        assertEquals(0L, data.getId());
        assertEquals("loanTermFrequency.periodFrequencyType.days", data.getCode());

        EnumOptionData dataFromEnum = LoanEnumerations.loanTermFrequencyType(PeriodFrequencyType.WEEKS);
        assertNotNull(dataFromEnum);
        assertEquals(1L, dataFromEnum.getId());
        assertEquals("loanTermFrequency.periodFrequencyType.weeks", dataFromEnum.getCode());
    }

    @Test
    void testTermFrequencyType() {
        EnumOptionData data = LoanEnumerations.termFrequencyType(2);
        assertNotNull(data);
        assertEquals(2L, data.getId());
        assertEquals("termFrequency.periodFrequencyType.months", data.getCode());

        EnumOptionData dataFromEnum = LoanEnumerations.termFrequencyType(PeriodFrequencyType.YEARS);
        assertNotNull(dataFromEnum);
        assertEquals(3L, dataFromEnum.getId());
        assertEquals("termFrequency.periodFrequencyType.years", dataFromEnum.getCode());
    }
}
