# gen_test_html.template used by closure_js_test rule.
exports_files(["LICENSE", "gen_test_html.template"])

java_binary(
    name = "webtest",
    testonly = 1,
    srcs = glob(["*.java"]),
    main_class = "tools.WebTestRunner",
    visibility = ["//visibility:public"],
    deps = [
        "@org_seleniumhq_selenium_selenium_api",
        "@org_seleniumhq_selenium_selenium_support",
        "@org_seleniumhq_selenium_selenium_remote_driver",
        "@io_bazel_rules_webtesting//java/com/google/testing/web:web",
    ],
)
