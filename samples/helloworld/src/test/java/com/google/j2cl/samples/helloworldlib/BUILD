load("@com_google_j2cl//build_defs:rules.bzl", "j2cl_test")

package(licenses = ["notice"])

j2cl_test(
    name = "HelloWorldTest",
    srcs = ["HelloWorldTest.java"],
    browsers = ["@io_bazel_rules_webtesting//browsers:chromium-local"],
    deps = [
        "//src/main/java/com/google/j2cl/samples/helloworldlib",
        "@com_google_j2cl//third_party:junit-j2cl",
    ],
)

j2cl_test(
    name = "SimpleTest",
    srcs = ["SimpleTest.java"],
    deps = [
        "@com_google_j2cl//third_party:junit-j2cl",
    ],
)
