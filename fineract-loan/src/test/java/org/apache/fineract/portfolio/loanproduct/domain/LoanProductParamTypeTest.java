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

import org.junit.jupiter.api.Test;

class LoanProductParamTypeTest {

    @Test
    void testFromInt() {
        assertEquals(LoanProductParamType.INVALID, LoanProductParamType.fromInt(null));
        assertEquals(LoanProductParamType.INVALID, LoanProductParamType.fromInt(0));
        assertEquals(LoanProductParamType.PRINCIPAL, LoanProductParamType.fromInt(1));
        assertEquals(LoanProductParamType.INTERESTRATE, LoanProductParamType.fromInt(2));
        assertEquals(LoanProductParamType.REPAYMENT, LoanProductParamType.fromInt(3));
        assertEquals(LoanProductParamType.INVALID, LoanProductParamType.fromInt(99));
    }

    @Test
    void testIsParamTypePrincipal() {
        assertTrue(LoanProductParamType.PRINCIPAL.isParamTypePrincipal());
        assertFalse(LoanProductParamType.INTERESTRATE.isParamTypePrincipal());
    }

    @Test
    void testIsParamTypeInterestRate() {
        assertTrue(LoanProductParamType.INTERESTRATE.isParamTypeInterestTate());
        assertFalse(LoanProductParamType.REPAYMENT.isParamTypeInterestTate());
    }

    @Test
    void testIsParamTypeRepayment() {
        assertTrue(LoanProductParamType.REPAYMENT.isParamTypeRepayment());
        assertFalse(LoanProductParamType.PRINCIPAL.isParamTypeRepayment());
    }
}
