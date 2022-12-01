/*
 * My simple custom tests to quickly check during development
 */
package com.google.j2cl.samples.helloworldlib;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({SimpleTest.class, HelloWorldTest.class})
// @SuiteClasses(SimpleTest.class)
public class SimpleSuite {}
