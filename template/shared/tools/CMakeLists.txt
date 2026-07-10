cmake_minimum_required(VERSION 3.24)
project(nxs_pack_tools LANGUAGES CXX)

add_executable(pack_archive
    pack_archive.cpp
    ../runtime/ScriptArchive.cpp
    ../runtime/ScriptCrypto.cpp)
target_include_directories(pack_archive PRIVATE ../runtime)
