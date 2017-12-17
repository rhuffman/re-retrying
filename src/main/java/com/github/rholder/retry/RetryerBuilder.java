/*
 * Copyright 2012-2015 Ray Holder
 * Modifications copyright 2017 Robert Huffman
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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A builder used to configure and create a {@link Retryer}.
 *
 * @author JB
 * @author Jason Dunkelberger (dirkraft)
 */
@SuppressWarnings("WeakerAccess")
public class RetryerBuilder {
    private AttemptTimeLimiter attemptTimeLimiter;
    private StopStrategy stopStrategy;
    private WaitStrategy waitStrategy;
    private BlockStrategy blockStrategy;
    private List<Predicate<Attempt<?>>> rejectionPredicates = Lists.newArrayList();
    private List<RetryListener> listeners = new ArrayList<>();

    private RetryerBuilder() {
    }

    public static RetryerBuilder newBuilder() {
        return new RetryerBuilder();
    }

    /**
     * Adds a listener that will be notified of each attempt that is made
     *
     * @param listener Listener to add
     * @return <code>this</code>
     */
    public RetryerBuilder withRetryListener(@Nonnull RetryListener listener) {
        Preconditions.checkNotNull(listener, "listener may not be null");
        listeners.add(listener);
        return this;
    }

    /**
     * Sets the wait strategy used to decide how long to sleep between failed attempts.
     * The default strategy is to retry immediately after a failed attempt.
     *
     * @param waitStrategy the strategy used to sleep between failed attempts
     * @return <code>this</code>
     * @throws IllegalStateException if a wait strategy has already been set.
     */
    public RetryerBuilder withWaitStrategy(@Nonnull WaitStrategy waitStrategy)
            throws IllegalStateException {
        Preconditions.checkNotNull(waitStrategy, "waitStrategy may not be null");
        Preconditions.checkState(this.waitStrategy == null,
                "a wait strategy has already been set %s", this.waitStrategy);
        this.waitStrategy = waitStrategy;
        return this;
    }

    /**
     * Sets the stop strategy used to decide when to stop retrying. The default strategy
     * is to not stop at all .
     *
     * @param stopStrategy the strategy used to decide when to stop retrying
     * @return <code>this</code>
     * @throws IllegalStateException if a stop strategy has already been set.
     */
    public RetryerBuilder withStopStrategy(@Nonnull StopStrategy stopStrategy)
            throws IllegalStateException {
        Preconditions.checkNotNull(stopStrategy, "stopStrategy may not be null");
        Preconditions.checkState(this.stopStrategy == null, "a stop strategy has already been set %s", this.stopStrategy);
        this.stopStrategy = stopStrategy;
        return this;
    }

    /**
     * Sets the block strategy used to decide how to block between retry attempts. The default strategy is to use Thread#sleep().
     *
     * @param blockStrategy the strategy used to decide how to block between retry attempts
     * @return <code>this</code>
     * @throws IllegalStateException if a block strategy has already been set.
     */
    public RetryerBuilder withBlockStrategy(@Nonnull BlockStrategy blockStrategy)
            throws IllegalStateException {
        Preconditions.checkNotNull(blockStrategy, "blockStrategy may not be null");
        Preconditions.checkState(this.blockStrategy == null,
                "a block strategy has already been set %s", this.blockStrategy);
        this.blockStrategy = blockStrategy;
        return this;
    }


    /**
     * Configures the retryer to limit the duration of any particular attempt by the given duration.
     *
     * @param attemptTimeLimiter to apply to each attempt
     * @return <code>this</code>
     */
    public RetryerBuilder withAttemptTimeLimiter(@Nonnull AttemptTimeLimiter attemptTimeLimiter) {
        Preconditions.checkNotNull(attemptTimeLimiter);
        this.attemptTimeLimiter = attemptTimeLimiter;
        return this;
    }

    /**
     * Configures the retryer to retry if an exception (i.e. any <code>Exception</code> or subclass
     * of <code>Exception</code>) is thrown by the call.
     *
     * @return <code>this</code>
     */
    public RetryerBuilder retryIfException() {
        rejectionPredicates.add(new ExceptionClassPredicate(Exception.class));
        return this;
    }

    /**
     * Configures the retryer to retry if a runtime exception (i.e. any <code>RuntimeException</code> or subclass
     * of <code>RuntimeException</code>) is thrown by the call.
     *
     * @return <code>this</code>
     */
    public RetryerBuilder retryIfRuntimeException() {
        rejectionPredicates.add(new ExceptionClassPredicate(RuntimeException.class));
        return this;
    }

    /**
     * Configures the retryer to retry if an exception of the given class (or subclass of the given class) is
     * thrown by the call.
     *
     * @param exceptionClass the type of the exception which should cause the retryer to retry
     * @return <code>this</code>
     */
    public RetryerBuilder retryIfExceptionOfType(@Nonnull Class<? extends Throwable> exceptionClass) {
        Preconditions.checkNotNull(exceptionClass, "exceptionClass may not be null");
        rejectionPredicates.add(new ExceptionClassPredicate(exceptionClass));
        return this;
    }

    /**
     * Configures the retryer to retry if an exception satisfying the given predicate is
     * thrown by the call.
     *
     * @param exceptionPredicate the predicate which causes a retry if satisfied
     * @return <code>this</code>
     */
    public RetryerBuilder retryIfException(@Nonnull Predicate<Throwable> exceptionPredicate) {
        Preconditions.checkNotNull(exceptionPredicate, "exceptionPredicate may not be null");
        rejectionPredicates.add(new ExceptionPredicate(exceptionPredicate));
        return this;
    }

    /**
     * Configures the retryer to retry if the result satisfies the given predicate.
     *
     * @param resultPredicate a predicate applied to the result, and which causes the retryer
     *                        to retry if the predicate is satisfied
     * @return <code>this</code>
     */
    public <T> RetryerBuilder retryIfResult(@Nonnull Predicate<T> resultPredicate) {
        Preconditions.checkNotNull(resultPredicate, "resultPredicate may not be null");
        rejectionPredicates.add(new ResultPredicate<>(resultPredicate));
        return this;
    }

    /**
     * Builds the retryer.
     *
     * @return the built retryer.
     */
    public Retryer build() {
        AttemptTimeLimiter theAttemptTimeLimiter = attemptTimeLimiter == null ? AttemptTimeLimiters.noTimeLimit() : attemptTimeLimiter;
        StopStrategy theStopStrategy = stopStrategy == null ? StopStrategies.neverStop() : stopStrategy;
        WaitStrategy theWaitStrategy = waitStrategy == null ? WaitStrategies.noWait() : waitStrategy;
        BlockStrategy theBlockStrategy = blockStrategy == null ? BlockStrategies.threadSleepStrategy() : blockStrategy;

        return new Retryer(theAttemptTimeLimiter, theStopStrategy, theWaitStrategy, theBlockStrategy, rejectionPredicates, listeners);
    }

    private static final class ExceptionClassPredicate implements Predicate<Attempt<?>> {

        private Class<? extends Throwable> exceptionClass;

        ExceptionClassPredicate(Class<? extends Throwable> exceptionClass) {
            this.exceptionClass = exceptionClass;
        }

        @Override
        public boolean test(Attempt<?> attempt) {
            return attempt.hasException() &&
                    exceptionClass.isAssignableFrom(attempt.getException().getClass());
        }
    }

    private static final class ResultPredicate<T> implements Predicate<Attempt<?>> {

        private Predicate<T> delegate;

        ResultPredicate(Predicate<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean test(Attempt<?> attempt) {
            if (!attempt.hasResult()) {
                return false;
            }
            try {
                @SuppressWarnings("unchecked")
                T result = (T) attempt.getResult();
                return delegate.test(result);
            } catch (ClassCastException e) {
                return false;
            }
        }
    }

    private static final class ExceptionPredicate implements Predicate<Attempt<?>> {

        private Predicate<Throwable> delegate;

        ExceptionPredicate(Predicate<Throwable> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean test(Attempt<?> attempt) {
            return attempt.hasException() && delegate.test(attempt.getException());
        }
    }
}
