/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.j2cl.junit.integration.junit4.data;

import com.google.j2cl.junit.integration.testlogger.TestCaseLogger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * TestCase used for integration testing for j2cl JUnit support.
 *
 * <p>Note this test will not pass and this is intentional since we want to test test failures in
 * our integration tests as well.
 */
@RunWith(JUnit4.class)
public class ThrowsInAfterTest {

  @After
  public void after() {
    TestCaseLogger.log("after");
    throw new RuntimeException("failure in after()");
  }

  @Test
  public void test() {
    TestCaseLogger.log("test");
  }

  @Test
  public void testOther() {
    TestCaseLogger.log("testOther");
  }

  @AfterClass
  public static void afterClass() {
    TestCaseLogger.log("afterClass");
  }
}
