load("@io_bazel_rules_go//go:def.bzl", "go_binary")

# The gen_test_html.template is used by js_test rule.
exports_files(["LICENSE", "gen_test_html.template"])

go_binary(
    name = "gwebtest",
    testonly = 1,
    srcs = ["webtest.go"],
    visibility = ["//visibility:public"],
    deps = [
        "@com_github_tebeka_selenium//:go_default_library",
        "@io_bazel_rules_webtesting//go/webtest:go_default_library",
    ],
)

java_binary(
    name = "webtest",
    testonly = 1,
    srcs = glob(["*.java"]),
    main_class = "tools.MyWebTest",
    visibility = ["//visibility:public"],
    args = ["--test_url", "some.url"],
    deps = [
        "@org_seleniumhq_selenium_selenium_api",
        "@org_seleniumhq_selenium_selenium_support",
        "@org_seleniumhq_selenium_selenium_remote_driver",
        "@io_bazel_rules_webtesting//java/com/google/testing/web:web",
    ],
)
