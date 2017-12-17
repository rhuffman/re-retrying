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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.junit.Assert.*;

class RetryerTest {

    @ParameterizedTest
    @MethodSource("checkedAndUnchecked")
    void testCallThrowsWithNoRetryOnException(Throwable throwable) throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder().build();
        Thrower thrower = new Thrower(throwable, 5);
        try {
            retryer.call(thrower);
            fail("Should have thrown");
        } catch (Throwable e) {
            assertSame(throwable, e);
        }
        assertEquals(1, thrower.invocations);
    }

    @ParameterizedTest
    @MethodSource("unchecked")
    void testRunThrowsWithNoRetryOnException(Throwable throwable) throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder().build();
        Thrower thrower = new Thrower(throwable, 5);
        try {
            retryer.run(thrower);
            fail("Should have thrown");
        } catch (Throwable e) {
            assertSame(throwable, e);
        }
        assertEquals(1, thrower.invocations);
    }

    @ParameterizedTest
    @MethodSource("checkedAndUnchecked")
    void testCallThrowsWithRetryOnException(Throwable throwable) throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(Throwable.class)
                .build();
        Thrower thrower = new Thrower(throwable, 5);
        retryer.call(thrower);
        assertEquals(5, thrower.invocations);
    }

    @ParameterizedTest
    @MethodSource("unchecked")
    void testRunThrowsWithRetryOnException(Throwable throwable) throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(Throwable.class)
                .build();
        Thrower thrower = new Thrower(throwable, 5);
        retryer.run(thrower);
        assertEquals(5, thrower.invocations);
    }

    @ParameterizedTest
    @MethodSource("checkedAndUnchecked")
    void testCallThrowsSubclassWithRetryOnException(Throwable throwable) throws Exception {
        @SuppressWarnings("unchecked")
        Class<? extends Throwable> superclass =
                (Class<? extends Throwable>) throwable.getClass().getSuperclass();
        Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(superclass)
                .build();
        Thrower thrower = new Thrower(throwable, 5);
        retryer.call(thrower);
        assertEquals(5, thrower.invocations);
    }

    @ParameterizedTest
    @MethodSource("unchecked")
    void testRunThrowsSubclassWithRetryOnException(Throwable throwable) throws Exception {
        @SuppressWarnings("unchecked")
        Class<? extends Throwable> superclass =
                (Class<? extends Throwable>) throwable.getClass().getSuperclass();
        Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(superclass)
                .build();
        Thrower thrower = new Thrower(throwable, 5);
        retryer.run(thrower);
        assertEquals(5, thrower.invocations);
    }

    @ParameterizedTest
    @MethodSource("checkedAndUnchecked")
    void testCallThrowsWhenRetriesAreStopped(Throwable throwable) throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(throwable.getClass())
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .build();
        Thrower thrower = new Thrower(throwable, 5);
        try {
            retryer.call(thrower);
            fail("Should have thrown");
        } catch (RetryException e) {
            assertSame(throwable, e.getCause());
            assertEquals(3, e.getNumberOfFailedAttempts());
        }
        assertEquals(3, thrower.invocations);
    }

    @ParameterizedTest
    @MethodSource("unchecked")
    void testRunThrowsWhenRetriesAreStopped(Throwable throwable) throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(throwable.getClass())
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .build();
        Thrower thrower = new Thrower(throwable, 5);
        try {
            retryer.call(thrower);
            fail("Should have thrown");
        } catch (RetryException e) {
            assertSame(throwable, e.getCause());
            assertEquals(3, e.getNumberOfFailedAttempts());
        }
        assertEquals(3, thrower.invocations);
    }

    private static Stream<Arguments> checkedAndUnchecked() {
        return Stream.concat(unchecked(), Stream.of(
                Arguments.of(new Exception("oops")),
                Arguments.of(new IOException("oops"))
        ));
    }

    private static Stream<Arguments> unchecked() {
        return Stream.of(
                Arguments.of(new Error("oops")),
                Arguments.of(new RuntimeException("oops")),
                Arguments.of(new NullPointerException("oops"))
        );
    }

    private class Thrower implements Callable<Void>, Runnable {

        private final Throwable throwable;

        private final int successAttempt;

        private int invocations = 0;

        Thrower(Throwable throwable, int successAttempt) {
            this.throwable = throwable;
            this.successAttempt = successAttempt;
        }

        @Override
        public Void call() throws Exception {
            invocations++;
            if (invocations == successAttempt) {
                return null;
            }
            if (throwable instanceof Error) {
                throw (Error) throwable;
            }
            throw (Exception) throwable;
        }

        @Override
        public void run() {
            invocations++;
            if (invocations == successAttempt) {
                return;
            }
            if (throwable instanceof Error) {
                throw (Error) throwable;
            }
            throw (RuntimeException) throwable;
        }

    }

}
