/*
 * My simple custom tests to quickly check during development
 */
package com.google.j2cl.samples.helloworldlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/*
 * A simple passing test.
 */
@RunWith(JUnit4.class)
public class SimpleTest {

  @Test
  public void testAdd() {
    assertEquals(1, 1);
  }

  @Test
  public void testAddFailed() {
    assertEquals(2, 1);
  }

  @Test
  public void testAddFailedLast() {
    assertEquals(5, 1);
  }
}
