#ifndef STRATMAS_SUBSCRIPTION_H
#define STRATMAS_SUBSCRIPTION_H

// System
#include <iosfwd>
#include <list>
#include <string>

// Xerces-c
#include <xercesc/dom/DOMElement.hpp>

// Own
#include "GridPos.h"


// Forward Declarations
class Buffer;
class CellGroup;
class ChangeTrackerAdapter;
class EthnicFaction;


using namespace XERCES_CPP_NAMESPACE;


/**
 * \brief Abstract base class for Subscriptions.
 *
 * All Subscriptions must implement the 'getSubscribedData()' method
 * that fetches data from the Buffer mBuf and produces an XML
 * representation of that data in accordance to the
 * stratmasProtocol.xsd schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/05 14:49:47 $
 */
class Subscription {     
protected:
     Buffer       &mBuf;   ///< Reference to the Buffer.
     int          mId;     ///< The id of this Subscription

public:
     Subscription(DOMElement *n, Buffer &buf);

     /// Destructor.
     virtual ~Subscription() {}

     /**
      * \brief Accessor for the id of this Subscription.
      *
      * \return The id for this Subscription.
      */
     int id() const { return mId; }

     /**
      * \brief Writes an XML representation of the subscribed data to
      * the provided stream.
      *
      * \param o The stream to write to.
      */
     virtual void getSubscribedData(std::ostream &o) = 0;
};



/**
 * \brief Subscription for individual SimulationObjects.
 *
 * Currently only used by the evolver.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/05 14:49:47 $
 */
class StratmasObjectSubscription : public Subscription {
private:
     /// The ChangeTrackerAdapter for the subscribed object.
     ChangeTrackerAdapter* mData;

public:
     StratmasObjectSubscription(DOMElement* n, Buffer& buf, int64_t id);
     ~StratmasObjectSubscription();
     void getSubscribedData(std::ostream& o);
};


/**
 * \brief LayerSubscription represents a subscription for one grid
 * layer, e.g one process variable for all active cells.
 *
 * Due to performance considerations the layer is represented as a
 * Base64 encoded array of doubles where the first element in the
 * array is the value for the top left active cell, the second is
 * the value for the top second left cell etc. down to the bottom
 * right active cell. mLayer is the name of the layer and mFaction
 * is the Reference that identifies which faction the subscription
 * refers to.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/05 14:49:47 $
 */
class LayerSubscription : public Subscription {
private:
     std::string      mLayer;     ///< The name of the process variable
     const Reference* mFaction;   ///< The faction this Subscription refers to
     unsigned int     mLength;    ///< The number of cells of interest.
     int32_t*         mIndex;     ///< The indices (among active cells) of the cells of interest.
     bool             mSessionBigEndian;   ///< Keeps track of if we have to swap byte order

public:
     LayerSubscription(DOMElement *n, Buffer &buf, bool sbe);
     
     /// Destructor.
     ~LayerSubscription() { delete mIndex; }
     void getSubscribedData(std::ostream &o);
};


/**
 * \brief RegionSubscription represents a subscription for a
 * collection of cells.
 *
 * When the RegionSubscription is created it finds out which cells the
 * specified Region overlaps and adds them to an AttributesGroup.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/05 14:49:47 $
 */
class RegionSubscription : public Subscription {
private:
     std::list<GridPos> mPositions;   ///< A list of grid positions this subscription covers.
     CellGroup *mRegion;      ///< The group of cells this Subscription refers to.
     int mResetCount;                 ///< The reset count for the Buffer.

     void printPV(std::ostream& o, const char* name, const char* type, double value, EthnicFaction* faction = 0);

public:
     RegionSubscription(DOMElement *n, Buffer &buf);
     ~RegionSubscription();
     void getSubscribedData(std::ostream &o);
};


#endif   // STRATMAS_SUBSCRIPTION_H
