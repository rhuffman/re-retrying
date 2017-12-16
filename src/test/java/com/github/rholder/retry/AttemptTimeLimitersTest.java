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

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class AttemptTimeLimitersTest {

    @Test
    public void testFixedTimeLimitWithNoExecutorReusesThreads() throws Exception {
        Set<Long> threadsUsed = Collections.synchronizedSet(Sets.newHashSet());
        Callable<Void> callable = () -> {
            threadsUsed.add(Thread.currentThread().getId());
            return null;
        };

        int iterations = 20;
        for (int i = 0; i < iterations; i++) {
            AttemptTimeLimiter timeLimiter =
                AttemptTimeLimiters.fixedTimeLimit(1, TimeUnit.SECONDS);
            timeLimiter.call(callable);
        }
        assertTrue("Should have used less than " + iterations +
            " threads", threadsUsed.size() < iterations);
    }

}