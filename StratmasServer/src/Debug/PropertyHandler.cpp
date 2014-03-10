// System
#include <fstream>
#include <cstring>

// Own
#include "PropertyHandler.h"
#include "debugheader.h"


using namespace std;


// Static Definitions
bool PropertyHandler::mUnitRandomWalk = false;
bool PropertyHandler::mValidateXML    = true;


/**
 * \brief Converts a string to a bool value.
 *
 * \param value The string to convert.
 * \return The bool value of the provided string.
 */
bool PropertyHandler::stringToBool(std::string& value)
{
#ifdef __win__
      if (_stricmp(value.c_str(), "true") == 0 ||
          _stricmp(value.c_str(), "on") == 0 ||
#else
      if (strcasecmp(value.c_str(), "true") == 0 ||
          strcasecmp(value.c_str(), "on") == 0 ||
#endif
          value == "1") {
          value = "true";
          return true;
     }
     else {
          value = "false";
          return false;
     } 
}

/**
 * \brief Gets properties on the form 'propertyname' = 'value' from
 * the specified file.
 *
 * \param filename The name of the file.
 * \return True if the file was read successfully, false otherwise.
 */
bool PropertyHandler::setPropertiesFromFile(const std::string& filename)
{
     string line;
     string prop;
     string value;

     ifstream ifs(filename.c_str());
     
     if (ifs.is_open()) {
          while (!ifs.eof()) {
               if (ifs.peek() == '#') {
                    getline(ifs, line);
               }
               else {
                    getline(ifs, prop, '=');
                    if (ifs.eof()) {
                         break;
                    }
                    getline(ifs, value);
                    unsigned int i = prop.find_first_not_of(' ');
                    prop = prop.substr(i, prop.find_last_not_of(' ') - i + 1);
                    i = value.find_first_not_of(' ');
                    value = value.substr(i, value.find_last_not_of(' ') - i + 1);
                    setProperty(prop, value);
               }
          }
          return true;
     }
     else {
          return false;
     }
}

/**
 * \brief Sets the specified property to the specifued value.
 *
 * \param property The name of the property to set.
 * \param value The value to use.
 */
void PropertyHandler::setProperty(string property, string value)
{
     bool set = true;

     if (property == "unitRandomWalk") {
          mUnitRandomWalk = stringToBool(value);
     }
     else if (property == "validateXML") {
          mValidateXML = stringToBool(value);
     }
     else {
          stratmasDebug("Unknown propery '" << property << "'");
          set = false;
     }

     if (set) {
          stratmasDebug("Property '" << property << "' set to " << value);
     }
}
