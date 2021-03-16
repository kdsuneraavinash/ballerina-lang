/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.runtime.entity;

import org.jacoco.core.analysis.ICounter;

/**
 * Represents a counter that eliminates partially covered counter status and coverts it to fully covered status.
 *
 * @since 2.0.0
 */
public class PartialCoverageModifiedCounter implements ICounter {

    private int covered;
    private int missed;

    public PartialCoverageModifiedCounter(ICounter prevCounter) {
        this.covered = prevCounter.getCoveredCount();
        this.missed = prevCounter.getMissedCount();
        modifyCoverageNumbers();
    }

    /**
     * Modify the covered and missed numbers in cases the counter status is calculated as PARTLY_COVERED.
     * It converts the counter status to FULLY_COVERED.
     */
    private void modifyCoverageNumbers() {
        if (getStatus() == PARTLY_COVERED) {
            covered = covered + missed;
            missed = 0;
        }
    }

    /**
     * As implemented in Jacoco API.
     * org.jacoco.core.internal.analysis.CounterImpl#getValue(org.jacoco.core.analysis.ICounter.CounterValue)
     */
    @Override
    public double getValue(CounterValue value) {
        switch (value) {
            case TOTALCOUNT:
                return getTotalCount();
            case MISSEDCOUNT:
                return getMissedCount();
            case COVEREDCOUNT:
                return getCoveredCount();
            case MISSEDRATIO:
                return getMissedRatio();
            case COVEREDRATIO:
                return getCoveredRatio();
            default:
                throw new RuntimeException("No such CounterValue object");
        }
    }

    @Override
    public int getTotalCount() {
        return covered + missed;
    }

    @Override
    public int getCoveredCount() {
        return covered;
    }

    @Override
    public int getMissedCount() {
        return missed;
    }

    @Override
    public double getCoveredRatio() {
        return (double) covered / (missed + covered);
    }

    @Override
    public double getMissedRatio() {
        return (double) missed / (missed + covered);
    }

    /**
     * As implemented in org.jacoco.core.internal.analysis.CounterImpl#getStatus().
     */
    @Override
    public int getStatus() {
        int status = covered > 0 ? FULLY_COVERED : EMPTY;
        if (missed > 0) {
            status |= NOT_COVERED;
        }
        return status;
    }
}
