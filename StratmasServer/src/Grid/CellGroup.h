#ifndef STRATMAS_CELLGROUP_H
#define STRATMAS_CELLGROUP_H


// System
#include <vector>

// Own
#include "ProcessVariables.h"

// Forward Declarations
class GridCell;
class GridDataHandler;

/**
 * \brief Represents a collection of cells.
 *
 * This class is used to keep track of groups of gridcells and to
 * aggregate the cells' pv values - for example when subscribing to a
 * Region.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:05:13 $
 */
class CellGroup {
private:
     /// The GridDatahandler
     const GridDataHandler& mGDH;

     /// The number of factions.
     int mFactions;

     /// Array of arrays of aggregated PV values for PV:s with faction.
     double **mPVF;

     /// Array of aggregated values for PV:s without faction.
     double *mPV;

     /// Array of arrays of aggregated values for derived PV:s with faction.
     double **mPDF;

     /// Array of aggregated values for derived PV:s without faction.
     double *mPD;

     /// Array of arrays of aggregated values for precalculated PV:s with faction.
     double **mPCF;

     /// Array of aggregated values for precalculated PV:s without faction.
     double *mPC;

     /// Array of aggregated values for stance layers.
//     double *mPS;

     /**
      * \brief Vector containing pointers to all Attributes objects
      * that should be aggregated and how to weight them.
      */
     std::vector<std::pair<double, const GridCell*> > mMembers;

     void zero();
     void handleRoundOffErrors();

public:
     CellGroup(const GridDataHandler& gdh);
     ~CellGroup();

     /**
      * \brief Accessor for the number of cells in this group.
      *
      * \return The number of cells in this group.
      */
     int size() const { return mMembers.size(); }

     /**
      * \brief Adds a Attributes object to this group.
      *
      * \param a A pointer to the Attributes object to add.
      * \param w The weight. 
      */
     void addMember(const GridCell* c, double w = 1.0) { 
          mMembers.push_back(std::pair<double, const GridCell*>(w, c));
     }

     const std::vector<std::pair<double, const GridCell*> >& members() const { return mMembers; }

     /**
      * \brief Accessor for values for PV:s with faction.
      *
      * \param pv The index of the PV.
      * \param f The faction index.
      * \return The value of the specified PV and faction.
      */
     double pvfGet(ePVF pv, int f = 0) const { return mPVF[pv][f]; }

     /**
      * \brief Accessor for values for PV:s without faction.
      *
      * \param pv The index of the PV.
      * \return The value of the specified PV.
      */
     double pvGet(ePV pv) const { return mPV[pv]; }

     /**
      * \brief Accessor for values for derived PV:s with faction.
      *
      * \param pv The index of the derived PV.
      * \param f The faction index.
      * \return The value of the specified derived PV and faction.
      */
     double pdfGet(eDerivedF pv, int f = 0) const { return mPDF[pv][f]; }

     /**
      * \brief Accessor for values for derived PV:s without faction.
      *
      * \param pv The index of the derived PV.
      * \return The value of the specified derived PV.
      */
     double pdGet(eDerived pv) const { return mPD[pv]; }

     /**
      * \brief Accessor for values for precalculated PV:s with faction.
      *
      * \param pv The index of the precalculated PV.
      * \param f The faction index.
      * \return The value of the specified precalculated PV and faction.
      */
     double pcfGet(ePreCalcF pv, int f = 0) const { return mPCF[pv][f]; }

     /**
      * \brief Accessor for values for precalculated PV:s without faction.
      *
      * \param pv The index of the precalculated PV.
      * \return The value of the specified precalculated PV.
      */
     double pcGet(ePreCalc pv) const { return mPC[pv]; }

     /**
      * \brief Accessor for values for stance layers.
      *
      * \param pv The index of the stance layer
      * \return The value of the specified stance layer.
      */
//     double psGet(int i) const { return mPS[i]; }

     void update();
     void updateWeights();

     friend std::ostream& operator << (std::ostream& o, const CellGroup& c);
};

#endif   // STRATMAS_CELLGROUP_H
