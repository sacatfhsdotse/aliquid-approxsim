#---------------------------------------------------------------------------
# Get and build boost

set( Boost_Bootstrap_Command )
if( UNIX )
  set( Boost_Bootstrap_Command ./bootstrap.sh )
  set( Boost_b2_Command ./b2 )
else()
  if( WIN32 )
    set( Boost_Bootstrap_Command bootstrap.bat )
    set( Boost_b2_Command b2.exe )
  endif()
endif()

include(ExternalProject)
ExternalProject_Add(
  Boost
  URL "http://sourceforge.net/projects/boost/files/boost/1.55.0/boost_1_55_0.tar.gz/download"
  URL_MD5 93780777cfbf999a600f62883bd54b17
  BUILD_IN_SOURCE 1
  UPDATE_COMMAND ""
  PATCH_COMMAND ""
  CONFIGURE_COMMAND ${Boost_Bootstrap_Command}
  BUILD_COMMAND  ${Boost_b2_Command} install
    --without-python
    --without-mpi
    --disable-icu
    --prefix=<INSTALL_DIR>
    --threading=single,multi
    --link=shared
    --variant=release
    -j${cores}
  INSTALL_COMMAND ""
#  INSTALL_COMMAND ${Boost_b2_Command} install 
#    --without-python
#    --without-mpi
#    --disable-icu
#    --prefix=${CMAKE_BINARY_DIR}/INSTALL
#    --threading=single,multi
#    --link=shared
#    --variant=release
#    -j8
)


ExternalProject_Get_Property(Boost install_dir)
if( NOT WIN32 )
  set(Boost_LIBRARY_DIRS ${install_dir}/lib )
  set(Boost_INCLUDE_DIRS ${install_dir}/include )
else()
  set(Boost_LIBRARY_DIRS ${install_dir}/lib )
  set(Boost_INCLUDE_DIRS ${install_dir}/include ) #/boost-1_55/
endif()
