"""Common utilities for creating J2CL test targets

Takes Java source that contains JUnit tests, translates it into JS
and packages it into web test targets for testing.

Example use:

j2cl_test(
    name = "MyTest",
    srcs = ["MyTest.java"],
    deps = [
        ":Bar", # Directly depends on j2cl_library(name="Bar")
        "@com_google_j2cl//third_party:junit-j2cl",
    ],
)
"""

load(":j2cl_library.bzl", j2cl_library_rule = "j2cl_library")
load(":j2cl_generate_jsunit_suite.bzl", "j2cl_generate_jsunit_suite")
load(":j2cl_util.bzl", "get_java_package")
load(":closure_js_test_zip.bzl", closure_js_test_zip = "closure_js_test")
load(":closure_js_test.bzl", "closure_js_test")
# load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_test")

_STRIP_JSUNIT_PARAMETERS = [
    "args",
    "compiler",
    "deps_mgmt",
    "distribs",
    "externs_list",
    "extra_properties",
    "instrumentation",
    "local",
    "plugins",
    "shard_count",
    "size",
    "test_timeout",
    "timeout",
]

def _strip_jsunit_parameters(args):
    parameters = {}
    for parameter in args:
        if not parameter in _STRIP_JSUNIT_PARAMETERS:
            parameters[parameter] = args[parameter]
    return parameters

def _get_test_class(name, build_package, test_class):
    """Infers the name of the test class to be compiled."""
    if name.endswith("-j2cl"):
        name = name[:-5]
    if name.endswith("-j2wasm"):
        name = name[:-7]
    if name.endswith("-j2kt-jvm"):
        name = name[:-9]
    return test_class or get_java_package(build_package) + "." + name

def _verify_attributes(runtime_deps, **kwargs):
    if not kwargs.get("srcs"):
        # Disallow deps without srcs
        if kwargs.get("deps"):
            fail("deps not allowed without srcs; move to runtime_deps?")

        # Need to have runtime deps if there are no sources
        if not runtime_deps:
            fail("without srcs, runtime_deps required")

    # Disallow exports since we use them internally to forward deps to
    # j2cl_generate_jsunit_suite.
    if "exports" in kwargs:
        fail("using exports on j2cl_test is not supported")

def j2cl_test_common(
        name,
        deps = [],
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

    _verify_attributes(runtime_deps, **kwargs)

    j2cl_parameters = _strip_jsunit_parameters(kwargs)

    # This library serves two purposes:
    #   - Compile srcs files
    #   - Reexport all deps so that they are available to our code generation
    #     within j2cl_generate_jsunit_suite.
    #
    # Reexporting is necessary since generated code refers to test classes
    # which can be either in this library, its deps or its runtime deps.
    exports = deps + runtime_deps

    j2cl_library_rule(
        name = "%s_testlib" % name,
        deps = deps,
        exports = exports,
        testonly = 1,
        tags = tags,
        **j2cl_parameters
    )

    test_class = _get_test_class(name, native.package_name(), test_class)
    generated_suite_name = name + "_generated_suite"

    j2cl_generate_jsunit_suite(
        name = generated_suite_name,
        test_class = test_class,
        deps = [":%s_testlib" % name],
        tags = tags,
    )

    out_zip = ":%s_generated_suite.js.zip" % name
    testsuite_file_name = name + "_test.js"

    fail_multiple_testsuites = """
        FAIL: j2cl_test supports testing with a single testsuite only.
        IF YOU HAVE MULTIPLE TESTSUITES, WE DO NOT KNOW IF ALL OF
        YOUR TESTS PASS OR NOT!
    """

    fail_suiteclasses = """
        FAIL: j2cl_test currently doesn't support testing with @RunWith(Suite.class) format.
        Please directly provide the tests that has @RunWith(JUnit4.class).
    """

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
            "if [ $$(find -name *.js | wc -l) -ne 1 ];",
            "then echo \"%s\"; exit 1; fi" % fail_multiple_testsuites,
            "if [ $$(find -name %s.js | wc -l) -ne 1 ];" % name,
            "then echo \"%s\"; exit 1; fi" % fail_suiteclasses,
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
    ]

    st = get_java_package(native.package_name()) + ".SimpleTest"
    hw = get_java_package(native.package_name()) + ".HelloWorldTest"

    if name.endswith("Suite"):
        entry_points = [
            "javatests." + hw + "_AdapterSuite",
            "javatests." + st + "_AdapterSuite",
        ]
        srcs = [out_zip]

        closure_js_test_zip(
            name = name,
            srcs = srcs,
            deps = deps,
            testonly = 1,
            timeout = "short",
            entry_points = entry_points,
        )

    else:
        entry_points = ["javatests." + test_class + "_AdapterSuite"]
        srcs = [":" + testsuite_file_name]

        closure_js_test(
            name = name,
            srcs = srcs,
            deps = deps,
            testonly = 1,
            timeout = "short",
            entry_points = entry_points,
        )
