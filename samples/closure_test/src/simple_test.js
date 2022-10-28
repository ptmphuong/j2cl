/**
 * @fileoverview Description of this file.
 */

goog.module('simple_test_object.SimpleTest');
goog.setTestOnly();

goog.require('goog.testing.asserts');
goog.require('goog.testing.jsunit');


class SimpleTest{
  constructor() {
    this.num = 0;
  }

  testSimpleEqual1() {
    assertEquals(2, 1 + 1);
  }

  testSimpleEqual2() {
    assertEquals(2, 1 + 1);
  }
}

exports = SimpleTest;
