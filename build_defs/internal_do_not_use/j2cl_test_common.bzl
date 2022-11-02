"""Common utilities for creating J2CL test targets"""

load(":j2cl_java_library.bzl", j2cl_library_rule = "j2cl_library")
load(":j2cl_generate_jsunit_suite.bzl", j2cl_generate_testsuite = "j2cl_generate_jsunit_suite")
load(":j2cl_util.bzl", "get_java_package")
load(":closure_js_test.bzl", "closure_js_test")
load(":j2cl_js_common.bzl", "J2CL_TEST_DEFS")

# buildifier: disable=unused-variable
def j2cl_test_common(
        name,
        deps,
        runtime_deps = [],
        test_class = None,
        data = [],
        bootstrap_files = [],
        flaky = None,
        compile = 0,
        platform = "CLOSURE",
        browsers = None,
        extra_defs = [],
        jvm_flags = [],
        tags = [],
        **kwargs):
    """Macro for running a JUnit test cross compiled as a web test"""

    deps = deps + ["@com_google_j2cl//:jre"]
    exports = deps + runtime_deps

    j2cl_library_rule(
        name = "%s_testlib" % name,
        deps = deps,
        exports = exports,
        testonly = 1,
        tags = tags,
        **kwargs
    )

    test_class = _get_test_class(name, native.package_name(), test_class)
    print("my_test_class: " + test_class)
    generated_suite_name = name + "_generated_suite"

    j2cl_generate_testsuite(
        name = generated_suite_name,
        test_class = test_class,
        deps = [":%s_testlib" % name],
        tags = tags,
    )

    out_zip = ":%s_generated_suite.js.zip" % name
    testsuite_file_name = name + "_test.js"

    native.genrule(
        name = "gen" + name + "_test.js",
        srcs = [out_zip],
        outs = [
            testsuite_file_name,
        ],
        cmd = "\n".join([
            "unzip -q -o $(locations %s) *.js -d zip_out/" % out_zip,
            "cd zip_out/",
            "mkdir -p ../$(RULEDIR)",
            "for f in $$(find -name *.js); do mv $$f ../$@; done",
        ]),
        testonly = 1,
    )

    deps = [
      "%s_testlib" % name,
      ":%s_generated_suite_lib" % name,
      "@com_google_javascript_closure_library//closure/goog/testing:asserts",
      "@com_google_javascript_closure_library//closure/goog/testing:jsunit",
      "@com_google_javascript_closure_library//closure/goog/testing:testsuite",
      "@com_google_javascript_closure_library//closure/goog/testing:testcase",
      # "@com_google_j2cl//build_defs/internal_do_not_use:internal_parametrized_test_suite",
    ]

    closure_js_test(
        name = name,
        srcs = [":" + testsuite_file_name],
        deps = deps,
        browsers = browsers,
        testonly = 1,
        timeout = "short",
        entry_points = ["javatests." + test_class + "_AdapterSuite",],
        # defs = J2CL_TEST_DEFS,
    )

def _get_test_class(name, build_package, test_class):
    """Infers the name of the test class to be compiled."""
    if name.endswith("-j2cl"):
        name = name[:-5]
    if name.endswith("-j2wasm"):
        name = name[:-7]
    if name.endswith("-j2kt-jvm"):
        name = name[:-9]
    return test_class or get_java_package(build_package) + "." + name
