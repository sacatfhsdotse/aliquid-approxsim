// Own

#include "DataObjectImpl.h"
#include "Error.h"
#include "Faction.h"
#include "Mapper.h"
#include "Shape.h"
#include "SOMapper.h"
#include "PVRegion.h"
#include "XMLHelper.h"


using namespace std;


// Static Definitions
PVInitValueSet* PVInitValueSet::sCurrentSet(0);


/**
 * brief Constructor
 *
 * \param n The DOMElement to create this object from.
 */
PVRegion::PVRegion(const DOMElement* n) : mValue(0), mShapeRef(0), mArea(0)
{
     mValue = XMLHelper::getDouble(*n, "value");
     //approxsimDebug("value: " << mValue);
     string type = XMLHelper::getTypeAttribute(*n);
     if (type == "ESRIRegion") {
          mShapeRef = &Reference::get(XMLHelper::getFirstChildByTag(*n, "reference"));
     }
     else if (type == "CreatedRegion") {
          mArea = XMLHelper::getShape(*n, "shape", Reference::nullRef());
     }
     else {
          Error e;
          e << "Unknown region type '" << type << "' in PVInitValue";
          throw e;
     }
}

/**
 * brief Destructor
 */
PVRegion::~PVRegion()
{
     if (mArea) {
          delete mArea;
     }
}

/**
 * \brief Accessor for the Shape the modifications refer to.
 *
 * \return The Shape.
 */
const Shape& PVRegion::area() const
{
     if (!mArea) {
          // Since subshapes of composites are not mapped in the
          // Mapper we have to look for them explicitly. So, first
          // look upwards in the reference hierarchy and if we find a
          // composite we call getPart that in turn goes down the
          // hierarchy again inside the composite.
          CompositeShape* comp = 0;
          for (const Reference* r = mShapeRef ; *r != Reference::root(); r = r->scope()) {
               ApproxsimShape* shape = dynamic_cast<ApproxsimShape*>(Mapper::map(*r));
               if (shape) {
                    comp = dynamic_cast<CompositeShape*>(shape->getShape());
                    if (comp) {
                         break;
                    }
               }
          }
          if (comp) {
               // comp is a clone so we must delete it but we still
               // want a copy of the subshape so let's clone it.
               mArea = comp->getPart(*mShapeRef)->clone();
               delete comp;
          }
          if (!mArea) {
               Error e;
               e << "Couldn't map the reference '" << *mShapeRef << " to any Shape in PVRegion";
               throw e;
          }
     }
     return *mArea;
}

/**
 * brief Constructor
 *
 * \param n The DOMElement to create this object from.
 */
PVInitValue::PVInitValue(const DOMElement* n)
{
     string pvName;
     XMLHelper::getString(*XMLHelper::getFirstChildByTag(*n, "pv"), "name", pvName);
     mPV = PVHelper::displayNameToOverAllOrder(pvName);
     //approxsimDebug("pvName: " << pvName<< ", pvIndex: " << mPV);
     vector<DOMElement*> elems;
     XMLHelper::getChildElementsByTag(*n, "faction", elems);
     for(vector<DOMElement*>::const_iterator it = elems.begin(); it != elems.end(); ++it) {
          mFactions.push_back(&Reference::get(*it));
     }
     elems.clear();
     XMLHelper::getChildElementsByTag(*n, "regions", elems);
     for(vector<DOMElement*>::const_iterator it = elems.begin(); it != elems.end(); ++it) {
          mRegions.push_back(new PVRegion(*it));
     }
     
}

/**
 * brief Destructor
 */
PVInitValue::~PVInitValue()
{
     for(vector<PVRegion*>::const_iterator it = mRegions.begin(); it != mRegions.end(); ++it) {
          delete *it;
     }
}

/**
 * brief Constructor
 *
 * \param n The DOMElement to create this object from.
 */
PVInitValueSet::PVInitValueSet(const DOMElement* n)
{
     vector<DOMElement*> elems;
     XMLHelper::getChildElementsByTag(*n, "pvinitvalues", elems);
     for(vector<DOMElement*>::const_iterator it = elems.begin(); it != elems.end(); ++it) {
          mInitValues.push_back(new PVInitValue(*it));
     }
     sCurrentSet = this;
}

/**
 * brief Destructor
 */
PVInitValueSet::~PVInitValueSet()
{
     for(vector<PVInitValue*>::iterator it = mInitValues.begin(); it != mInitValues.end(); ++it) {
          delete *it;
     }
     if (sCurrentSet == this) {
          sCurrentSet = 0;
     }
}
