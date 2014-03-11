# <Copyright Holder>. Copyright (C) <Copyright Year(s)>. <License>.

#
# Project Properties
#
CMAKE_MINIMUM_REQUIRED (VERSION 2.6.2)
PROJECT (stratmas-server)
SET (APPLICATION_NAME ${PROJECT_NAME})
SET (APPLICATION_COPYRIGHT_YEARS "2014")
SET (APPLICATION_VERSION_MAJOR 7)
SET (APPLICATION_VERSION_MINOR 5)
SET (APPLICATION_VERSION_PATCH 1)
SET (APPLICATION_VERSION_STRING "${APPLICATION_VERSION_MAJOR}.${APPLICATION_VERSION_MINOR}.${APPLICATION_VERSION_PATCH}")
SET (APPLICATION_VENDOR_NAME "Your Company")
SET (APPLICATION_VENDOR_URL "yourcompany.com")

option(TESTS "Enable tests build" OFF)
option(DEBUG "Enable debug output" OFF)

IF(DEBUG)
  add_definitions(-DDEBUG)
ENDIF()

set (CMAKE_CONFIGURATION_TYPES Debug Release)
set (CMAKE_BUILD_TYPE Debug)

#
# CMake Debugging Options
#
SET (CMAKE_VERBOSE_MAKEFILE 1) # Use 1 for debugging, 0 for release

#
# Project Output Paths
#
SET (MAINFOLDER ${PROJECT_SOURCE_DIR})
SET (EXECUTABLE_OUTPUT_PATH "${MAINFOLDER}/bin")
#SET (LIBRARY_OUTPUT_PATH "${MAINFOLDER}/lib")

#
# Project Search Paths
#
LIST (APPEND CMAKE_PREFIX_PATH "${MAINFOLDER}")
LIST (APPEND CMAKE_PREFIX_PATH "${MAINFOLDER}/tools")
LIST (APPEND CMAKE_PREFIX_PATH "${MAINFOLDER}/lib")
LIST (APPEND CMAKE_MODULE_PATH "${MAINFOLDER}/tools/share/cmake")
#INCLUDE_DIRECTORIES("${MAINFOLDER}/include")

include(ProcessorCount)
ProcessorCount(cores)
if(cores EQUAL 0)
  message("Unable to determine amount of cpu cores, assuming 1")
  SET(cores 1)
endif()

#
# Locate Project Prerequisites 
#
SET (Boost_ADDITIONAL_VERSIONS "1.39" "1.39.0" "1.40" "1.40.0" "1.41" "1.41.0" "1.42" "1.42.0" "1.43" "1.43.0" "1.44" "1.44.0" "1.45" "1.45.0" "1.46" "1.46.0" "1.47" "1.47.0" "1.48" "1.48.0" "1.49" "1.49.0" "1.50" "1.50.0" "1.51" "1.51.0" "1.52" "1.52.0" "1.53" "1.53.0" "1.54" "1.54.0" "1.55" "1.55.0")
SET (Boost_USE_MULTITHREADED ON)
FIND_PACKAGE (Boost 1.37 COMPONENTS "date_time" "filesystem" "program_options" "system" "thread")

IF (Boost_FOUND)
#  message("Found Boost")
ELSE()
  message(WARNING "Boost not found locally, you should attempt to install it or let me download and compile during build")
  include(External-Boost)
  SET (Boost_LIBRARIES boost_date_time boost_filesystem boost_program_options boost_system boost_thread)
ENDIF()
INCLUDE_DIRECTORIES(${Boost_INCLUDE_DIRS})
LINK_DIRECTORIES(${Boost_LIBRARY_DIRS})

FIND_PACKAGE (Log4Cxx)
IF (${Log4Cxx_FOUND})
#  message("Found Log4Cxx")
ELSE()
  message(WARNING "Log4Cxx not found locally, you should attempt to install it or let me download and compile during build")
  include(ExternalProject)
  ExternalProject_Add(
    Log4Cxx
    #maybe use http://svn.apache.org/viewvc/incubator/log4cxx/tags/latest_stable/ ?
    URL http://apache.mirrors.spacedump.net/logging/log4cxx/0.10.0/apache-log4cxx-0.10.0.tar.gz
    URL_MD5 b30ffb8da3665178e68940ff7a61084c
    CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR>
    BUILD_COMMAND make -j${cores}
  )
  ExternalProject_Get_Property(Log4Cxx install_dir)
  SET (Log4Cxx_INCLUDE_DIRS ${install_dir}/include)
  SET (Log4Cxx_LIBRARY_DIRS ${install_dir}/lib)
  SET (Log4Cxx_LIBRARIES xerces-c)
ENDIF()
INCLUDE_DIRECTORIES(${Log4Cxx_INCLUDE_DIRS})
LINK_DIRECTORIES(${Log4Cxx_LIBRARY_DIRS})

if(TESTS)
  FIND_PACKAGE (UnitTestPlusPlus)
  IF (${UnitTestPlusPlus_FOUND})
#    message("Found UnitTestPlusPlus")
  ELSE()
    message(WARNING "UnitTestPlusPlus not found locally, you should attempt to install it or let me download and compile during build")
    include(ExternalProject)
    ExternalProject_Add(
      UnitTestPlusPlus
      GIT_REPOSITORY https://github.com/unittest-cpp/unittest-cpp.git
      UPDATE_COMMAND ""
      CMAKE_ARGS -DCMAKE_INSTALL_PREFIX:PATH=<INSTALL_DIR> -Dj${cores}
    )
    ExternalProject_Get_Property(UnitTestPlusPlus install_dir)
		SET (UnitTestPlusPlus_INCLUDE_DIRS ${install_dir}/include)
		SET (UnitTestPlusPlus_LIBRARY_DIRS ${install_dir}/lib)
    SET (UnitTestPlusPlus_LIBRARIES UnitTest++)
  ENDIF()
  INCLUDE_DIRECTORIES(${UnitTestPlusPlus_INCLUDE_DIRS})
  LINK_DIRECTORIES(${UnitTestPlusPlus_LIBRARY_DIRS})
endif()

FIND_PACKAGE (XercesC)
IF (${XercesC_FOUND})
#  message("Found XercesC")
ELSE()
  message(WARNING "XercesC not found locally, you should attempt to install it or let me download and compile during build")
  include(ExternalProject)
  ExternalProject_Add(
    XercesC
    URL http://apache.mirrors.spacedump.net/xerces/c/3/sources/xerces-c-3.1.1.tar.gz
    URL_MD5 6a8ec45d83c8cfb1584c5a5345cb51ae
    CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR>
    BUILD_COMMAND make -j${cores}
  )
  ExternalProject_Get_Property(XercesC install_dir)
  SET (XercesC_INCLUDE_DIRS ${install_dir}/include)
  SET (XercesC_LIBRARY_DIRS ${install_dir}/lib)
  SET (XercesC_LIBRARIES xerces-c)
ENDIF()
INCLUDE_DIRECTORIES(${XercesC_INCLUDE_DIRS})
LINK_DIRECTORIES(${XercesC_LIBRARY_DIRS})


#
# Build settings
#

set(DEBUG_FLAGS "-g -O1 -Wall -std=c++11")
set(RELEASE_FLAGS "-O3 -std=c++11")

set(CMAKE_CXX_FLAGS_DEBUG ${DEBUG_FLAGS})
set(CMAKE_C_FLAGS_DEBUG ${DEBUG_FLAGS})

set(CMAKE_CXX_FLAGS_RELEASE ${RELEASE_FLAGS})
set(CMAKE_C_FLAGS_RELEASE ${RELEASE_FLAGS})

#
# Configure Files
#
FILE (GLOB_RECURSE CONFIGINPUTS3 src/*.in.h.cmake)
FILE (GLOB_RECURSE CONFIGINPUTS4 src/*.h.in.cmake)
if(TESTS)
  FILE (GLOB_RECURSE CONFIGINPUTS5 test/*.in.h.cmake)
  FILE (GLOB_RECURSE CONFIGINPUTS6 test/*.h.in.cmake)
endif()
LIST (APPEND CONFIGINPUTS "${CONFIGINPUTS3}")
LIST (APPEND CONFIGINPUTS "${CONFIGINPUTS4}")
LIST (APPEND CONFIGINPUTS "${CONFIGINPUTS5}")
LIST (APPEND CONFIGINPUTS "${CONFIGINPUTS6}")
FOREACH (CONFIGINPUT ${CONFIGINPUTS})
    STRING (REPLACE ".in.h.cmake" ".h" CONFIGOUTPUT1 "${CONFIGINPUT}")
    STRING (REPLACE ".h.in.cmake" ".h" CONFIGOUTPUT2 "${CONFIGOUTPUT1}")
    CONFIGURE_FILE ("${CONFIGINPUT}" "${CONFIGOUTPUT2}")
ENDFOREACH (CONFIGINPUT ${CONFIGINPUTS})

#
# Add Build Targets
#
ADD_SUBDIRECTORY(src)
if(TESTS)
  ADD_SUBDIRECTORY(test)
endif()

#
# Add Install Targets
#
IF (EXISTS "${MAINFOLDER}/include/${PROJECT_NAME}" AND IS_DIRECTORY "${MAINFOLDER}/include/${PROJECT_NAME}")
    INSTALL(DIRECTORY "${MAINFOLDER}/include/${PROJECT_NAME}" DESTINATION "include")
ENDIF (EXISTS "${MAINFOLDER}/include/${PROJECT_NAME}" AND IS_DIRECTORY "${MAINFOLDER}/include/${PROJECT_NAME}")

#
# Add Documentation Targets
#
INCLUDE (DocumentationTargets)

