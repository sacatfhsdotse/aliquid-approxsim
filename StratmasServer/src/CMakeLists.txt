FILE (GLOB_RECURSE project_SRCS *.cpp *.cxx *.cc *.C *.c *.h *.hpp)
FILE (GLOB glob_remove s_subst.c schemas/file2h.cpp GridCellPV.cpp XMLHelper.cpp)
LIST(REMOVE_ITEM project_SRCS ${glob_remove})

SET (project_LIBS ${Boost_LIBRARIES} ${XERCESC_LIBRARIES} ${Log4Cxx_LIBRARIES})
SET (project_BIN ${PROJECT_NAME})

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
ENDIF()

IF(${CYGWIN})
ENDIF()

IF(${CMAKE_COMPILER_IS_GNUCXX})
  #set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -m64")
ENDIF()

#Generate schema header from .xsd files
ADD_EXECUTABLE(file2h schemas/file2h.cpp)
TARGET_LINK_LIBRARIES(file2h ${project_LIBS} ${project_OS_SPECIFIC})
FILE (GLOB_RECURSE project_schemas *.xsd)
ADD_CUSTOM_COMMAND(
  OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/schemas.h ${CMAKE_CURRENT_BINARY_DIR}/schemas.cpp
  COMMAND file2h ${CMAKE_CURRENT_BINARY_DIR}/schemas.cpp ${CMAKE_CURRENT_BINARY_DIR}/schemas.h ${project_schemas}
  DEPENDS file2h
)
include_directories(${project_BIN} ${CMAKE_CURRENT_BINARY_DIR})


ADD_EXECUTABLE(${project_BIN} ${project_SRCS} ${CMAKE_CURRENT_BINARY_DIR}/schemas.h ${CMAKE_CURRENT_BINARY_DIR}/schemas.cpp)
TARGET_LINK_LIBRARIES(${project_BIN} ${project_LIBS} ${project_OS_SPECIFIC})
SET_TARGET_PROPERTIES(${project_BIN} PROPERTIES VERSION "${APPLICATION_VERSION_MAJOR}.${APPLICATION_VERSION_MINOR}" OUTPUT_NAME ${project_BIN} CLEAN_DIRECT_OUTPUT 1)

INSTALL(TARGETS ${project_BIN} DESTINATION bin)
