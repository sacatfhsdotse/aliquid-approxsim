#ifndef STRATMAS_STRX_H
#define STRATMAS_STRX_H


// System
#include <cstring>
#include <ostream>
#include <string>

// Own
#include "Error.h"

// Xerces
#include <xercesc/util/XercesDefs.hpp>
#include <xercesc/util/XMLString.hpp>
#include <xercesc/util/PlatformUtils.hpp>
#include <xercesc/util/TransService.hpp>


XERCES_CPP_NAMESPACE_USE


/**
 * \brief Wraps an XMLTranscoder object to be used by the StrX and
 * XStr classes.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/12 11:54:20 $
 */
class TranscoderWrapper {
private:
     /**
      * \brief Block size used internally by the parser. Should
      * according to xercesc documentation be in the 4 to 64k range.
      */
     static const int kBlockSize = 1024 * 16;

     // The maximum size of a character in the current encoding.
     static unsigned int sMaxCharSize;

     /// Pointer to the transcoder object.
     static XMLTranscoder* sTranscoder;

public:
     /**
      * \brief Sets the encoding to use when transcoding strings with
      * the StrX and XStr classes.
      *
      * \param encoding The name of the encoding, for example 'ISO-8859-1'.
      * \param maxCharSize The maximum size of a character in the
      * given encoding.
      */
     static void setEncoding(const XMLCh* encoding, unsigned int maxCharSize) {
	  sMaxCharSize = maxCharSize;
	  XMLTransService::Codes res;
	  XMLTranscoder* tmp = XMLPlatformUtils::fgTransService->makeNewTranscoderFor(encoding, res, kBlockSize);
	  if (sTranscoder) {
	       delete sTranscoder;
	  }
	  sTranscoder = tmp;
     }
     
     /**
      * \brief Sets the encoding to use when transcoding strings with
      * the StrX and XStr classes.
      *
      * \param encoding The name of the encoding, for example 'ISO-8859-1'.
      * \param maxCharSize The maximum size of a character in the
      * given encoding.
      */
     static void setEncoding(const char* encoding, unsigned int maxCharSize) {
	  sMaxCharSize = maxCharSize;
	  XMLTransService::Codes res;
	  XMLTranscoder* tmp = XMLPlatformUtils::fgTransService->makeNewTranscoderFor(encoding, res, kBlockSize);
	  if (sTranscoder) {
	       delete sTranscoder;
	  }
	  sTranscoder = tmp;
     }
     
     /**
      * \brief Accessor for the transcoder object.
      *
      * \return The transcoder object.
      */
     static XMLTranscoder* getTranscoder() { return sTranscoder; }

     /**
      * \brief Accessor for the block size.
      *
      * \return The block size.
      */
     static unsigned int getBlockSize() { return kBlockSize; }

     /**
      * \brief Accessor for the maximum char size of the current
      * encoding.
      *
      * \return The maximum char size of the current encoding.
      */
     static unsigned int getMaxCharSize() { return sMaxCharSize; }
};


/**
 * \brief This is a simple class for transcoding of XMLCh data to
 * local code page for display.
 *
 * Inspired by the StrX class in the DOMCount example in the Xerces-c
 * distribution
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/12 11:54:20 $
 */
class StrX {
private :
     /// Default size of a block to transcode.
     static const unsigned int kBlock = 256;

     /// Buffer for the string in char array form.
     char mStr[kBlock];

     /**
      * \brief Pointer to allocated memory if string is to large to
      * fit in mStr.
      */ 
     char* mLongStr;
     
public :
     /**
      * \brief Constructor that transcodes an XMLCh string to a char string
      *
      * \param toTranscode The string to transcode
      */
     StrX(const XMLCh* const toTranscode) : mLongStr(0) {
	  int maxCharSize = TranscoderWrapper::getMaxCharSize();
	  unsigned int charsEaten;
	  unsigned int charsToTranscode = XMLString::stringLen(toTranscode) + 1;
	  if (charsToTranscode <= kBlock / maxCharSize) {
	       TranscoderWrapper::getTranscoder()->transcodeTo(toTranscode,
							       (const XMLSize_t)charsToTranscode,
							       (XMLByte*)mStr,
							       (const XMLSize_t)kBlock,
							       (XMLSize_t &)charsEaten,
							       XMLTranscoder::UnRep_RepChar);
	  }
	  else {
	       mLongStr = new char[charsToTranscode * sizeof(XMLCh)];
	       unsigned int chunkSize;
	       char* writeHere = mLongStr;
	       unsigned int readBytes;
	       for (unsigned int i = 0; i < charsToTranscode; i += charsEaten) {
		    chunkSize = (charsToTranscode - i < TranscoderWrapper::getBlockSize() ? 
				 charsToTranscode - i : TranscoderWrapper::getBlockSize());
		    readBytes = TranscoderWrapper::getTranscoder()->transcodeTo(toTranscode + i,
										(const XMLSize_t)chunkSize,
										(XMLByte*)writeHere,
										(const XMLSize_t)(charsToTranscode - i) * maxCharSize,
										(XMLSize_t &)charsEaten,
										XMLTranscoder::UnRep_RepChar);
		    writeHere += readBytes;
	       }
	  }
     }

     /**
      * \brief Destructor
      */
     ~StrX() {
	  if (mLongStr) {
	       delete [] mLongStr;
	  }
     }
     
     /**
      * \brief Accessor for the string
      *
      * \return The string as a char array
      */
     const char* str() const { return (mLongStr ? mLongStr : mStr); }

     /**
      * \brief Comparsion operator for char arrays
      *
      * \param str The string to compare to.
      * \return true if the strings are lexicographically equal, false otherwise.
      */
     bool operator == (const char *str) { return (strcmp(this->str(), str) == 0); }

     // Friends
     /// For printing this string to an ostream
     friend std::ostream &operator << (std::ostream &o, const StrX &s) { return o << s.str(); }
     /// For printing this string to an Error
     friend Error        &operator << (Error &e, const StrX &s)  { return e << s.str(); }
};

/**
 * \brief This is a simple class for transcoding of char arrays to
 * XMLCh strings.
 *
 * Inspired (very much) by the StrX class in the DOMCount example in
 * the Xerces-c distribution
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/12 11:54:20 $
 */
class XStr {
private:
     /// Default size of a block to transcode.
     static const unsigned int kBlock = 256;

     /// Buffer for the string in XMLCh array form.
     XMLCh mStr[kBlock];

     /**
      * \brief Pointer to allocated memory if string is to large to
      * fit in mStr.
      */ 
     XMLCh* mLongStr;

     /// Never really used but required by transcodeFrom() call.
     unsigned char mNumBytesPerChar[kBlock];

     /**
      * \brief Converts the provided string to XMLCh using the
      * XMLTranscoder given by TranscoderWrapper::getTranscoder().
      *
      * \param toTranscode The string to transcode.
      */
     void transcode(const char* const toTranscode) {
	  if (mLongStr) {
	       delete [] mLongStr;
	  }
	  unsigned int bytesEaten;
	  unsigned int bytesToTranscode = strlen(toTranscode) + 1;
	  if (bytesToTranscode <= kBlock) {
	       mLongStr = 0;
	       TranscoderWrapper::getTranscoder()->transcodeFrom((XMLByte*)toTranscode,
								 (const XMLSize_t)bytesToTranscode,
								 mStr,
								 (const XMLSize_t)kBlock,
								 (XMLSize_t &)bytesEaten,
								 mNumBytesPerChar);
	  }
	  else {
	       mLongStr = new XMLCh[bytesToTranscode];
	       unsigned char* numBytesPerChar = new unsigned char[bytesToTranscode];
	       XMLCh* writeHere = mLongStr;
	       unsigned int readChars;
	       unsigned int chunkSize;
	       for (unsigned int i = 0; i < bytesToTranscode; i += bytesEaten) {
		    chunkSize = (bytesToTranscode - i < TranscoderWrapper::getBlockSize() ? 
				 bytesToTranscode - i : TranscoderWrapper::getBlockSize());
		    readChars = TranscoderWrapper::getTranscoder()->transcodeFrom((XMLByte*)(toTranscode + i),
										  (const XMLSize_t)chunkSize,
										  writeHere,
										  (const XMLSize_t)bytesToTranscode - i,
										  (XMLSize_t &)bytesEaten,
										  numBytesPerChar);
		    writeHere += readChars;
	       }
	  }
     }

public:
     /**
      * \brief Constructor that transcodes a char array to an XMLCh
      * string.
      *
      * \param toTranscode The string to transcode
      */
     XStr(const char* const toTranscode) : mLongStr(0) {
	  transcode(toTranscode);
     }
     /**
      * \brief Constructor that transcodes a std::string to an XMLCh
      * string.
      *
      * \param toTranscode The string to transcode
      */
     XStr(const std::string toTranscode) : mLongStr(0) {
	  transcode(toTranscode.c_str());
     }

     /**
      * \brief Destructor
      */
     ~XStr() {
	  if (mLongStr) {
	       delete [] mLongStr;
	  }
     }

     /**
      * \brief Accessor for the string
      *
      * \return The string as an XMLCh array.
      */
     const XMLCh* str() const { return (mLongStr ? mLongStr : mStr); }
};


#endif   // STRATMAS_STRX_H
