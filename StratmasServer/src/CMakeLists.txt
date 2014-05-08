
# all our files.
FILE (GLOB_RECURSE project_SRCS *.cpp *.cxx *.cc *.C *.c)
FILE (GLOB_RECURSE project_HEADERS *.h *.hpp)

# but remove files to not compile/link with the rest:
FILE (GLOB_RECURSE glob_remove s_subst.c file2h.cpp GridCellPV.cpp)
LIST(REMOVE_ITEM project_SRCS ${glob_remove})

# more vars.
SET (project_LIBS ${Boost_LIBRARIES} ${XERCESC_LIBRARIES} ${Log4Cxx_LIBRARIES} ${APR_LIBRARIES} ${APRUTIL_LIBRARIES})
SET (project_BIN ${PROJECT_NAME})
SET (schemas_dir "${MAINFOLDER}/../schemas")

#OS
IF(${CMAKE_SYSTEM_NAME} MATCHES "Windows")
  add_definitions(-DOS_WIN32)
ENDIF()

IF(${CMAKE_SYSTEM_NAME} MATCHES "Linux")
  add_definitions(-DOS_UNIX)
  add_definitions(-DOS_LINUX)
  #set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pthread")
  LIST (APPEND project_OS_SPECIFIC "-pthread")
ENDIF()

IF(${CMAKE_SYSTEM_NAME} MATCHES "Darwin")
  add_definitions(-DOS_UNIX)
  add_definitions(-DOS_DARWIN)
ELSE()
  #set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wl,-rpath")
ENDIF()

IF(${CMAKE_SYSTEM_NAME} MATCHES "Solaris")
  add_definitions(-DOS_UNIX)
  add_definitions(-DOS_SOLARIS)
  LIST (APPEND project_OS_SPECIFIC "-lsocket -lnsl")
ENDIF()

#Compiler
IF(${MSVC})
  #set(CMAKE_EXE_LINKER_FLAGS "/machine:x86_64")
  add_definitions(-DCOMPILER_MSVC)
ENDIF()

IF(${MINGW})
  add_definitions(-DCOMPILER_MINGW)
ENDIF()

IF(${CYGWIN})
  add_definitions(-DOS_UNIX)
  add_definitions(-DCOMPILER_CYGWIN)
  add_definitions(-D_XOPEN_SOURCE)
ENDIF()

IF(${CMAKE_COMPILER_IS_GNUCXX})
  #set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -m64")
  #add_definitions(-DCOMPILER_GNU)
ENDIF()

#Generate schemas.h and schemas.cpp from *.xsd files.
ADD_EXECUTABLE(file2h Schemas/file2h.cpp)
TARGET_LINK_LIBRARIES(file2h ${Boost_LIBRARIES})
FILE (GLOB_RECURSE project_schemas "${schemas_dir}/stratmasProtocol/*.xsd")
ADD_CUSTOM_COMMAND(
  OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/schemas.h ${CMAKE_CURRENT_BINARY_DIR}/schemas.cpp
  COMMAND file2h ${CMAKE_CURRENT_BINARY_DIR}/schemas.cpp ${CMAKE_CURRENT_BINARY_DIR}/schemas.h ${project_schemas}
  DEPENDS file2h
)

### Get include dirs based on header locations.
# help by: http://stackoverflow.com/questions/6921695/how-can-i-build-a-c-project-with-multiple-interdependent-subdirectories
SET (project_includes "")
FOREACH (_hs ${project_HEADERS})
    GET_FILENAME_COMPONENT(_dir ${_hs} PATH)
    LIST (APPEND project_includes ${_dir})
ENDFOREACH()
LIST(REMOVE_DUPLICATES project_includes)
include_directories(${CMAKE_CURRENT_BINARY_DIR} ${project_includes})
###

#stratmas
ADD_EXECUTABLE(${project_BIN} ${project_SRCS} ${CMAKE_CURRENT_BINARY_DIR}/schemas.cpp)
TARGET_LINK_LIBRARIES(${project_BIN} ${project_LIBS} ${project_OS_SPECIFIC})
SET_TARGET_PROPERTIES(${project_BIN} PROPERTIES VERSION "${APPLICATION_VERSION_MAJOR}.${APPLICATION_VERSION_MINOR}" OUTPUT_NAME ${project_BIN} CLEAN_DIRECT_OUTPUT 1)

IF(STATIC)
  set_target_properties(${project_BIN} PROPERTIES LINK_SEARCH_START_STATIC 1)
  set_target_properties(${project_BIN} PROPERTIES LINK_SEARCH_END_STATIC 1)
ENDIF()

IF(NOT Boost_FOUND)
  add_dependencies(${project_BIN} Boost)
ENDIF()

IF(NOT Log4Cxx_FOUND)
  add_dependencies(${project_BIN} Log4Cxx)
ENDIF()

IF(NOT XercesC_FOUND)
  add_dependencies(${project_BIN} XercesC)
ENDIF()

IF(NOT APR_FOUND)
  add_dependencies(${project_BIN} APR)
ENDIF()

IF(NOT APRUTIL_FOUND)
  add_dependencies(${project_BIN} APRUTIL)
ENDIF()

IF(TESTS AND NOT UnitTestPlusPlus_FOUND)
  add_dependencies(${project_BIN} UnitTestPlusPlus)
ENDIF()

INSTALL(TARGETS ${project_BIN} DESTINATION bin)
