# <Copyright Holder>. Copyright (C) <Copyright Year(s)>. <License>.

#
# Project Properties
#
CMAKE_MINIMUM_REQUIRED (VERSION 2.6.2)
PROJECT (stratmas-server)
SET (APPLICATION_NAME ${PROJECT_NAME})
SET (APPLICATION_COPYRIGHT_YEARS "2014")
SET (APPLICATION_VERSION_MAJOR 7)
SET (APPLICATION_VERSION_MINOR 6)
SET (APPLICATION_VERSION_PATCH 0)
SET (APPLICATION_VERSION_STRING "${APPLICATION_VERSION_MAJOR}.${APPLICATION_VERSION_MINOR}.${APPLICATION_VERSION_PATCH}")
SET (APPLICATION_VENDOR_NAME "Försvarshögskolan")
SET (APPLICATION_VENDOR_URL "_")

option(TESTS "Enable tests build" OFF)
option(DEBUG "Enable program extra debug output" ON)
option(STATIC "Static linking" OFF)
option(STATIC_FORCED "Force static linking" OFF)

SET (CMAKE_VERBOSE_MAKEFILE 0) # Use 1 for compile debugging

IF(DEBUG)
  add_definitions(-DDEBUG)
ENDIF()

set (CMAKE_CONFIGURATION_TYPES Debug Release)
IF (NOT CMAKE_BUILD_TYPE)
  message(STATUS "Defaulting to Debug build")
  set (CMAKE_BUILD_TYPE "Debug")
ENDIF()

IF(STATIC)
  IF(WIN32)
    IF(STATIC_FORCED)
      set(CMAKE_FIND_LIBRARY_SUFFIXES ".lib" ".a")
    ELSE()
      set(CMAKE_FIND_LIBRARY_SUFFIXES ".lib" ".a" ${CMAKE_FIND_LIBRARY_SUFFIXES})
    ENDIF()
  ENDIF()
    IF(STATIC_FORCED)
      set(CMAKE_FIND_LIBRARY_SUFFIXES ".a")
    ELSE()
      set(CMAKE_FIND_LIBRARY_SUFFIXES ".a" ${CMAKE_FIND_LIBRARY_SUFFIXES})
    ENDIF()
  ELSE()
  IF(STATIC_FORCED)
    set(CMAKE_EXE_LINKER_FLAGS "-static")
  ENDIF()
  set(Boost_USE_STATIC_LIBS ON)
ENDIF()

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
  message("Boost not found locally, you should attempt to install it or let me download and compile during build (slow)")
  include(External-Boost)
  SET (Boost_LIBRARIES boost_date_time boost_filesystem boost_program_options boost_system boost_thread)
ENDIF()
INCLUDE_DIRECTORIES(${Boost_INCLUDE_DIRS})
LINK_DIRECTORIES(${Boost_LIBRARY_DIRS})

FIND_PACKAGE (APR)
IF (APR_FOUND)
#  message("Found Apache Runtime Environment")
# should not be needed but APRUTIL seems incapable at finding APR
  SET (APR_WITH --with-apr=/usr/)
ELSE()
  message("APR not found locally, you should attempt to install it or let me download and compile during build (slow)")
  include(ExternalProject)
  ExternalProject_Add(
    APR
    URL http://apache.mirrors.spacedump.net//apr/apr-1.5.0.tar.gz
    URL_MD5 6419a8f7e89ad51b5bad7b0c84cc818c
    CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR>
    PATCH_COMMAND patch -p1 -t -N < ${MAINFOLDER}/tools/share/cmake/apr-1.5.0-out-of-tree-build.patch 
    BUILD_COMMAND make -j${cores}
  )
  ExternalProject_Get_Property(APR install_dir)
  SET (APR_DEPEND APR)
  SET (APR_WITH --with-apr=${install_dir})
  SET (APR_INCLUDE_DIRS ${install_dir}/include)
  SET (APR_LIBRARY_DIRS ${install_dir}/lib)
  IF(STATIC)
    SET (APR_LIBRARIES libapr-1.a)
  ELSE()
    SET (APR_LIBRARIES apr-1)
  ENDIF()
ENDIF()
INCLUDE_DIRECTORIES($APR_INCLUDE_DIRS})
LINK_DIRECTORIES(${APR_LIBRARY_DIRS})

IF (APRUTIL_FOUND)
#  message("Found Apache Runtime Environment Utilities")
ELSE()
  message("APRUTIL not found locally, you should attempt to install it or let me download and compile during build (slow)")
  include(ExternalProject)
  ExternalProject_Add(
    APRUTIL
    URL http://apache.mirrors.spacedump.net//apr/apr-util-1.5.3.tar.gz
    URL_MD5 71a11d037240b292f824ba1eb537b4e3
    CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR> ${APR_WITH}
    BUILD_COMMAND make -j${cores}
    DEPENDS ${APR_DEPEND}
  )
  ExternalProject_Get_Property(APRUTIL install_dir)
  SET (APRUTIL_DEPEND APRUTIL)
  SET (APRUTIL_WITH --with-apr-util=${install_dir})
  SET (APRUTIL_INCLUDE_DIRS ${install_dir}/include)
  SET (APRUTIL_LIBRARY_DIRS ${install_dir}/lib)
  IF(STATIC)
    SET (APRUTIL_LIBRARIES libaprutil-1.a)
  ELSE()
    SET (APRUTIL_LIBRARIES aprutil-1)
  ENDIF()
ENDIF()
INCLUDE_DIRECTORIES($APRUTIL_INCLUDE_DIRS})
LINK_DIRECTORIES(${APRUTIL_LIBRARY_DIRS})

FIND_PACKAGE (Log4Cxx)
IF (Log4Cxx_FOUND)
#  message("Found Log4Cxx")
ELSE()
  message("Log4Cxx not found locally, you should attempt to install it or let me download and compile during build (slow)")
  include(ExternalProject)
  ExternalProject_Add(
    Log4Cxx
    #maybe use http://svn.apache.org/viewvc/incubator/log4cxx/tags/latest_stable/ ?
    URL http://apache.mirrors.spacedump.net/logging/log4cxx/0.10.0/apache-log4cxx-0.10.0.tar.gz
    URL_MD5 b30ffb8da3665178e68940ff7a61084c
    CONFIGURE_COMMAND <SOURCE_DIR>/configure --prefix=<INSTALL_DIR> ${APR_WITH} ${APRUTIL_WITH}
    PATCH_COMMAND patch -p1 -t -N < ${MAINFOLDER}/tools/share/cmake/Log4Cxx.patch
    BUILD_COMMAND make -j${cores}
    DEPENDS ${APR_DEPEND} ${APRUTIL_DEPEND}
  )
  ExternalProject_Get_Property(Log4Cxx install_dir)
  SET (Log4Cxx_INCLUDE_DIRS ${install_dir}/include)
  SET (Log4Cxx_LIBRARY_DIRS ${install_dir}/lib)
  IF(STATIC)
    SET (Log4Cxx_LIBRARIES liblog4cxx.a)
  ELSE()
    SET (Log4Cxx_LIBRARIES log4cxx)
  ENDIF()
ENDIF()
INCLUDE_DIRECTORIES(${Log4Cxx_INCLUDE_DIRS})
LINK_DIRECTORIES(${Log4Cxx_LIBRARY_DIRS})

if(TESTS)
  FIND_PACKAGE (UnitTestPlusPlus)
  IF (UnitTestPlusPlus_FOUND)
#    message("Found UnitTestPlusPlus")
  ELSE()
    message("UnitTestPlusPlus not found locally, you should attempt to install it or let me download and compile during build (slow)")
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
    IF(STATIC)
      SET (UnitTestPlusPlus_LIBRARIES libUnitTest++.a)
    ELSE()
      SET (UnitTestPlusPlus_LIBRARIES UnitTest++)
    ENDIF()
  ENDIF()
  INCLUDE_DIRECTORIES(${UnitTestPlusPlus_INCLUDE_DIRS})
  LINK_DIRECTORIES(${UnitTestPlusPlus_LIBRARY_DIRS})
endif()

FIND_PACKAGE (XercesC)
IF (XERCESC_FOUND)
#  message("Found XercesC")
ELSE()
  message("XercesC not found locally, you should attempt to install it or let me download and compile during build (slow)")
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
  IF(STATIC)
    SET (XERCESC_LIBRARIES libxerces-c.a)
  ELSE()
    SET (XERCESC_LIBRARIES xerces-c)
  ENDIF()
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

