/*
 * Copyright 2021 Google Inc.
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
package javaemul.internal;

import static javaemul.internal.InternalPreconditions.checkNotNull;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Platform specific utilities. This class will eventually replace most of the functionalities in
 * JsUtils.
 */
public final class Platform {

  @JsMethod(namespace = JsPackage.GLOBAL)
  public static native boolean isNaN(double x);

  @JsMethod(namespace = JsPackage.GLOBAL)
  public static native boolean isFinite(double x);

  @JsType(isNative = true, namespace = JsPackage.GLOBAL)
  private static class ArrayBuffer {
    ArrayBuffer(int size) {}
  }

  @JsType(isNative = true, namespace = JsPackage.GLOBAL)
  private static class Float64Array {
    Float64Array(ArrayBuffer buf) {}
  }

  @JsType(isNative = true, namespace = JsPackage.GLOBAL)
  private static class Float32Array {
    Float32Array(ArrayBuffer buf) {}
  }

  @JsType(isNative = true, namespace = JsPackage.GLOBAL)
  private static class Uint32Array {
    Uint32Array(ArrayBuffer buf) {}
  }

  public static int floatToRawIntBits(float value) {
    ArrayBuffer buf = new ArrayBuffer(4);
    JsUtils.<float[]>uncheckedCast(new Float32Array(buf))[0] = value;
    return JsUtils.<int[]>uncheckedCast(new Uint32Array(buf))[0] | 0;
  }

  public static float intBitsToFloat(int value) {
    ArrayBuffer buf = new ArrayBuffer(4);
    JsUtils.<int[]>uncheckedCast(new Uint32Array(buf))[0] = value;
    return JsUtils.<float[]>uncheckedCast(new Float32Array(buf))[0];
  }

  public static long doubleToRawLongBits(double value) {
    ArrayBuffer buf = new ArrayBuffer(8);
    JsUtils.<double[]>uncheckedCast(new Float64Array(buf))[0] = value;
    int[] intBits = JsUtils.<int[]>uncheckedCast(new Uint32Array(buf));
    return LongUtils.fromBits(intBits[0] | 0, intBits[1] | 0);
  }

  public static double longBitsToDouble(long value) {
    ArrayBuffer buf = new ArrayBuffer(8);
    int[] intBits = JsUtils.<int[]>uncheckedCast(new Uint32Array(buf));
    intBits[0] = (int) value;
    intBits[1] = LongUtils.getHighBits(value);
    return JsUtils.<double[]>uncheckedCast(new Float64Array(buf))[0];
  }

  @JsMethod(name = "Number.prototype.toPrecision.call", namespace = JsPackage.GLOBAL)
  public static native String toPrecision(double value, int precision);

  @SuppressWarnings("StringEquality")
  public static boolean objectsStringEquals(String x, String y) {
    // In JS, strings have identity equality.
    return x == y;
  }

  public static boolean isEqual(Object x, Object y) {
    return checkNotNull(x) == y;
  }

  public static boolean isEqual(Float x, Object y) {
    // Make sure Float follow the same semantic as Double for consistency.
    return (y instanceof Float)
        && Double.valueOf(x.doubleValue()).equals(((Float) y).doubleValue());
  }

  public static int hashCode(double x) {
    return (int) x;
  }

  public static int hashCode(float x) {
    return (int) x;
  }

  private Platform() {}
}
