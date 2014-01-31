#ifndef STRATMAS_PVINFO_H
#define STRATMAS_PVINFO_H


// System
#include <iosfwd>
#include <string>
#include <vector>


/**
 * \brief This class contains the description of a process variable.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 13:13:23 $
 */
class PVDescription {
private:
     std::string mName;      ///< The name of the PV.
     std::string mType;      ///< The type of the PV.
     std::string mCategory;  ///< The category of the PV.
     bool mFactions;         ///< Flag for factions or not.
     std::string mMin;       ///< The minimum value.
     std::string mMax;       ///< The maximum value.
     bool mVisible;          ///< Flag indicating user visibility.
public:
     /**
      * \brief Creates a PVDescription from the provided values.
      *
      * \param n   The name of the PV.		  
      * \param c   The category of the PV.	  
      * \param f   Flag for factions or not.	  
      * \param min The minimum value.		  
      * \param max The maximum value.		  
      * \param v   Flag indicating user visibility.
      */
     PVDescription(std::string n, std::string type, std::string c, bool f, std::string min, std::string max, bool v = true)
	  : mName(n), mType(type), mCategory(c), mFactions(f), mMin(min), mMax(max), mVisible(v) {}

     /**
      * \brief Copy constructor.
      *
      * \param p The PVDescription to copy.
      */
     PVDescription(const PVDescription& p)
	  : mName(p.mName), mType(p.mType), mCategory(p.mCategory), mFactions(p.mFactions), mMin(p.mMin), mMax(p.mMax), mVisible(p.mVisible) {}

     /**
      * \brief Accessor for the visible flag.
      *
      * \return The value of the visible flag.
      */
     bool visible() const { return mVisible; }

     /**
      * \brief Less-than operator.
      *
      * \param p The PVDescription to compare with.
      * \return True if this PVDescription is less than the provided PVDescription.
      */
     bool operator < (const PVDescription& p) { return mName < p.mName; }
     std::ostream& toXML(std::ostream& o) const;
};




/**
 * \brief Static class that holds information about the process
 * variables that the server is capable of simulating.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 13:13:23 $
 */
class PVInfo {
private:
     /// Process variables known from the start.
     static std::vector<PVDescription> sStaticPV;

     /// Process varaibles that depends on the simulation (e.g. troop density etc.)
     static std::vector<PVDescription> sSimulationDependentPV;

     static void addStaticPV(std::string name, std::string type, std::string cat, bool fac, std::string min, std::string max, bool visible = true);

public:
     static void init();
     static void addPV(std::string n, std::string t, std::string c, bool f, std::string min, std::string max);

     /**
      * \brief Resets this PVInfo object.
      */
     static void reset() { sSimulationDependentPV.clear(); }
     static std::ostream& toXML(std::ostream& o);
};

#endif   // STRATMAS_PVINFO_H
