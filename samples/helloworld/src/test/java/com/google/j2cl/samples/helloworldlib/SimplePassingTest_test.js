/**
 * Generated test suite from j2cl_test target
 */
goog.module('javatests.com.google.j2cl.samples.helloworldlib.SimplePassingTest_AdapterSuite');


const JavaWrapper = goog.require('javatests.com.google.j2cl.samples.helloworldlib.SimplePassingTest_Adapter');

goog.setTestOnly();

const TestCase = goog.require('goog.testing.TestCase');

const testSuite = goog.require('goog.testing.testSuite');
testSuite(new JavaWrapper(), {order: TestCase.Order.NATURAL});
