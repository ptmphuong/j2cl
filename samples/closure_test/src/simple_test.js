goog.module('simple_test');
goog.setTestOnly('simple_test');

var testSuite = goog.require('goog.testing.testSuite');

goog.require('goog.testing.asserts');
goog.require('goog.testing.jsunit');

class SimpleTest {
  constructor() {
    this.num = 0;
  }

  testSimpleEqual() {
    assertEquals(2, 1 + 1);
  }
}

testSuite(new SimpleTest());
