"""Runs JavaScript unit tests inside a headless web browser"""

load("@io_bazel_rules_webtesting//web:web.bzl", "web_test_suite")
load(
    "@io_bazel_rules_closure//closure:defs.bzl",
    "closure_js_library",
    "closure_js_binary"
)

load(
    "@io_bazel_rules_closure//closure/compiler/test/js_zip:zip_file_test_library.bzl",
    "zip_file_test_library"
)

load(
    "//build_defs/internal_do_not_use:closure_js_library_zip.bzl",
    "closure_js_library_zip"
)

def closure_js_test(
        name,
        srcs,
        data = [],
        deps = None,
        compilation_level = None,
        css = None,
        defs = None,
        entry_points = None,
        html = None,
        language = None,
        lenient = False,
        suppress = None,
        visibility = None,
        tags = [],
        debug = False,
        browsers = None,
        **kwargs):

    if not srcs:
        fail("closure_js_test can not have an empty 'srcs' list")
    if language:
        print("closure_js_test 'language' is removed and now always ES6 strict")
    # for src in srcs:
    #     if not src.endswith("_test.js"):
    #         fail("closure_js_test srcs must be files ending with _test.js")
    # if len(srcs) == 1:
    #     work = [(name, srcs)]
    # else:
    #     work = [(name + _make_suffix(src), [src]) for src in srcs]

    all_tests = []

    for enp in entry_points:
        closure_js_library_zip(
            name = "%s_%s_closure_lib" % (name, enp),
            srcs = srcs,
            # data = data,
            deps = deps,
            # lenient = lenient,
            # suppress = suppress,
            suppress = ["strictDependencies"],
            visibility = visibility,
            testonly = True,
            tags = tags,
        )

        deps = deps + [
            "%s_%s_closure_lib" % (name, enp),
        ]

        closure_js_binary(
            name = "%s_%s_closure_bin" % (name, enp),
            deps = deps,
            compilation_level = compilation_level,
            css = css,
            debug = True,
            defs = defs,
            entry_points = [enp],
            formatting = "PRETTY_PRINT",
            visibility = visibility,
            testonly = True,
            tags = tags,
        )

        html = "gen_%s_%s" % (name, enp)

        gen_test_html(
          name = html,
          test_file_js = "%s_%s_closure_bin.js" % (name, enp),
        )

        data = [":%s_%s_closure_bin" % (name, enp), html]

        web_test_suite(
            name = "%s_%s" % (name, enp),
            data = data,
            test = "@com_google_j2cl//build_defs/internal_do_not_use/tools:webtest",
            args = ["--test_url", "$(location %s)" % html],
            browsers = ["@io_bazel_rules_webtesting//browsers:chromium-local"],
            tags = ["no-sandbox", "native"],
            visibility = visibility,
            **kwargs
        )

        all_tests.append(":%s_%s" % (name, enp))

    native.test_suite(
        name = name,
        tests = all_tests,
        tags = tags,
    )
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

# Used to generate default test.html file for running Closure-based JS tests.
# The test_file_js argument specifies the name of the JS file containing tests,
# typically created with closure_js_binary.
# The output is created from gen_test_html.template file.
gen_test_html = rule(
    implementation = _gen_test_html_impl,
    attrs = {
        "test_file_js": attr.string(mandatory = True),
        "_template": attr.label(
            default = Label("@com_google_j2cl//build_defs/internal_do_not_use/tools:gen_test_html.template"),
            allow_single_file = True,
        ),
    },
    outputs = {"html_file": "%{name}.html"},
)

def _make_suffix(path):
    return "_" + path.replace("_test.js", "").replace("-", "_").replace("/", "_")
