// Own
#include <cstdlib>
#include "AccessRightHandler.h"
#include "Error.h"
#include "Mapper.h"
#include "TypeFactory.h"


using namespace std;


// Static definitions
set<const Type*, lessTypeP> AccessRightHandler::sUnchangableTypes;


/**
 * \brief Checks if the object with the provided Reference is
 * changeable or not.
 *
 * \param ref The Reference to the object to check.
 * \return True if the object with the provided Reference is
 * changeable, false otherwise.
 */
bool AccessRightHandler::changeable(const Reference& ref)
{
     static bool firstTime = true;
     if (firstTime) {
	  firstTime = false;
	  sUnchangableTypes.insert(&TypeFactory::getType("Population"));
	  sUnchangableTypes.insert(&TypeFactory::getType("SquarePartitioner"));
     }

     DataObject* d = Mapper::map(ref);
     return (d ? (sUnchangableTypes.find(&d->getType()) == sUnchangableTypes.end()) : false);
}
