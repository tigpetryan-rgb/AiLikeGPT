cmake_minimum_required(VERSION 3.31.6)
project(ailikegpt_native LANGUAGES C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(LLAMA_SRC "${CMAKE_CURRENT_LIST_DIR}/../../../../third_party/llama.cpp")
set(AILIKEGPT_LLAMA_AVAILABLE OFF)

if(EXISTS "${LLAMA_SRC}/CMakeLists.txt")
    message(STATUS "AiLikeGPT: using local llama.cpp at ${LLAMA_SRC}")

    set(BUILD_SHARED_LIBS ON CACHE BOOL "" FORCE)
    set(LLAMA_BUILD_APP OFF CACHE BOOL "" FORCE)
    set(LLAMA_BUILD_COMMON ON CACHE BOOL "" FORCE)
    set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
    set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
    set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)
    set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
    set(GGML_BACKEND_DL ON CACHE BOOL "" FORCE)
    set(GGML_CPU_ALL_VARIANTS ON CACHE BOOL "" FORCE)
    set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)

    if(ANDROID_ABI STREQUAL "arm64-v8a")
        set(GGML_SYSTEM_ARCH "ARM" CACHE STRING "" FORCE)
        set(GGML_CPU_KLEIDIAI ON CACHE BOOL "" FORCE)
        set(GGML_OPENMP ON CACHE BOOL "" FORCE)
    endif()

    add_subdirectory("${LLAMA_SRC}" "${CMAKE_BINARY_DIR}/llama" EXCLUDE_FROM_ALL)
    set(AILIKEGPT_LLAMA_AVAILABLE ON)
else()
    message(WARNING "AiLikeGPT: llama.cpp not synced; building JNI foundation stub")
endif()

add_library(
    ailikegpt_native
    SHARED
    native_bridge.cpp
)

target_compile_options(ailikegpt_native PRIVATE -Wall -Wextra -Wpedantic)

find_library(log-lib log REQUIRED)

if(AILIKEGPT_LLAMA_AVAILABLE)
    target_compile_definitions(ailikegpt_native PRIVATE AILIKEGPT_WITH_LLAMA=1)
    target_include_directories(
        ailikegpt_native
        PRIVATE
        "${LLAMA_SRC}"
        "${LLAMA_SRC}/common"
        "${LLAMA_SRC}/include"
        "${LLAMA_SRC}/ggml/include"
        "${LLAMA_SRC}/ggml/src"
    )
    target_link_libraries(
        ailikegpt_native
        PRIVATE
        llama
        llama-common
        android
        ${log-lib}
    )
else()
    target_link_libraries(
        ailikegpt_native
        PRIVATE
        ${log-lib}
    )
endif()
