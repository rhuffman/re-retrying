/*
 * Copyright 2012-2015 Ray Holder
 * Modifications copyright 2017-2018 Robert Huffman
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

import static com.google.common.base.Preconditions.checkState;

/**
 * An attempt of a call, which resulted either in a result returned by the call,
 * or in a Throwable thrown by the call.
 *
 * @param <T> The type returned by the wrapped callable.
 * @author JB
 */
@SuppressWarnings("WeakerAccess")
public class Attempt<T> {

    private final T result;

    private final Throwable throwable;

    private final int attemptNumber;

    private final long delaySinceFirstAttempt;

    Attempt(T result, int attemptNumber, long delaySinceFirstAttempt) {
        this.result = result;
        this.throwable = null;
        this.attemptNumber = attemptNumber;
        this.delaySinceFirstAttempt = delaySinceFirstAttempt;
    }

    Attempt(Throwable throwable, int attemptNumber, long delaySinceFirstAttempt) {
        this.result = null;
        this.throwable = throwable;
        this.attemptNumber = attemptNumber;
        this.delaySinceFirstAttempt = delaySinceFirstAttempt;
    }

    /**
     * Returns the result of the attempt, if any.
     *
     * @return the result of the attempt
     * @throws IllegalStateException If the attempt resulted in an exception rather
     *         than returning a result.
     */
    public T get() {
        checkState(hasResult(), "The attempt resulted in an exception, not in a result");
        return result;
    }

    /**
     * Tells if the call returned a result or not
     *
     * @return <code>true</code> if the call returned a result, <code>false</code>
     *         if it threw an exception
     */
    public boolean hasResult() {
        // Check the exception field, because the Callable may have succeeded and returned null.
        // In that case both exception and result will be null.
        return throwable == null;
    }

    /**
     * Tells if the call threw an exception or not
     *
     * @return <code>true</code> if the call threw an exception, <code>false</code>
     *         if it returned a result
     */
    public boolean hasException() {
        return throwable != null;
    }

    /**
     * Gets the result of the call
     *
     * @return the result of the call
     * @throws IllegalStateException if the call didn't return a result, but threw an exception,
     *                               as indicated by {@link #hasResult()}
     */
    public T getResult() throws IllegalStateException {
        return get();
    }

    /**
     * Gets the exception thrown by the call
     *
     * @return the exception thrown by the call
     * @throws IllegalStateException if the call didn't throw an exception,
     *                               as indicated by {@link #hasException()}
     */
    public Throwable getException() throws IllegalStateException {
        checkState(hasException(), "The attempt resulted in a result, not in an exception");
        return throwable;
    }

    /**
     * The number, starting from 1, of this attempt.
     *
     * @return the attempt number
     */
    public int getAttemptNumber() {
        return attemptNumber;
    }

    /**
     * The delay since the start of the first attempt, in milliseconds.
     *
     * @return the delay since the start of the first attempt, in milliseconds
     */
    public long getDelaySinceFirstAttempt() {
        return delaySinceFirstAttempt;
    }
}
