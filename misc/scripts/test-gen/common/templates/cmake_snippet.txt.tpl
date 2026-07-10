# {{MARKER}}
# Append to CMakeLists.txt (near the end) to register the smoke test with CTest.

enable_testing()
add_executable({{PROJECT_NAME}}_smoke_test tests/smoke_test.cpp)
add_test(NAME smoke COMMAND {{PROJECT_NAME}}_smoke_test)
set_tests_properties(smoke PROPERTIES LABELS "nexus-generated")
