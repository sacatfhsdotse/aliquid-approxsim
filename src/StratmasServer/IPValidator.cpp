// System
#include <fstream>
#include <iostream>
#include <sstream>

// Own
#include "Error.h"
#include "IPAddress.h"
#include "IPValidator.h"


using namespace std;


/**
 * \brief Finds out if the provided string may be interpreted as an IP
 * address.
 *
 * \param The string to check.
 * \return True if the string could be interpreted as an IP address,
 * false otherwise.
 */
bool IPValidator::isStringIP(const std::string& str)
{
     if (str.length() >= 7 && str[0] != '#') {
	  int part;
	  char dot = '.';
	  istringstream ist(str);
	  ist >> part;
	  for (int i = 0; i < 4; i++) {
	       if (part < 0 || part > 255 || dot != '.') {
		    return false;
	       }
	       ist >> dot >> part;
	  }
	  return true;
     }
     return false;
}

/**
 * \brief Gets valid ip numbers from the specified file.
 *
 * \param filename The name of the file.
 * \return True if the file was read successfully, false otherwise.
 */
bool IPValidator::getValidIPsFromFile(const std::string& filename)
{
     string line;

     ifstream ifs(filename.c_str());
     
     if (ifs.is_open()) {
	  bool atLeastOne = false;
	  while (!getline(ifs, line).eof()) {
	       if (line.length() > 0 && line[0] != '#') {
		    if (addValidIP(line)) {
			 atLeastOne = true;
		    }
	       }
	  }
	  return atLeastOne; 
     }
     else {
	  return false;
     }
}

/**
 * \brief Adds a valid ip number.
 *
 * \param ipToAdd The ip number to add.
 */
bool IPValidator::addValidIP(const std::string& ipToAdd)
{
     try {
	  IPAddress tmp(ipToAdd);
	  mIPSet.insert(tmp.toString());
	  return true;
     }
     catch (Error e) {
	  return false;
     }
}

/**
 * \brief Checks if an ip number is valid.
 *
 * \param ipToValidate The ip number to validate.
 * \return True if the ip numner is valid, false otherwise.
 */
bool IPValidator::isValidIP(const std::string& ipToValidate)
{
     IPAddress tmp(ipToValidate);
     return (mIPSet.find(tmp.toString()) != mIPSet.end());
}

