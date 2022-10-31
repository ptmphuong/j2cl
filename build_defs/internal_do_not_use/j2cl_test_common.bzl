"""j2cl_test build macro"""

load(":j2cl_java_library.bzl", j2cl_library_rule = "j2cl_library")
load(":j2cl_generate_jsunit_suite.bzl", "j2cl_generate_jsunit_suite")
load(":j2cl_util.bzl", "get_java_package")
load(":closure_js_test.bzl", "closure_js_test")
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_library")
load(":j2cl_js_common.bzl", "J2CL_TEST_DEFS")

# buildifier: disable=unused-variable
def j2cl_test_common(
        name,
        runtime_deps = [],
        test_class = None,
        data = [],
        bootstrap_files = [],
        flaky = None,
        compile = 0,
        platform = "CLOSURE",
        browsers = None,
        default_browser = ["@io_bazel_rules_webtesting//browsers:chromium-local"],
        extra_defs = [],
        jvm_flags = [],
        tags = [],
        **kwargs):
    """Macro for running a JUnit test cross compiled as a web test"""

    exports = (kwargs.get("deps") or []) + runtime_deps
    test_class = _get_test_class(name, native.package_name(), test_class)
    generated_suite_name = name + "_generated_suite"

    j2cl_library_rule(
        name = "%s_testlib" % name,
        exports = exports,
        testonly = 1,
        tags = tags,
        **kwargs
    )

    j2cl_generate_jsunit_suite(
        name = generated_suite_name,
        test_class = test_class,
        deps = [":%s_testlib" % name],
        tags = tags,
    )

    deps = [
      "%s_testlib" % name,
      ":%s_generated_suite_lib" % name,
      "@com_google_javascript_closure_library//closure/goog/testing:asserts",
      "@com_google_javascript_closure_library//closure/goog/testing:jsunit",
      "@com_google_javascript_closure_library//closure/goog/testing:testsuite",
      "@com_google_j2cl//build_defs/internal_do_not_use:internal_parametrized_test_suite",
    ]

    # closure_js_library(
    #   name = "%s_closure_testlib" % name,
    #   srcs = [
    #     # ":lib" + generated_suite_name + "_lib.jar",
    #     # generated_suite_name,
    #     # ":%s_testlib" % name,
    #     # ":%s_generated_suite.js.zip" % name,
    #   ],
    #   deps = deps,
    #   testonly = 1,
    # )

    # deps = deps + ["%s_closure_testlib" % name]

    closure_js_test(
        name = name,
        srcs = [":%s_generated_suite.js" % name],
        deps = deps,
        browsers = browsers,
        testonly = 1,
        timeout = "short",
        entry_points = ["javatests.com.google.j2cl.samples.helloworldlib.SimplePassingTest_AdapterSuite"],
        defs = J2CL_TEST_DEFS,
    )

    # No-op until unit testing support implemented for open-source.
    # native.genrule(
    #     name = name,
    #     deprecation = "\nCAUTION: This is a placeholder. " +
    #                   "j2cl_test has not ported to opensource yet." +
    #                   "\nHENCE WE DO *NOT* KNOW IF YOUR TEST IS PASSING OR NOT!",
    #     cmd = "echo Empty > $@",
    #     tags = ["manual"],
    #     outs = [name + ".out"],
    # )

def _get_test_class(name, build_package, test_class):
    """Infers the name of the test class to be compiled."""
    if name.endswith("-j2cl"):
        name = name[:-5]
    if name.endswith("-j2wasm"):
        name = name[:-7]
    if name.endswith("-j2kt-jvm"):
        name = name[:-9]
    return test_class or get_java_package(build_package) + "." + name
