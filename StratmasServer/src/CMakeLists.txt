FILE (GLOB_RECURSE project_SRCS *.cpp *.cxx *.cc *.C *.c *.h *.hpp)
SET (project_LIBS ${Boost_LIBRARIES} ${Xerces_LIBRARIES} ${Log4Cxx_LIBRARIES})
SET (project_BIN ${PROJECT_NAME})

#OS
IF(${CMAKE_SYSTEM_NAME} MATCHES "Windows")
  add_definitions(-DOS_WIN32)
ENDIF()

IF(${CMAKE_SYSTEM_NAME} MATCHES "Linux")
  add_definitions(-DOS_LINUX)
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pthread")
  LIST (APPEND project_OS_SPECIFIC "-pthread")
ENDIF()

IF(${CMAKE_SYSTEM_NAME} MATCHES "Darwin")
  add_definitions(-DOS_DARWIN)
ELSE()
  LIST (APPEND project_OS_SPECIFIC "-Wl,-rpath")
ENDIF()

IF(${CMAKE_SYSTEM_NAME} MATCHES "Solaris")
  add_definitions(-DOS_SOLARIS)
  LIST (APPEND project_OS_SPECIFIC "-lsocket -lnsl")
ENDIF()

#Compiler
IF(${MSVC})
ENDIF()

IF(${MINGW})
ENDIF()

IF(${CYGWIN})
ENDIF()

IF(${CMAKE_COMPILER_IS_GNUCXX})
ENDIF()


ADD_EXECUTABLE(${project_BIN} ${project_SRCS})
TARGET_LINK_LIBRARIES(${project_BIN} ${project_LIBS} ${project_OS_SPECIFIC})
SET_TARGET_PROPERTIES(${project_BIN} PROPERTIES VERSION "${APPLICATION_VERSION_MAJOR}.${APPLICATION_VERSION_MINOR}" OUTPUT_NAME ${project_BIN} CLEAN_DIRECT_OUTPUT 1)

INSTALL(TARGETS ${project_BIN} DESTINATION bin)
