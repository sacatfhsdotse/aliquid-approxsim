// This file is included in XMLHandler.cpp and thus 'compiled there'

// System
#include <iostream>

// Own
#include "Buffer.h"
#include "CellGroup.h"
#include "ChangeTrackerAdapter.h"
#include "CombatGrid.h"
#include "debugheader.h"
#include "Faction.h"
#include "Grid.h"
#include "GridCell.h"
#include "GridDataHandler.h"
#include "Shape.h"
#include "StrX.h"
#include "Server.h"
#include "Subscription.h"
#include "XMLHelper.h"

// Xerces-c
#include <xercesc/util/Base64.hpp>
#include <xercesc/util/XMLString.hpp>


using namespace std;


/**
 * Base class constructor. Reads the subscriptionId.
 *
 * \param n The DOMElement to create the Subscription from.
 * \param buf The Buffer to fetch data from.
 */
Subscription::Subscription(DOMElement *n, Buffer &buf) : mBuf(buf)
{
     if (!n) {
	  Error e(Error::eWarning);
	  e << "Invalid Stratmas message. Null element node in"
	    << "Subscription::Subscription()";
	  throw e;
     }

     // Get the id for this Subscription
     mId = XMLHelper::getIntAttribute(*n, "id");
}



/**
 * \brief Creates a subscription from a DOMElement, e.g. an xml
 * representation.
 *
 * \param n The DOMElement from which this subscription should be
 * created.
 * \param buf The Buffer from which data should be fetched.
 * \param id The id of the Session holding this Subscription.
 */
StratmasObjectSubscription::StratmasObjectSubscription(DOMElement* n, Buffer& buf, int64_t id)
     : Subscription(n, buf), mData(0)
{
     DOMElement* elem = XMLHelper::getFirstChildByTag(*n, "reference");
     const Reference& ref = Reference::get(elem);
     mData = ChangeTrackerAdapterFactory::createChangeTrackerAdapter(ref, id);
}

/**
 * \brief Destructor.
 */
StratmasObjectSubscription::~StratmasObjectSubscription()
{
     if (mData) { delete mData; }
}

/**
 * \brief Writes an XML representation of the subscribed data to the
 * provided stream.
 *
 * \param o The stream to write to.
 */
void StratmasObjectSubscription::getSubscribedData(std::ostream &o)
{
     debug("In SOSub:");
     if (mData->changed()) {
	  debug("    Changed == true");
	  o << "<subscribedData xsi:type=\"sp:SubscribedStratmasObjectData\" id=\"" << mId << "\">" << endl;
	  mData->toXML(o);
	  o << "</subscribedData>" << endl;
     }
}


/**
 * \brief Creates a subscription from a DOMElement, e.g. an xml
 * representation.
 *
 * \param n The DOMElement from which this subscription should be
 * created.
 * \param buf The Buffer from which data should be fetched.
 * \param sbe True if the client that submitted this subscription
 * runs on a big endian plattform.
 */
LayerSubscription::LayerSubscription(DOMElement* n, Buffer& buf, bool sbe)
     : Subscription(n, buf), mSessionBigEndian(sbe)
{
     XMLHelper::getString(*n, "layer", mLayer);

     // Optional faction- if no faction element was found then we mean faction 'all'
     DOMElement* factionElem = XMLHelper::getFirstChildByTag(*n, "faction");
     Faction* fac = &EthnicFaction::all();
     if (!fac) {
	  Error e;
	  e << "Tried to subscribe to Faction 'all' that does not exist";
	  throw e;
     }
     mFaction = (factionElem ? &Reference::get(factionElem) : &fac->ref());

     DOMElement* elem = XMLHelper::getFirstChildByTag(*n, "index");
     if (elem) {
	  XMLCh* data = Base64::decode(XMLHelper::getXMLChString(*n, "index"), &mLength);
	  mIndex = reinterpret_cast<int32_t*>(data);
     }
     else {
	  mLength = mBuf.grid().active();
	  mIndex = 0;
     }
     debug("Created LayerSubscription for layer '" << mLayer << "', faction: '" << *mFaction << "'");
}

/**
 * \brief Writes an XML representation of the subscribed data to the
 * provided stream.
 *
 * \param o The stream to write to.
 */
void LayerSubscription::getSubscribedData(std::ostream &o)
{
     double *toEncode = new double[mLength];

     // Get data for the specified layer
     mBuf.layer(mLayer, *mFaction, mLength, mIndex, toEncode);

     o << "<subscribedData xsi:type=\"sp:SubscribedLayerData\" id=\"" << mId << "\">" << endl;
     o << "<layerData>";
     XMLHelper::base64Print(toEncode, mLength, mSessionBigEndian != Server::bigEndian(), o);
     o << "</layerData>" << endl;
     o << "</subscribedData>" << endl;

     delete [] toEncode;
}


/**
 * \brief Creates a subscription from a DOMElement, e.g. an xml
 * representation.
 *
 * \param n The DOMElement from which this subscription should be
 * created.
 * \param buf The Buffer from which data should be fetched.
 */
RegionSubscription::RegionSubscription(DOMElement *n, Buffer &buf)
     : Subscription(n, buf)
{
     if (!mBuf.hasData()) {
	  Error e;
	  e << "Tried to create a RegionSubscription when no scenario is initialized.";
	  throw e;
     }
     else if (Projection::mCurrent == 0) {
	  Error e;
	  e << "Tried to create a RegionSubscription when there is no current projection.";
	  throw e;
     }

     mResetCount = mBuf.resetCount();
//     mRegion = new CellGroup(mBuf.grid().factions());
     mRegion = new CellGroup(mBuf.gridDataHandler());

     Shape* area = XMLHelper::getShape(*n, "region", Reference::nullRef());
     // Notice that the area must be in projection space for cells() to work properly
     area->toProj(*Projection::mCurrent);
     area->cells(mBuf.grid(), mPositions);
     debug("Created RegionSubscription with id: '" << mId << "' and " << mPositions.size() << " cells");
     for (list<GridPos>::iterator it = mPositions.begin(); it != mPositions.end(); it++) {
          mRegion->addMember(mBuf.grid().cell(*it));
     }
     delete area;
}

/**
 * \brief Destructor.
 */
RegionSubscription::~RegionSubscription()
{
     if (mRegion) { delete mRegion; }
}

/**
 * \brief Writes an XML representation of the subscribed data to the
 * provided stream.
 *
 * \param o The stream to write to.
 */
void RegionSubscription::getSubscribedData(std::ostream &o)
{
     // Update our AttributesGroup if the Buffer has been reset since
     // the last call
     if (mResetCount != mBuf.resetCount()) {
	  delete mRegion;
	  mRegion = new CellGroup(mBuf.gridDataHandler());
//	  mRegion = new CellGroup(mBuf.grid().factions());
	  for (list<GridPos>::iterator it = mPositions.begin(); it != mPositions.end(); it++) {
	       mRegion->addMember(mBuf.grid().cell(*it));
	  }
     }

     mRegion->update();

     o << "<subscribedData xsi:type=\"sp:SubscribedRegionData\" id=\"" << mId << "\">" << endl;
     // PV:s with factions.
     for (int i = 0; i < eNumWithFac; ++i) {
	  ePVF pv = static_cast<ePVF>(i);
	  for (int j = 0; j < mBuf.grid().factions() + 1; ++j) {
	       printPV(o,
		       PVHelper::pvfName(pv),
		       PVHelper::pvfType(pv),
		       mRegion->pvfGet(pv, j),
		       (j == EthnicFaction::ALL ? 0 : EthnicFaction::faction(j)));
	  }
     }
     // PV:s without factions.
     for (int i = 0; i < eNumNoFac; ++i) {
	  ePV pv = static_cast<ePV>(i);
	  printPV(o, PVHelper::pvName(pv), PVHelper::pvType(pv), mRegion->pvGet(pv));
     }
     // Derived PV:s with factions.
     for (int i = 0; i < eDNumDerivedF; ++i) {
	  eDerivedF pv = static_cast<eDerivedF>(i);
	  for (int j = 0; j < mBuf.grid().factions() + 1; ++j) {
	       printPV(o, PVHelper::pdfName(pv),
		       PVHelper::pdfType(pv),
		       mRegion->pdfGet(pv, j),
		       (j == EthnicFaction::ALL ? 0 : EthnicFaction::faction(j)));
	  }
     }
     // Derived PV:s without factions.
     for (int i = 0; i < eDNumDerived; ++i) {
	  eDerived pv = static_cast<eDerived>(i);
	  printPV(o, PVHelper::pdName(pv), PVHelper::pdType(pv), mRegion->pdGet(pv));
     }
     
     // Precalculated PV:s with factions.
     for (int i = 0; i < ePNumPreCalcF; ++i) {
	  ePreCalcF pv = static_cast<ePreCalcF>(i);
	  for (int j = 0; j < mBuf.grid().factions() + 1; ++j) {
	       printPV(o, PVHelper::pcfName(pv),
		       PVHelper::pcfType(pv),
		       mRegion->pcfGet(pv, j),
		       (j == EthnicFaction::ALL ? 0 : EthnicFaction::faction(j)));
	  }
     }
     // Precalculated PV:s without factions.
     for (int i = 0; i < ePNumPreCalc; ++i) {
	  ePreCalc pv = static_cast<ePreCalc>(i);
	  printPV(o, PVHelper::pcName(pv), PVHelper::pcType(pv), mRegion->pcGet(pv));
     }
     
     // CombatGrid
     int numCombatLayers = mBuf.combatGrid().layers();
     double* agg = new double[numCombatLayers];
     mBuf.combatGrid().aggregate(mPositions, agg);
     for (int i = 0; i < numCombatLayers; i++) {
	  printPV(o, mBuf.combatGrid().indexToName(i).c_str(), "sp:Positive", agg[i]);
     }
     delete [] agg;

     // Stance Layers
//      const GridDataHandler& gdh = mBuf.gridDataHandler();
//      for (int i = 0; i < gdh.stanceLayers(); ++i) {
// 	  printPV(o, gdh.stanceLayerName(i).c_str(), "sp:Percent", mRegion->psGet(i));
//      }

     o << "</subscribedData>" << endl;
}

void RegionSubscription::printPV(ostream& o, const char* name, const char* type, double value, EthnicFaction* faction)
{
     o << "<pv>" << endl;
     o << "<name>" << name << "</name>" << endl;
     if (faction) {
	  o << "<faction>" << endl;
	  faction->ref().toXML(o) << endl;
	  o << "</faction>" << endl;
     }
     o << "<value xsi:type=\"" << type << "\">";
     if (!isnan(value)) {
	  o << value;
     }
     else {
	  o << "NaN";
     }
     o << "</value>" << endl;
     o << "</pv>" << endl;
}
