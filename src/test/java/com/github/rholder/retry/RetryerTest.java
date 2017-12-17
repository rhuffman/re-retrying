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
  void testCallThrowsWithNoRetryOnException(Class<? extends Throwable> throwable) throws Exception {
    Retryer retryer = RetryerBuilder.newBuilder().build();
    Thrower thrower = new Thrower(throwable, 5);
    try {
      retryer.call(thrower);
      fail("Should have thrown");
    } catch (Throwable e) {
      assertSame(throwable, e.getClass());
    }
    assertEquals(1, thrower.invocations);
  }

  @ParameterizedTest
  @MethodSource("unchecked")
  void testRunThrowsWithNoRetryOnException(Class<? extends Throwable> throwable) throws Exception {
    Retryer retryer = RetryerBuilder.newBuilder().build();
    Thrower thrower = new Thrower(throwable, 5);
    try {
      retryer.run(thrower);
      fail("Should have thrown");
    } catch (Throwable e) {
      assertSame(throwable, e.getClass());
    }
    assertEquals(1, thrower.invocations);
  }

  @ParameterizedTest
  @MethodSource("checkedAndUnchecked")
  void testCallThrowsWithRetryOnException(Class<? extends Throwable> throwable) throws Exception {
    Retryer retryer = RetryerBuilder.newBuilder()
        .retryIfExceptionOfType(Throwable.class)
        .build();
    Thrower thrower = new Thrower(throwable, 5);
    retryer.call(thrower);
    assertEquals(5, thrower.invocations);
  }

  @ParameterizedTest
  @MethodSource("unchecked")
  void testRunThrowsWithRetryOnException(Class<? extends Throwable> throwable) throws Exception {
    Retryer retryer = RetryerBuilder.newBuilder()
        .retryIfExceptionOfType(Throwable.class)
        .build();
    Thrower thrower = new Thrower(throwable, 5);
    retryer.run(thrower);
    assertEquals(5, thrower.invocations);
  }

  @ParameterizedTest
  @MethodSource("checkedAndUnchecked")
  void testCallThrowsSubclassWithRetryOnException(Class<? extends Throwable> throwable) throws Exception {
    @SuppressWarnings("unchecked")
    Class<? extends Throwable> superclass = (Class<? extends Throwable>) throwable.getSuperclass();
    Retryer retryer = RetryerBuilder.newBuilder()
        .retryIfExceptionOfType(superclass)
        .build();
    Thrower thrower = new Thrower(throwable, 5);
    retryer.call(thrower);
    assertEquals(5, thrower.invocations);
  }

  @ParameterizedTest
  @MethodSource("unchecked")
  void testRunThrowsSubclassWithRetryOnException(Class<? extends Throwable> throwable) throws Exception {
    @SuppressWarnings("unchecked")
    Class<? extends Throwable> superclass = (Class<? extends Throwable>) throwable.getSuperclass();
    Retryer retryer = RetryerBuilder.newBuilder()
        .retryIfExceptionOfType(superclass)
        .build();
    Thrower thrower = new Thrower(throwable, 5);
    retryer.run(thrower);
    assertEquals(5, thrower.invocations);
  }

  @ParameterizedTest
  @MethodSource("checkedAndUnchecked")
  void testCallThrowsWhenRetriesAreStopped(Class<? extends Throwable> throwable) throws Exception {
    Retryer retryer = RetryerBuilder.newBuilder()
        .retryIfExceptionOfType(throwable)
        .withStopStrategy(StopStrategies.stopAfterAttempt(3))
        .build();
    Thrower thrower = new Thrower(throwable, 5);
    try {
      retryer.call(thrower);
      fail("Should have thrown");
    } catch (Throwable t) {
      assertSame(throwable, t.getClass());
    }
    assertEquals(3, thrower.invocations);
  }

  @ParameterizedTest
  @MethodSource("unchecked")
  void testRunThrowsWhenRetriesAreStopped(Class<? extends Throwable> throwable) throws Exception {
    Retryer retryer = RetryerBuilder.newBuilder()
        .retryIfExceptionOfType(throwable)
        .withStopStrategy(StopStrategies.stopAfterAttempt(3))
        .build();
    Thrower thrower = new Thrower(throwable, 5);
    try {
      retryer.run(thrower);
      fail("Should have thrown");
    } catch (Throwable t) {
      assertSame(throwable, t.getClass());
    }
    assertEquals(3, thrower.invocations);
  }

  private static Stream<Arguments> checkedAndUnchecked() {
    return Stream.concat(unchecked(), Stream.of(
        Arguments.of(Exception.class),
        Arguments.of(IOException.class)
    ));
  }

  private static Stream<Arguments> unchecked() {
    return Stream.of(
        Arguments.of(Error.class),
        Arguments.of(RuntimeException.class),
        Arguments.of(NullPointerException.class)
    );
  }

  private class Thrower implements Callable<Void>, Runnable {

    private final Class<? extends Throwable> throwableType;

    private final int successAttempt;

    private int invocations = 0;

    Thrower(Class<? extends Throwable> throwableType, int successAttempt) {
      this.throwableType = throwableType;
      this.successAttempt = successAttempt;
    }

    @Override
    public Void call() throws Exception {
      invocations++;
      if (invocations == successAttempt) {
        return null;
      }
      if (Error.class.isAssignableFrom(throwableType)) {
        throw (Error) throwable();
      }
      throw (Exception) throwable();
    }

    @Override
    public void run() {
      invocations++;
      if (invocations == successAttempt) {
        return;
      }
      if (Error.class.isAssignableFrom(throwableType)) {
        throw (Error) throwable();
      }
      throw (RuntimeException) throwable();
    }

    private Throwable throwable() {
      try {
        return throwableType.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException("Failed to create throwable of type " + throwableType);
      }
    }
  }
}

