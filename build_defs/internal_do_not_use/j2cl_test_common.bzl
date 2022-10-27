"""j2cl_test build macro"""

load(":j2cl_java_library.bzl", j2cl_library_rule = "j2cl_library")
load(":j2cl_generate_jsunit_suite.bzl", "j2cl_generate_jsunit_suite")
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_binary", "closure_js_test")
load("@io_bazel_rules_webtesting//web:web.bzl", "web_test_suite")

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
        default_browser = "//testing/web/browsers:chrome-linux",
        extra_defs = [],
        jvm_flags = [],
        tags = [],
        **kwargs):
    """Macro for running a JUnit test cross compiled as a web test"""

    exports = (kwargs.get("deps") or []) + runtime_deps

    j2cl_library_rule(
        name = "%s_lib" % name,
        exports = exports,
        testonly = 1,
        tags = tags,
        # **j2cl_parameters
    )

    j2cl_generate_jsunit_suite(
        # name = "%s_generated_suite" % name,
        name = name,
        test_class = test_class,
        deps = [":%s_lib" % name],
        tags = tags,
    )

    # deps = [
    #   "%s_lib" % name,
    #   "@com_google_javascript_closure_library//closure/goog/testing:testsuite",
    # ]

    # # No-op until unit testing support implemented for open-source.
    # native.genrule(
    #     name = name,
    #     deprecation = "\nCAUTION: This is a placeholder. " +
    #                   "j2cl_test has not ported to opensource yet." +
    #                   "\nHENCE WE DO *NOT* KNOW IF YOUR TEST IS PASSING OR NOT!",
    #     cmd = "echo Empty > $@",
    #     tags = ["manual"],
    #     outs = [name + ".out"],
    # )

def _gen_test_html_impl(ctx):
    """Implementation of the gen_test_html rule."""
    ctx.actions.expand_template(
        template = ctx.file._template,
        output = ctx.outputs.html_file,
        substitutions = {
            "{{TEST_FILE_JS}}": ctx.attr.test_file_js,
        },
    )
    runfiles = ctx.runfiles(files = [ctx.outputs.html_file], collect_default = True)
    return [DefaultInfo(runfiles = runfiles)]

# A rule used to generate default test.html file suitable 
# for running Closure-based JS tests.
# The test_file_js argument specifies the name of the JS file containing tests,
# typically created with closure_js_binary.
# The output is created from gen_test_html.template file.
gen_test_html = rule(
    implementation = _gen_test_html_impl,
    attrs = {
        "test_file_js": attr.string(mandatory = True),
        "_template": attr.label(
            default = Label("//build_defs/interal_do_not_use/tools:gen_test_html.template"),
            allow_single_file = True,
        ),
    },
    outputs = {"html_file": "%{name}.html"},
)
