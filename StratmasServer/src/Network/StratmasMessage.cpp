// System
#include <iostream>
#include <set>

// Own
#include "Buffer.h"
#include "ChangeTrackerAdapter.h"
#include "Environment.h"
#include "Error.h"
#include "Grid.h"
#include "Map.h"
#include "PVInfo.h"
#include "Shape.h"
#include "Server.h"
#include "Session.h"
#include "StratmasMessage.h"
#include "Subscription.h"
#include "Time.h"
#include "XMLHelper.h"

// Xerces-c
#include <xercesc/util/Base64.hpp>


/**
 * \brief Produces an XML representation of information about the
 * Grid.
 *
 * \param o The stream to write to.
 * \param grid The grid to fetch data from.
 * \param bigEndian Indicates endian of client the message should be
 * sent to. Necessary in order to know whether to byteswap or not.
 * \return o The stream written to.
 */
static std::ostream &gridToXML(std::ostream& o, const BasicGrid& grid, bool bigEndian)
{
     // Number of doubles for describing the cell positions.
     int rows = grid.rows();
     int cols = grid.cols();
     int nCellPos = (rows + 1) * (cols + 1) * 2;

     // Build array marking which cells that are active.
     int8_t *activeCells = new int8_t[grid.cells()];
     for (int r = 0; r < rows; r++) {
          for (int c = 0; c < cols; c++) {
               activeCells[r * cols + c] = (grid.isActive(r, c) ? 1 : 0);
          }
     }

     bool swapEndian = bigEndian != Server::bigEndian();

     o << "<gridData>" << std::endl;
     o << "<numberOfRows>" << rows << "</numberOfRows>" << std::endl;
     o << "<numberOfCols>" << cols << "</numberOfCols>" << std::endl;
     o << "<positionData>";
     XMLHelper::base64Print(grid.cellPosLatLng(), nCellPos, swapEndian, o);
     o << "</positionData>" << std::endl;
     o << "<activeCells>";
     XMLHelper::base64Print(activeCells, grid.cells(), o);
     o << "</activeCells>" << std::endl;

     vector<Shape*> shapes;
     Shape* mapShape = &grid.map().borders();
     if (mapShape->type() == "Composite") {
          dynamic_cast<CompositeShape*>(mapShape)->getFlattened(shapes);
     }
     else {
          shapes.push_back(mapShape);
     }

     for (vector<Shape*>::const_iterator it = shapes.begin(); it != shapes.end(); it++) {
          o << "<regionData>" << endl;
          (*it)->cellsToXML(grid, swapEndian, o);
          o << "</regionData>" << endl;
     }

     o << "</gridData>";

     delete [] activeCells;

     return o;
}



/**
 * \brief Helper for creating the header of the XML representation
 * of a StratmasMessage.
 *
 * \param o   The stream to write to.
 * \param type The type of the message to write a header for.
 */
void StratmasMessage::openMessage(std::ostream &o, const std::string &type) const
{
     o << "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" << std::endl
       << "<sp:stratmasMessage xmlns:sp=\"" << Environment::DEFAULT_SCHEMA_NAMESPACE << "\"" << std::endl
       << "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" << std::endl
       << "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" << std::endl
       << "xsi:type=\"sp:" << type << "\">";
}

/**
 * \brief Helper for creating the end of the XML representation of a
 * StratmasMessage.
 *
 * \param o   The stream to write to.
 */
void StratmasMessage::closeMessage(std::ostream &o) const
{
     o << "</sp:stratmasMessage>";
}



/**
 * \brief Produces the XML representation of this message.
 *
 * \param o The stream to which the message is written
 */
void ConnectResponseMessage::toXML(std::ostream &o) const
{
     openMessage(o, "ConnectResponseMessage");
     o << "<active>" << (mActive ? "true" : "false") << "</active>" << std::endl;
     closeMessage(o);
}



/**
 * \brief Produces the XML representation of this message.
 *
 * \param o The stream to which the message is written
 */
void ServerCapabilitiesResponseMessage::toXML(std::ostream &o) const
{
     openMessage(o, "ServerCapabilitiesResponseMessage");
     PVInfo::toXML(o);
     closeMessage(o);
}



/**
 * \brief Produces the XML representation of this message.
 *
 * \param o The stream to which the message is written
 */
void GetGridResponseMessage::toXML(std::ostream &o) const
{
     openMessage(o, "GetGridResponseMessage");
     Lock bufLock(mBuf.mutex());
     gridToXML(o, mBuf.grid(), mSessionBigEndian) << std::endl;
     bufLock.unlock();
     closeMessage(o);
}



/**
 * \brief Produces the XML representation of this message.
 *
 * \param o The stream to which the message is written
 */
void StatusMessage::toXML(std::ostream &o) const
{
     openMessage(o, "StatusMessage");
     o << "<type>" << mType << "</type>" << std::endl;
     for (std::vector<Error>::const_iterator it = mErrors.begin(); it != mErrors.end(); it++) {
          it->toXML(o);
          o << std::endl;
     }
     closeMessage(o);
}



/**
 * \brief Produces the XML representation of this message.
 *
 * \param o The stream to which the message is written
 */
void UpdateMessage::toXML(std::ostream &o) const
{
     openMessage(o, "UpdateClientMessage");
     Lock bufLock(mBuf.mutex());
     mValidForTime = mBuf.simTime();
     o << endl << "<simulationTime><value>";
     XMLHelper::timeToDateTime(o, mBuf.simTime());
     o << "</value></simulationTime>" << endl;
     if (mRegisteredForUpdates && mChangeTracker.changed()) {
          mChangeTracker.toXML(o);
     }
     for (std::vector<Subscription*>::const_iterator it = mSubscriptions.begin(); it != mSubscriptions.end(); it++) {
          (*it)->getSubscribedData(o);
     }
     bufLock.unlock();
     closeMessage(o);
}



/**
 * \brief Produces the XML representation of this message.
 *
 * \param o The stream to which the message is written
 */
void ProgressQueryResponseMessage::toXML(std::ostream &o) const
{
     openMessage(o, "ProgressQueryResponseMessage");
     o << "<simulationTime>" << std::endl;
     o << "<value>";
     XMLHelper::timeToDateTime(o, mBuf.currentTime());
     o << "</value>" << std::endl;
     o << "</simulationTime>" << std::endl;
     o << "<idle>" << (mBuf.engineIdle() ? "true" : "false") << "</idle>" << std::endl;
     closeMessage(o);
}

/**
 * \brief Produces the XML representation of this message.
 *
 * \param o The stream to which the message is written
 */
void LoadQueryResponseMessage::toXML(std::ostream &o) const
{
     set<string> sims;
     for (std::map<int64_t, Session*>::const_iterator it = mServer.sessions().begin();
          it != mServer.sessions().end(); it++) {
          sims.insert(it->second->simulationName());
     }
     // An empty string means that there is no simulation, e.g. that
     // there is a Session but that the simulation is not initialized
     // yet.
     sims.erase("");

     openMessage(o, "LoadQueryResponseMessage");
     o << "<hasActiveClient>" << (mServer.hasActiveClient() ? "true" : "false") << "</hasActiveClient>" << endl;
     for (set<string>::iterator it = sims.begin(); it != sims.end(); it++) {
          o << "<simulation>" << *it << "</simulation>" << endl;
     }
     closeMessage(o);
}
