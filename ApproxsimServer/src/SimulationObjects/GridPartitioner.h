#ifndef APPROXSIM_GRIDPARTITIONER_H
#define APPROXSIM_GRIDPARTITIONER_H

// Own
#include "SimulationObject.h"

// Forward Declarations
class DataObject;
class Grid;
class Map;
class Update;

/**
 * \brief An abstract base class for all GridPartitioners
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:07 $
 */
class GridPartitioner : public SimulationObject {
public:
     /**
      * \brief Creates a GridPartitioner from the provided DataObject.
      *
      * \param d The DataObject to create this object from.
      */
     GridPartitioner(const DataObject& d) : SimulationObject(d) {}

     /// Destructor
     virtual ~GridPartitioner() {}

     /**
      * \brief Creates a Grid.
      *
      * \param m The map to lay the Grid over.
      * \param numEthnicFactions The number of ethnic factions.
      * \return The newly created Grid.
      */
     virtual Grid* createGrid(const Map& m, int numEthnicFactions) const = 0;
};


/**
 * \brief A GridPartitioner that creates a grid with square cells.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:07 $
 */
class SquarePartitioner : public GridPartitioner {
private:
     /// Length of the timestep.
     double mCellSideMeters;

public:
     /**
      * \brief Creates a SquarePartitioner from the provided DataObject.
      *
      * \param d The DataObject to create this object from.
      */
     SquarePartitioner(const DataObject& d);

     /// Destructor
     virtual ~SquarePartitioner() {}

     void update(const Update& u);
     void extract(Buffer &b) const {}
     void reset(const DataObject& d);
     Grid* createGrid(const Map& m, int numEthnicFactions) const;

     /**
      * \brief Get the length of the cell side in meters.
      *
      * \return The length of the cell side in meters.
      */
     double cellSideMeters() { return mCellSideMeters; }
};

#endif   // APPROXSIM_GRIDPARTITIONER_H
