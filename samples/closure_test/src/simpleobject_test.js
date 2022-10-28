/**
 * @fileoverview Description of this file.
 */

goog.module('simple_test_object.SimpleTestObject');
goog.setTestOnly();

goog.require('goog.testing.asserts');
goog.require('goog.testing.jsunit');


class SimpleTestObject {
  constructor() {
    this.num = 0;
  }

  testSimpleEqual() {
    assertEquals(2, 1 + 1);
  }
}

exports = SimpleTestObject;
