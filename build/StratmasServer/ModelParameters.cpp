// System

// Own
#include "Buffer.h"
#include "DataObject.h"
#include "ModelParameters.h"
#include "Update.h"


using namespace std;


// Static Definitions
std::map<std::string, eModelParameter> ModelParameters::sNameToIndex;


/**
 * \brief Creates a ModelParameters object from the provided
 * DataObject.
 *
 * \param d The data object to create this TimeStepper from.
 */
ModelParameters::ModelParameters(const DataObject& d)
     : SimulationObject(d), mParam(0)
{
     static bool firstTime = true;
     if (firstTime) {
	  firstTime = false;
	  for (int i = 0; i < eNumModelParameters; ++i) {
	       eModelParameter mp = static_cast<eModelParameter>(i);
	       sNameToIndex[paramName(mp)] = mp;
	  }
     }

     mParam = new double[eNumModelParameters];
     setDefault();
     getDataFromDataObject(d);
}

/**
 * \brief Destructor
 */
ModelParameters::~ModelParameters()
{
     if (mParam) { delete [] mParam; }
}

void ModelParameters::getDataFromDataObject(const DataObject& d) {
     mParam[eFractionPotentialInsurgents   ] = d.getChild(paramName(eFractionPotentialInsurgents   ))->getDouble();
     mParam[eInsurgentDisaffectionThreshold] = d.getChild(paramName(eInsurgentDisaffectionThreshold))->getDouble();
     mParam[eInsurgentGenerationCoefficient] = d.getChild(paramName(eInsurgentGenerationCoefficient))->getDouble();
     mParam[eInsurgentStrengthFactor       ] = d.getChild(paramName(eInsurgentStrengthFactor       ))->getDouble();
}

/**
 * \brief Sets values to default.
 */
void ModelParameters::setDefault()
{
     mParam[eCivilianToMilitaryCasualtyRate  ] = 1.0;
     mParam[eFractionPotentialInsurgents     ] = 0.03;
     mParam[eInsurgentDisaffectionThreshold  ] = 30;
     mParam[eInsurgentGenerationCoefficient  ] = 0.01;
     mParam[eInsurgentStrengthFactor         ] = 0.01;
     mParam[eWorkforceFraction               ] = 0.30;
     mParam[eFractionNewHired                ] = 0.01;
     mParam[eEmployerRiskTakingFactor        ] = 0.5;
     mParam[eInfrastructureDecay             ] = 0.001;
     mParam[eFractionInfrastructureWorkers   ] = 0.5;
     mParam[eFractionRainfallCollected       ] = 0.5;
     mParam[eFractionCollectedPurified       ] = 0.5;
     mParam[eFoodProductionPerKm2            ] = 53.0 / 365.0;
     mParam[eFoodImportFromAbroad            ] = 100;
     mParam[eMeanFamilySize                  ] = 4;
     mParam[eConstructionWorkforceFraction   ] = 0.001;
     mParam[eHousesDestroyedPerCasualty      ] = 0.1;
     mParam[eFractionSusceptible             ] = 0.3;
     mParam[eFractionCivCasualtiesSeekingCare] = 0.7;
}

/**
 * \brief Updates this object.
 *
 * \param u The Update to update this object with.
 */
void ModelParameters::update(const Update& u)
{
     const string& attr = u.getReference().name();
     if (u.getType() == Update::eModify) {
	  std::map<string, eModelParameter>::iterator it = sNameToIndex.find(attr);
	  if (it != sNameToIndex.end()) {
	       mParam[it->second] = u.getObject()->getDouble();
	  }
	  else {
	       Error e;
	       e << "No updatable attribute '" << attr << "' in '" << ref() << "'";
	       throw e;
	  }
     }
     else {
	  Error e;
	  e << "Invalid ModelParameters Update (type:" << u.getTypeAsString();
	  e << ", object: " << attr << ").";
	  throw e;
     }
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void ModelParameters::extract(Buffer &b) const
{
     DataObject& me = *b.map(ref());
     for (int i = 0; i < eNumModelParameters; ++i) {
	  eModelParameter mp = static_cast<eModelParameter>(i);
	  DataObject* c = me.getChild(paramName(mp));
	  if (c) {
	       c->setDouble(mParam[mp]);
	  }
//	  me.getChild(paramName(mp))->setDouble(mParam[mp]);
     }
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void ModelParameters::reset(const DataObject& d)
{
     getDataFromDataObject(d);
}

