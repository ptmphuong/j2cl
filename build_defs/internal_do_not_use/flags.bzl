# TODO(phpham): check if we really need these flags?

# keep sorted
USE_TYPES_FOR_OPTIMIZATIONS_FLAGS = [
    "--ambiguate_properties=true",
    "--disambiguate_properties=true",
    "--inline_properties=ON",
    "--use_types_for_optimization",
]

# keep sorted
ADVANCED_OPTIMIZATIONS_FLAGS = [
    "--closure_pass=true",
    "--coalesce_variable_names=on",
    "--collapse_anonymous_functions=true",
    "--collapse_object_literals=on",
    "--collapse_properties=true",
    "--collapse_variable_declarations=true",
    "--compute_function_side_effects=true",
    "--convert_to_dotted_properties=true",
    "--cross_chunk_code_motion=true",
    "--cross_chunk_method_motion=true",
    "--devirtualize_methods=true",
    "--extract_prototype_member_decl=true",
    "--fold_constants=true",
    "--inline_functions=true",
    "--inline_variables=true",
    "--jscomp_warning=nonStandardJsDocs",
    "--label_renaming=true",
    "--optimize_arguments_array=true",
    "--property_renaming=ALL_UNQUOTED",
    "--remove_abstract_methods=true",
    "--remove_closure_asserts=true",
    "--remove_dead_assignments=true",
    "--remove_dead_code=true",
    "--remove_unused_prototype_props=true",
    "--remove_unused_vars=true",
    "--reserve_raw_exports=true",
    "--rewrite_function_expressions=false",
    "--smart_name_removal=true",
    "--variable_renaming=ALL",
]

ADVANCED_OPTIMIZATION_TEST_FLAGS = [
    "--export_test_functions",
]

# keep sorted
DEBUG_FLAGS = [
    "--generate_pseudo_names",
    "--input_delimiter_format=//[%name%]",
    "--pretty_print",
    "--print_input_delimiter",
]

# keep sorted
VERBOSE_WARNING_FLAGS = [
    "--jscomp_warning=checkRegExp",
    "--jscomp_warning=checkTypes",
    "--jscomp_warning=const",
    "--jscomp_warning=missingProperties",
    "--jscomp_warning=tooManyTypeParams",
    "--jscomp_warning=visibility",
]

JS_TEST_FLAGS = (
    ADVANCED_OPTIMIZATIONS_FLAGS +
    ADVANCED_OPTIMIZATION_TEST_FLAGS +
    DEBUG_FLAGS +
    VERBOSE_WARNING_FLAGS + [
        # Allow assertion failures because it's useful for tests.
        "--remove_closure_asserts=false",
        # TODO(b/73956781): Remove this flag now that closure_test_suite supports AJD.
        "--manage_closure_dependencies",
        # goog.setTestOnly will throw exceptions without this.
        "--define=goog.DISALLOW_TEST_ONLY_CODE=false",
        # Required for jsunit.
        "--define=goog.DEBUG=true",
    ]
)
