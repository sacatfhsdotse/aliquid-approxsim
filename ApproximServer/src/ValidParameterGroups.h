#ifndef APPROXSIM_VALIDPARAMETERGROUPS_H
#define APPROXSIM_VALIDPARAMETERGROUPS_H


// Own
#include "ParameterGroup.h"

static const ParameterGroupEntry defaultParameterGroups[] = {
     {"Food Model"},
     {"Insurgent Model"}
};

const int kNumDefaultParameterGroups = 2;

/**
 * \brief The default parameter group for the simulation.
 *
 * Creation of this object will also create the necessary child
 * parameter groups that are not already created.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 13:13:25 $
 */
class DefaultParameterGroup : public TemplateParameterGroup<NOTYPE, 0> {
private:
     
public:
     DefaultParameterGroup(const DataObject& d)
          : TemplateParameterGroup<NOTYPE, 0>(d, defaultParameterGroups, kNumDefaultParameterGroups, 0) {}
};


enum eFoodModelP {
     eFoodProductionPerKm22,
     eFoodImportFromAbroad2,
     eNumFoodModelP
};

static const ParameterEntry foodModelP[] = {
     {eFoodProductionPerKm22, "Food Production Per Km2", "Double", 53.0 / 365.0},
     {eFoodImportFromAbroad2, "Food Import From Abroad", "Double", 0           }
};

/**
 * \brief The food model parameter group. This refers to a model that
 * isn't implemented yet. It has been left here for future use.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 13:13:25 $
 */
class FoodModelParameters : public TemplateParameterGroup<eFoodModelP, eNumFoodModelP> {
public:
     FoodModelParameters(const DataObject& d) : TemplateParameterGroup<eFoodModelP, eNumFoodModelP>(d, 0, 0, foodModelP) {}
};


enum eInsurgentModelP {
     eFractionPotentialInsurgents2   ,
     eInsurgentDisaffectionThreshold2,
     eInsurgentGenerationCoefficient2,
     eInsurgentStrengthFactor2       ,
     eNumInsurgentModelP
};

static const ParameterEntry insurgentModelP[] = {
     {eFractionPotentialInsurgents2   , "Fraction Potential Insurgents"   , "Double", 0.03},
     {eInsurgentDisaffectionThreshold2, "Insurgent Disaffection Threshold", "Double", 30  },
     {eInsurgentGenerationCoefficient2, "Insurgent Generation Coefficient", "Double", 0.01},
     {eInsurgentStrengthFactor2       , "Insurgent Strength Factor"       , "Double", 0.01}
};

/**
 * \brief The insurgent model parameter group. This refers to the
 * implemented insurgent model but is not used yet since the
 * ModelParameters class is still used. It has been left here for
 * future use.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 13:13:25 $
 */
class InsurgentModelParameters : public TemplateParameterGroup<eInsurgentModelP, eNumInsurgentModelP> {
public:
     InsurgentModelParameters(const DataObject& d)
          : TemplateParameterGroup<eInsurgentModelP, eNumInsurgentModelP>(d, 0, 0, insurgentModelP) {}
};


#endif   // APPROXSIM_VALIDPARAMETERGROUPS_H
