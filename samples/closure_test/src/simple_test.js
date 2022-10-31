/**
 * @fileoverview Description of this file.
 */

goog.module('simple_test_object.SimpleTest');
goog.setTestOnly();

var add = goog.require('simple_library.add');

goog.require('goog.testing.asserts');
goog.require('goog.testing.jsunit');

class SimpleTest{
  constructor() {
    this.num = 0;
  }

  testSimpleEqual1() {
    assertEquals(2, add(1, 1));
  }

  testSimpleEqual2() {
    assertEquals(2, add(1, 1));
  }
}

exports = SimpleTest;
