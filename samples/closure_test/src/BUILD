# test closure_js_test

load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_library")
load("@com_google_j2cl//build_defs:rules.bzl", "closure_js_test")

closure_js_test(
    name = "simple_test",
    timeout = "short",
    srcs = ["simple_testsuite_test.js"],
    entry_points = ["goog:simple_testsuite"],
    deps = [
      ":object_simple",
      "@com_google_javascript_closure_library//closure/goog/testing:asserts",
      "@com_google_javascript_closure_library//closure/goog/testing:jsunit",
      "@com_google_javascript_closure_library//closure/goog/testing:testsuite",
    ],
    browsers = ["@io_bazel_rules_webtesting//browsers:chromium-local"],
)

closure_js_library(
    name = "object_simple",
    srcs = ["simpleobject_test.js"],
    deps = [
        "@com_google_javascript_closure_library//closure/goog/testing:asserts",
        "@com_google_javascript_closure_library//closure/goog/testing:jsunit",
    ],
    testonly = 1,
)


closure_js_library(
    name = "arithmetic_module_lib",
    srcs = ["arithmetic_module.js"],
)

closure_js_test(
    name = "arithmetic_module_test",
    timeout = "short",
    srcs = ["arithmetic_module_test.js"],
    entry_points = ["goog:arithmetic_module_test"],
    deps = [
        "//src:arithmetic_module_lib",
        "@com_google_javascript_closure_library//closure/goog/testing:asserts",
        "@com_google_javascript_closure_library//closure/goog/testing:jsunit",
        "@com_google_javascript_closure_library//closure/goog/testing:testsuite",
    ],
)
