# test closure_js_test

# load("@com_google_j2cl//build_defs:rules.bzl", "closure_js_test")
load(
    "@io_bazel_rules_closure//closure:defs.bzl",
    "closure_js_test",
    "closure_js_library")

closure_js_library(
    name = "simple_test",
    srcs = ["simple_test.js", "simple_library.js"],
    deps = [
        "@com_google_javascript_closure_library//closure/goog/testing:asserts",
        "@com_google_javascript_closure_library//closure/goog/testing:jsunit",
    ],
    testonly = 1,
)

# test
closure_js_test(
    name = "simple_testsuite",
    timeout = "short",
    srcs = ["simple_testsuite_test.js"],
    entry_points = ["goog:simple_testsuite"],
    deps = [
      ":simple_test",
      "@com_google_javascript_closure_library//closure/goog/testing:asserts",
      "@com_google_javascript_closure_library//closure/goog/testing:jsunit",
      "@com_google_javascript_closure_library//closure/goog/testing:testsuite",
    ],
    # browsers = ["@io_bazel_rules_webtesting//browsers:chromium-local"],
)


# TODO(phpham): try doing multiple testsuites, multiple endpoints

closure_js_library(
    name = "arithmetic_module_lib",
    srcs = ["arithmetic_module.js"],
)
#
# closure_js_test(
#     name = "arithmetic_module_test",
#     timeout = "short",
#     srcs = ["arithmetic_module_test.js"],
#     entry_points = ["goog:arithmetic_module_test"],
#     deps = [
#         ":arithmetic_module_lib",
#         "@com_google_javascript_closure_library//closure/goog/testing:asserts",
#         "@com_google_javascript_closure_library//closure/goog/testing:jsunit",
#         "@com_google_javascript_closure_library//closure/goog/testing:testsuite",
#     ],
# )

# closure_js_test(
#     name = "multiple_testsuite",
#     timeout = "short",
#     srcs = [
#         "arithmetic_module_test.js",
#         "simple_testsuite_test.js",
#     ],
#     entry_points = [
#         "goog:arithmetic_module_test",
#         "goog:simple_testsuite"
#     ],
#     deps = [
#         ":arithmetic_module_lib",
#         ":simple_test",
#         "@com_google_javascript_closure_library//closure/goog/testing:asserts",
#         "@com_google_javascript_closure_library//closure/goog/testing:jsunit",
#         "@com_google_javascript_closure_library//closure/goog/testing:testsuite",
#     ],
# )
