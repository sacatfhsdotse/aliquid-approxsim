
# all our files.
FILE (GLOB_RECURSE project_SRCS *.cpp *.cxx *.cc *.C *.c)
FILE (GLOB_RECURSE project_HEADERS *.h *.hpp)

# but remove files to not compile/link with the rest:
FILE (GLOB_RECURSE glob_remove s_subst.c file2h.cpp GridCellPV.cpp XMLHelper.cpp)
LIST(REMOVE_ITEM project_SRCS ${glob_remove})

# more vars.
SET (project_LIBS ${Boost_LIBRARIES} ${XERCESC_LIBRARIES} ${Log4Cxx_LIBRARIES})
SET (project_BIN ${PROJECT_NAME})
SET (schemas_dir "${MAINFOLDER}/../schemas")

#OS
IF(${CMAKE_SYSTEM_NAME} MATCHES "Windows")
  add_definitions(-DOS_WIN32)
ENDIF()

IF(${CMAKE_SYSTEM_NAME} MATCHES "Linux")
  add_definitions(-DOS_LINUX)
  #set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pthread")
  LIST (APPEND project_OS_SPECIFIC "-pthread")
ENDIF()

IF(${CMAKE_SYSTEM_NAME} MATCHES "Darwin")
  add_definitions(-DOS_DARWIN)
ELSE()
  #set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wl,-rpath")
ENDIF()

IF(${CMAKE_SYSTEM_NAME} MATCHES "Solaris")
  add_definitions(-DOS_SOLARIS)
  LIST (APPEND project_OS_SPECIFIC "-lsocket -lnsl")
ENDIF()

#Compiler
IF(${MSVC})
  #set(CMAKE_EXE_LINKER_FLAGS "/machine:x86_64")
ENDIF()

IF(${MINGW})
  # TODO ?
ENDIF()

IF(${CYGWIN})
  # TODO ?
ENDIF()

IF(${CMAKE_COMPILER_IS_GNUCXX})
  #set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -m64")
ENDIF()

#Generate schemas.h and schemas.cpp from *.xsd files.
ADD_EXECUTABLE(file2h schemas/file2h.cpp)
TARGET_LINK_LIBRARIES(file2h ${project_LIBS} ${project_OS_SPECIFIC})
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

INSTALL(TARGETS ${project_BIN} DESTINATION bin)
