/*
 * Copyright 2017 Robert Huffman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rholder.retry;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * An exception indicating that none of the attempts of the {@link Retryer}
 * succeeded. This is thrown only if the Retryer has a result predicate and
 * the result predicate failed on the last attempt.
 */
@SuppressWarnings("WeakerAccess")
@Immutable
public final class UncheckedRetryException extends Exception {

    private final int numberOfFailedAttempts;
    private final Attempt<?> lastFailedAttempt;

    /**
     * If the last {@link Attempt} had an Exception, ensure it is available in
     * the stack trace.
     *
     * @param numberOfFailedAttempts times we've tried and failed
     * @param lastFailedAttempt      what happened the last time we failed
     */
    UncheckedRetryException(int numberOfFailedAttempts, @Nonnull Attempt<?> lastFailedAttempt) {
        super("Retrying failed to complete successfully after " + numberOfFailedAttempts + " attempts.");
        this.numberOfFailedAttempts = numberOfFailedAttempts;
        this.lastFailedAttempt = lastFailedAttempt;
    }

    /**
     * Returns the number of failed attempts
     *
     * @return the number of failed attempts
     */
    public int getNumberOfFailedAttempts() {
        return numberOfFailedAttempts;
    }

    /**
     * Returns the last failed attempt
     *
     * @return the last failed attempt
     */
    public Attempt<?> getLastFailedAttempt() {
        return lastFailedAttempt;
    }
}
