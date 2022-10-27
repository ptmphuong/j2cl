"""Similar to rules_closure's closure_js_test, but using rules_webtesting's web_test_suite instead of phantomJS"""

load("@io_bazel_rules_webtesting//web:web.bzl", "web_test_suite")
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_library")
load("@io_bazel_rules_go//go:def.bzl", "go_binary")
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_binary")

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
    for src in srcs:
        if not src.endswith("_test.js"):
            fail("closure_js_test srcs must be files ending with _test.js")
    if len(srcs) == 1:
        work = [(name, srcs)]
    else:
        work = [(name + _make_suffix(src), [src]) for src in srcs]

    for shard, sauce in work:
        closure_js_library(
            name = "%s_lib" % shard,
            srcs = sauce,
            data = data,
            deps = deps,
            lenient = lenient,
            suppress = suppress,
            visibility = visibility,
            testonly = True,
            tags = tags,
        )

        if type(entry_points) == type({}):
            ep = entry_points.get(sauce[0])
        else:
            ep = entry_points

        closure_js_binary(
            name = "%s_bin" % shard,
            deps = [":%s_lib" % shard],
            compilation_level = compilation_level,
            css = css,
            debug = True,
            defs = defs,
            entry_points = ep,
            formatting = "PRETTY_PRINT",
            visibility = visibility,
            testonly = True,
            tags = tags,
        )

        ### NEWLY ADDED TO REPLACE PHANTOMJS ###
        if not html:
          gen_test_html(
            name = "gen_%s" % shard,
            test_file_js = "%s_bin.js" % shard,
          )
          html = "gen_%s" % shard

        if not browsers:
            browsers = ["@io_bazel_rules_webtesting//browsers:chromium-local"]

        web_test_suite(
            name = shard,
            data = [":%s_bin" % shard, html],
            test = "@com_google_j2cl//build_defs/internal_do_not_use/tools:webtest",
            args = ["--test_url", "$(location %s)" % html],
            browsers = browsers,
            tags = ["no-sandbox", "native"],
            visibility = visibility,
            **kwargs
        )

    if len(srcs) > 1:
        native.test_suite(
            name = name,
            tests = [":" + shard for shard, _ in work],
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

# A rule used by closure_js_test to generate default test.html file
# suitable for running Closure-based JS tests.
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
