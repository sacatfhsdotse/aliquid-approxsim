// Own
#include "Buffer.h"
#include "DataObject.h"
#include "Disease.h"
#include "Update.h"


using namespace std;


/**
 * \brief Creates a Disease from the provided DataObject.
 *
 * \param d The DataObject to use for construction.
 */
Disease::Disease(const DataObject& d)
     : SimulationObject(d),
       mDescription(d.getChild("description")->getString()),
       mInfectionRate(d.getChild("infectionRate")->getDouble()),
       mRecoveryRate (d.getChild("recoveryRate")->getDouble()),
       mMortalityRate(d.getChild("mortalityRate")->getDouble())
{
}

/**
 * \brief Updates this object.
 *
 * \param u The Update to update this object with.
 */
void Disease::update(const Update& u)
{
     const string& attr = u.getReference().name();
     if (u.getType() == Update::eModify) {
          if (attr == "description") {
               mDescription = u.getObject()->getString();
          }
          else if (attr == "infectionRate") {
               mInfectionRate = u.getObject()->getDouble();
          }
          else if (attr == "recoveryRate") {
               mRecoveryRate = u.getObject()->getDouble();
          }
          else if (attr == "mortalityRate") {
               mMortalityRate = u.getObject()->getDouble();
          }
          else {
               Error e;
               e << "No updatable attribute '" << attr << "' in '" << ref() << "'";
               throw e;
          }
     }
     else {
          Error e;
          e << "Invalid Disease Update (type:" << u.getTypeAsString();
          e << ", object: " << attr << ").";
          throw e;
     }
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void Disease::extract(Buffer &b) const
{
     DataObject& me = *b.map(ref());
     me.getChild("description"  )->setString(mDescription  );
     me.getChild("infectionRate")->setDouble(mInfectionRate);
     me.getChild("recoveryRate" )->setDouble(mRecoveryRate );
     me.getChild("mortalityRate")->setDouble(mMortalityRate);
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void Disease::reset(const DataObject& d)
{
     mDescription = d.getChild("description")->getString();
     mInfectionRate = d.getChild("infectionRate")->getDouble();
     mRecoveryRate = d.getChild("recoveryRate")->getDouble();
     mMortalityRate = d.getChild("mortalityRate")->getDouble();
}
