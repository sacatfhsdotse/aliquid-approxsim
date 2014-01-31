#ifndef _STRATMAS_PROPERTYHANDLER_H
#define _STRATMAS_PROPERTYHANDLER_H


// System
#include <string>


/**
 * \brief Handles different properties that may be set for the server.
 *
 * Mostly used for debuging purposes.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:09 $
 */
class PropertyHandler {
private:
     /// Set to true if units should move randomly [deafult false].
     static bool mUnitRandomWalk;

     /// Set to false if xml should not be validated [deafult true].
     static bool mValidateXML;

     static bool stringToBool(std::string& value);

public:
     /**
      * \brief Accessor for the unitRandomWalk property.
      *
      * \return The value of the unitRandomWalk property.
      */
     static bool unitRandomWalk() { return mUnitRandomWalk; }

     /**
      * \brief Accessor for the validateXML property.
      *
      * \return The value of the validateXML property.
      */
     static bool validateXML() { return mValidateXML; }
     static bool setPropertiesFromFile(const std::string& filename);
     static void setProperty(std::string property, std::string value);
};


#endif   // _STRATMAS_PROPERTYHANDLER_H
