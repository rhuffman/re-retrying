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

import static org.junit.Assert.*;

public class RetryerTest {

    @Test
    public void testErrorWithCallable() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder().build();
        Thrower thrower = new Thrower(new Error("oops"), 5);
        try {
            retryer.call(thrower);
            fail("Should have thrown");
        } catch (Error e) {
            assertSame(thrower.throwable, e);
        }
        assertEquals(1, thrower.invocations);
    }

    @Test
    public void testRuntimeExceptionWithCallable() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder().build();
        Thrower thrower = new Thrower(new RuntimeException("oops"), 5);
        try {
            retryer.call(thrower);
            fail("Should have thrown");
        } catch (RuntimeException e) {
            assertSame(thrower.throwable, e);
        }
        assertEquals(1, thrower.invocations);
    }

    @Test
    public void testCheckedExceptionWithCallable() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder().build();
        Thrower thrower = new Thrower(new Exception("oops"), 5);
        try {
            retryer.call(thrower);
            fail("Should have thrown an exception");
        } catch (Exception e) {
            assertSame(thrower.throwable, e);
        }
        assertEquals(1, thrower.invocations);
    }

    @Test
    public void testRetryOnErrorWithCallable() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(Error.class)
                .build();
        Thrower thrower = new Thrower(new Error("oops"), 5);
        retryer.call(thrower);
        assertEquals(5, thrower.invocations);
    }

    @Test
    public void testRetryOnRuntimeExceptionWithCallable() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(RuntimeException.class)
                .build();
        Thrower thrower = new Thrower(new RuntimeException("oops"), 5);
        retryer.call(thrower);
        assertEquals(5, thrower.invocations);
    }

    @Test
    public void testRetryOnCheckedExceptionWithCallable() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .build();
        Thrower thrower = new Thrower(new Exception("oops"), 5);
        retryer.call(thrower);
        assertEquals(5, thrower.invocations);
    }

    @Test
    public void testRetryOnSubclassOfCheckedExceptionWithCallable()
            throws Exception { Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .build();
        NullPointerException toThrow = new NullPointerException("oops");
        Thrower callable = new Thrower(toThrow, 5);
        retryer.call(callable);
        assertEquals(5, callable.invocations);
    }

    @Test
    public void testErrorWithRunnable() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder().build();
        Thrower thrower = new Thrower(new Error("oops"), 5);
        try {
            retryer.run(thrower);
            fail("Should have thrown");
        } catch (Error e) {
            assertSame(thrower.throwable, e);
        }
        assertEquals(1, thrower.invocations);
    }

    @Test
    public void testRuntimeExceptionWithRunnable() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder().build();
        Thrower thrower = new Thrower(new RuntimeException("oops"), 5);
        try {
            retryer.run(thrower);
            fail("Should have thrown");
        } catch (RuntimeException e) {
            assertSame(thrower.throwable, e);
        }
        assertEquals(1, thrower.invocations);
    }

    @Test
    public void testRetryOnErrorWithRunnable() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(Error.class)
                .build();
        Thrower thrower = new Thrower(new Error("oops"), 5);
        retryer.run(thrower);
        assertEquals(5, thrower.invocations);
    }

    @Test
    public void testRetryOnRuntimeExceptionWithRunnable() throws Exception {
        Retryer retryer = RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(RuntimeException.class)
                .build();
        Thrower thrower = new Thrower(new RuntimeException("oops"), 5);
        retryer.run(thrower);
        assertEquals(5, thrower.invocations);
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
