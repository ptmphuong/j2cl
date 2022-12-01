load(
    "@io_bazel_rules_closure//closure:defs.bzl",
    "create_closure_js_library",
    "CLOSURE_JS_TOOLCHAIN_ATTRS"
)

def _impl_zip_file_test_library(ctx):
    return create_closure_js_library(ctx, ctx.files.srcs, ctx.files.deps, suppress = ctx.attr.suppress)

closure_js_library_zip = rule(
    implementation = _impl_zip_file_test_library,
    attrs = dict(CLOSURE_JS_TOOLCHAIN_ATTRS, **{
        "srcs": attr.label_list(allow_files = [".js", ".js.zip"]),
        "suppress": attr.string_list(),
        "deps": attr.label_list(
            providers = ["closure_js_library"],
        ),
    }),
)
