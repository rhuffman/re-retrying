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

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class RetryerTest {

    @Test
    public void testError() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .withStopStrategy(StopStrategies.stopAfterAttempt(2))
                .build();
        Error toThrow = new Error("oops");
        ThrowingCallable callable = new ThrowingCallable(toThrow, 2);
        try {
            retryer.call(callable);
            fail("Should have thrown");
        } catch (Error e) {
            assertSame(toThrow, e);
        }
        assertEquals(1, callable.invocations);
    }

    @Test
    public void testRetryOnError() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .withStopStrategy(StopStrategies.stopAfterAttempt(2))
                .retryIfExceptionOfType(Error.class)
                .build();
        Error toThrow = new Error("oops");
        ThrowingCallable callable = new ThrowingCallable(toThrow, 2);
        retryer.call(callable);
        assertEquals(2, callable.invocations);
    }

    @Test
    public void testRuntimeException() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .withStopStrategy(StopStrategies.stopAfterAttempt(2))
                .build();
        RuntimeException toThrow = new RuntimeException("oops");
        ThrowingCallable callable = new ThrowingCallable(toThrow, 2);
        try {
            retryer.call(callable);
            fail("Should have thrown");
        } catch (RuntimeException e) {
            assertSame(toThrow, e);
        }
        assertEquals(1, callable.invocations);
    }

    @Test
    public void testRetryOnRuntimeException() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .withStopStrategy(StopStrategies.stopAfterAttempt(2))
                .retryIfExceptionOfType(RuntimeException.class)
                .build();
        RuntimeException toThrow = new RuntimeException("oops");
        ThrowingCallable callable = new ThrowingCallable(toThrow, 2);
        retryer.call(callable);
        assertEquals(2, callable.invocations);
    }

    @Test
    public void testCheckedException() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .withStopStrategy(StopStrategies.stopAfterAttempt(2))
                .build();
        Exception toThrow = new Exception("oops");
        ThrowingCallable callable = new ThrowingCallable(toThrow, 2);
        try {
            retryer.call(callable);
        } catch (Exception e) {
            assertSame(toThrow, e);
        }
        assertEquals(1, callable.invocations);
    }

    @Test
    public void testRetryOnCheckedException() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .withStopStrategy(StopStrategies.stopAfterAttempt(2))
                .retryIfExceptionOfType(Exception.class)
                .build();
        Exception toThrow = new Exception("oops");
        ThrowingCallable callable = new ThrowingCallable(toThrow, 2);
        try {
            retryer.call(callable);
        } catch (ExecutionException e) {
            assertSame(toThrow, e.getCause());
        }
        assertEquals(2, callable.invocations);
    }

    @Test
    public void testRetryOnSubclassOfCheckedException() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .withStopStrategy(StopStrategies.stopAfterAttempt(2))
                .retryIfExceptionOfType(Exception.class)
                .build();
        NullPointerException toThrow = new NullPointerException("oops");
        ThrowingCallable callable = new ThrowingCallable(toThrow, 2);
        try {
            retryer.call(callable);
        } catch (ExecutionException e) {
            assertSame(toThrow, e.getCause());
        }
        assertEquals(2, callable.invocations);
    }

    private class ThrowingCallable implements Callable<Void> {

        private final Throwable throwable;

        private final int successAttempt;

        private int invocations = 0;

        ThrowingCallable(Throwable throwable, int successAttempt) {
            this.throwable = throwable;
            this.successAttempt = successAttempt;
        }

        @Override
        public Void call() throws Exception {
            if (invocations == Integer.MAX_VALUE) {
                throw new RuntimeException("Already invoked the maximum number of times");
            }
            invocations++;
            if (invocations == successAttempt) {
                return null;
            }
            if (throwable instanceof Error) {
                throw (Error)throwable;
            }
            throw (Exception)throwable;
        }
    }

}
