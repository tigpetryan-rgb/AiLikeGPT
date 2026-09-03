cmake_minimum_required(VERSION 3.22.1)
project(ailikegpt_native LANGUAGES C CXX)

add_library(
    ailikegpt_native
    SHARED
    native_bridge.cpp
)

target_compile_features(ailikegpt_native PRIVATE cxx_std_17)
target_compile_options(ailikegpt_native PRIVATE -Wall -Wextra -Wpedantic)

find_library(log-lib log REQUIRED)

target_link_libraries(
    ailikegpt_native
    PRIVATE
    ${log-lib}
)

# Phase 2 will vendor/pin llama.cpp under third_party/llama.cpp and link it
# into this target. The bridge deliberately builds without llama.cpp today so
# the Android foundation can be validated independently from model inference.
