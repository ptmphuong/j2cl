goog.module('simple_testsuite');
goog.setTestOnly();

const ObjectWrapper = goog.require('simple_test_object.SimpleTest');
const testSuite = goog.require('goog.testing.testSuite');

goog.require('goog.testing.asserts');
goog.require('goog.testing.jsunit');

testSuite(new ObjectWrapper());

// class SimpleTest {
//   constructor() {
//     this.num = 0;
//   }
// 
//   testSimpleEqual() {
//     assertEquals(2, 1 + 1);
//   }
// }
// 
// testSuite(new SimpleTest());
