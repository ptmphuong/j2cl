# The gen_test_html.template is used by js_test rule.
exports_files(["LICENSE", "gen_test_html.template"])

java_binary(
    name = "webtest",
    testonly = 1,
    # srcs = glob(["*.java"]),
    srcs = [
        "webtest.java",
        "FileServerHandler.java",
    ],
    main_class = "tools.MyWebTest",
    visibility = ["//visibility:public"],
    deps = [
        "@org_seleniumhq_selenium_selenium_api",
        "@org_seleniumhq_selenium_selenium_support",
        "@org_seleniumhq_selenium_selenium_remote_driver",
        "@io_bazel_rules_webtesting//java/com/google/testing/web:web",
    ],
)

java_binary(
    name = "webdriver",
    testonly = 1,
    # srcs = glob(["*.java"]),
    srcs = [
        "driver.java",
    ],
    main_class = "tools.MyWebDriver",
    visibility = ["//visibility:public"],
    deps = [
        "@org_seleniumhq_selenium_selenium_api",
        "@org_seleniumhq_selenium_selenium_support",
        "@org_seleniumhq_selenium_selenium_remote_driver",
        "@io_bazel_rules_webtesting//java/com/google/testing/web:web",
    ],
)
