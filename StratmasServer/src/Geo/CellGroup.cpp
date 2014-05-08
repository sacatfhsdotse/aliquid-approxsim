// System
#include <cstring>
#include <cmath>
#include <iomanip>
#include <iostream>

// Own
#include "CellGroup.h"
#include "Grid.h"
#include "GridCell.h"
#include "GridDataHandler.h"
#include "ApproxsimConstants.h"


using namespace std;


/**
 * \brief Constructor.
 *
 * \param gdh The GridDataHandler.
 */
CellGroup::CellGroup(const GridDataHandler& gdh) : mGDH(gdh), mFactions(mGDH.grid().factions())
{
     // With factions
     mPVF = new double*[eNumWithFac];
     for (int i = 0; i < eNumWithFac; ++i) {
          mPVF[i] = new double[mFactions + 1];
     }
     // Without factions
     mPV = new double[eNumNoFac];

     // Derived with factions
     mPDF = new double*[eNumWithFac];
     for (int i = 0; i < eDNumDerivedF; ++i) {
          mPDF[i] = new double[mFactions + 1];
     }
     // Derived without factions
     mPD = new double[eDNumDerived];

     // Precalculated with factions
     mPCF = new double*[ePNumPreCalcF];
     for (int i = 0; i < ePNumPreCalcF; ++i) {
          mPCF[i] = new double[mFactions + 1];
     }
     // Precalculated without factions
     mPC = new double[ePNumPreCalc];

     // Precalculated with factions
//     mPS = new double[mGDH.stanceLayers()];

     zero();
}

/**
 * \brief Destructor
 */
CellGroup::~CellGroup()
{
     for (int i = 0; i < eNumWithFac; ++i) {
          delete [] mPVF[i];
     }
     delete [] mPVF;
     delete [] mPV;

     for (int i = 0; i < eDNumDerivedF; ++i) {
          delete [] mPDF[i];
     }
     delete [] mPDF;
     delete [] mPD;

     for (int i = 0; i < ePNumPreCalcF; ++i) {
          delete [] mPCF[i];
     }
     delete [] mPCF;
     delete [] mPC;

//     delete [] mPS;
}

/**
 * \brief Resets all aggregates to zero.
 */
void CellGroup::zero()
{
     for (int i = 0; i < eNumWithFac; ++i) {
          memset(mPVF[i], 0, (mFactions + 1) * sizeof(double));
     }
     memset(mPV, 0, eNumNoFac * sizeof(double));

     for (int i = 0; i < eDNumDerivedF; ++i) {
          memset(mPDF[i], 0, (mFactions + 1) * sizeof(double));
     }
     memset(mPD, 0, eDNumDerived * sizeof(double));

     for (int i = 0; i < ePNumPreCalcF; ++i) {
          memset(mPCF[i], 0, (mFactions + 1) * sizeof(double));
     }
     memset(mPC, 0, ePNumPreCalc * sizeof(double));

//     memset(mPS, 0, mGDH.stanceLayers() * sizeof(double));
}

/**
 * \brief Handles round off errors.
 */
void CellGroup::handleRoundOffErrors()
{
     GridCell::handleRoundOffErrorsPositive(mPVF[ePopulation     ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPVF[eDisplaced      ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPVF[eSheltered      ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPVF[eProtected      ], mFactions + 1);
     GridCell::handleRoundOffErrorsPercent (mPVF[eViolence       ], mFactions + 1);
     GridCell::handleRoundOffErrorsPercent (mPVF[ePerceivedThreat], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPVF[eInsurgents     ], mFactions + 1);

     GridCell::handleRoundOffErrorsFraction(&mPV[eFractionNoMedical   ]);
     GridCell::handleRoundOffErrorsFraction(&mPV[eFractionNoWork      ]);
     GridCell::handleRoundOffErrorsFraction(&mPV[eEthnicTension       ]);
     GridCell::handleRoundOffErrorsFraction(&mPV[eFractionCrimeVictims]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eHousingUnits        ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eStoredFood          ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eFoodConsumption     ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eFarmStoredFood      ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eMarketedFood        ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eFoodDays            ]);
     GridCell::handleRoundOffErrorsFraction(&mPV[eFractionNoFood      ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eWaterConsumption    ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eWaterDays           ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eSuppliedWater       ]);
     GridCell::handleRoundOffErrorsFraction(&mPV[eFractionNoWater     ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eSusceptible         ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eInfected            ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eRecovered           ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eDeadDueToDisease    ]);
     GridCell::handleRoundOffErrorsFraction(&mPV[eFractionInfected    ]);
     GridCell::handleRoundOffErrorsFraction(&mPV[eFractionRecovered   ]);
     GridCell::handleRoundOffErrorsFraction(&mPV[eInfrastructure      ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eDailyDead           ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eTotalDead           ]);
     GridCell::handleRoundOffErrorsPositive(&mPV[eSmoothedDead        ]);

     GridCell::handleRoundOffErrorsPercent(mPDF[eDDisaffection], mFactions + 1);

     GridCell::handleRoundOffErrorsFraction(&mPD[eDPolarization    ]);
     GridCell::handleRoundOffErrorsPositive(&mPD[eDHoused          ]);
     GridCell::handleRoundOffErrorsPositive(&mPD[eDAvailableFood   ]);
     GridCell::handleRoundOffErrorsFraction(&mPD[eDFoodDeprivation ]);
     GridCell::handleRoundOffErrorsFraction(&mPD[eDWaterDeprivation]);

     GridCell::handleRoundOffErrorsPositive(mPCF[ePAtHome                 ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePSheltered              ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePUnsheltered            ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePHomeIll                ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePShelteredIll           ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePUnshelteredIll         ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePHomeImmune             ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePShelteredImmune        ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePUnshelteredImmune      ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePHomeDead               ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePShelteredDead          ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePUnshelteredDead        ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePIDPDeadDueToViolence   ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePHousedDeadDueToViolence], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePNewIDPDueToViolence    ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePTowardsCampDelta       ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePDiffusionDelta         ], mFactions + 1);
     GridCell::handleRoundOffErrorsPositive(mPCF[ePDiffusionDisplacedDelta], mFactions + 1);

     GridCell::handleRoundOffErrorsPositive(&mPC[ePFoodProduction ]);
     GridCell::handleRoundOffErrorsPositive(&mPC[ePWaterProduction]);

//     GridCell::handleRoundOffErrorsPercent(mPS, mGDH.stanceLayers());
}

/**
 * \brief Aggregates data from all members.
 */
void CellGroup::update()
{
     if (mMembers.size() == 1) {
          const GridCell& c = *mMembers.begin()->second;
          // Ordinary
          for (int i = 0; i < eNumWithFac; ++i) {
               for (int j = 0; j < mFactions + 1; ++j) {
                    mPVF[i][j] = c.pvfGet(static_cast<ePVF>(i), j);
               }
          }
          for (int i = 0; i < eNumNoFac; ++i) {
               mPV[i] =  c.pvGet(static_cast<ePV>(i));
          }

          // Derived
          for (int i = 0; i < eDNumDerivedF; ++i) {
               for (int j = 0; j < mFactions + 1; ++j) {
                    mPDF[i][j] = c.pdfGet(static_cast<eDerivedF>(i), j);
               }
          }
          for (int i = 0; i < eDNumDerived; ++i) {
               mPD[i] =  c.pdGet(static_cast<eDerived>(i));
          }

          // Precalculated
          for (int i = 0; i < ePNumPreCalcF; ++i) {
               for (int j = 0; j < mFactions + 1; ++j) {
                    mPCF[i][j] = c.pcfGet(static_cast<ePreCalcF>(i), j);
               }
          }
          for (int i = 0; i < ePNumPreCalc; ++i) {
               mPC[i] =  c.pcGet(static_cast<ePreCalc>(i));
          }
     }
     else {
          zero();
          std::vector<std::pair<double, const GridCell*> >::iterator it;

          // Aggregate the weighted counts across all members
          for(it = mMembers.begin(); it != mMembers.end(); it++) {
               double weight = it->first;
               const GridCell& c = *it->second;
               for (int i = 0; i < mFactions + 1; i++) {
                    mPVF[ePopulation][i] += weight * c.pvfGet(ePopulation, i);
                    mPVF[eDisplaced ][i] += weight * c.pvfGet(eDisplaced,  i);
                    mPVF[eSheltered ][i] += weight * c.pvfGet(eSheltered,  i);
                    mPVF[eProtected ][i] += weight * c.pvfGet(eProtected,  i);
                    mPVF[eInsurgents][i] += weight * c.pvfGet(eInsurgents, i);

                    // Precalculated
                    mPCF[ePAtHome                 ][i] += weight * c.pcfGet(ePAtHome                 , i);
                    mPCF[ePSheltered              ][i] += weight * c.pcfGet(ePSheltered              , i);
                    mPCF[ePUnsheltered            ][i] += weight * c.pcfGet(ePUnsheltered            , i);
                    mPCF[ePHomeIll                ][i] += weight * c.pcfGet(ePHomeIll                , i);
                    mPCF[ePShelteredIll           ][i] += weight * c.pcfGet(ePShelteredIll           , i);
                    mPCF[ePUnshelteredIll         ][i] += weight * c.pcfGet(ePUnshelteredIll         , i);
                    mPCF[ePHomeImmune             ][i] += weight * c.pcfGet(ePHomeImmune             , i);
                    mPCF[ePShelteredImmune        ][i] += weight * c.pcfGet(ePShelteredImmune        , i);
                    mPCF[ePUnshelteredImmune      ][i] += weight * c.pcfGet(ePUnshelteredImmune      , i);
                    mPCF[ePHomeDead               ][i] += weight * c.pcfGet(ePHomeDead               , i);
                    mPCF[ePShelteredDead          ][i] += weight * c.pcfGet(ePShelteredDead          , i);
                    mPCF[ePUnshelteredDead        ][i] += weight * c.pcfGet(ePUnshelteredDead        , i);
                    mPCF[ePIDPDeadDueToViolence   ][i] += weight * c.pcfGet(ePIDPDeadDueToViolence   , i);
                    mPCF[ePHousedDeadDueToViolence][i] += weight * c.pcfGet(ePHousedDeadDueToViolence, i);
                    mPCF[ePNewIDPDueToViolence    ][i] += weight * c.pcfGet(ePNewIDPDueToViolence    , i);
                    mPCF[ePTowardsCampDelta       ][i] += weight * c.pcfGet(ePTowardsCampDelta       , i);
                    mPCF[ePDiffusionDelta         ][i] += weight * c.pcfGet(ePDiffusionDelta         , i);
                    mPCF[ePDiffusionDisplacedDelta][i] += weight * c.pcfGet(ePDiffusionDisplacedDelta, i);
               }
               mPV[eHousingUnits    ] += weight * c.pvGet(eHousingUnits    );
               mPV[eSusceptible     ] += weight * c.pvGet(eSusceptible     );
               mPV[eInfected        ] += weight * c.pvGet(eInfected        );
               mPV[eRecovered       ] += weight * c.pvGet(eRecovered       );
               mPV[eDeadDueToDisease] += weight * c.pvGet(eDeadDueToDisease);
               mPV[eDailyDead       ] += weight * c.pvGet(eDailyDead       );
               mPV[eTotalDead       ] += weight * c.pvGet(eTotalDead       );
               mPV[eSmoothedDead    ] += weight * c.pvGet(eSmoothedDead    );

               // Derived
               mPD[eDHoused] += weight * c.pdGet(eDHoused);

               // Precalculated
               mPC[ePFoodProduction ] += weight * c.pcGet(ePFoodProduction );
               mPC[ePWaterProduction] += weight * c.pcGet(ePWaterProduction);
          }

          // Second, aggregate the quantitative variables using population-weighted averaging.
          double popWeight;
          for(it = mMembers.begin(); it != mMembers.end(); it++) {
               const GridCell& c = *it->second;
               for (int i = 0; i < mFactions + 1; i++) {
                    double popInThisAttrGrp = pvfGet(ePopulation, i);
                    if (popInThisAttrGrp > 0) {
                         popWeight = it->first * c.pvfGet(ePopulation, i) / popInThisAttrGrp;
                         mPVF[eViolence       ][i] += popWeight * c.pvfGet(eViolence,        i);
                         mPVF[ePerceivedThreat][i] += popWeight * c.pvfGet(ePerceivedThreat, i);

                         mPDF[eDDisaffection  ][i] += popWeight * c.pdfGet(eDDisaffection,   i);
                    }
               }

               if (pvfGet(ePopulation) > 0) {
                    // Could be saved from above if loop is reversed but isn't since this is clearer...
                    popWeight = it->first * c.pvfGet(ePopulation) / pvfGet(ePopulation);
          
                    // Population-weighted averages
                    mPV[eStoredFood          ] += popWeight * c.pvGet(eStoredFood          );
                    mPV[eFoodConsumption     ] += popWeight * c.pvGet(eFoodConsumption     );
                    mPV[eFarmStoredFood      ] += popWeight * c.pvGet(eFarmStoredFood      );
                    mPV[eMarketedFood        ] += popWeight * c.pvGet(eMarketedFood        );
                    mPV[eFoodDays            ] += popWeight * c.pvGet(eFoodDays            );
                    mPV[eFractionNoFood      ] += popWeight * c.pvGet(eFractionNoFood      );
                    mPV[eWaterConsumption    ] += popWeight * c.pvGet(eWaterConsumption    );
                    mPV[eWaterDays           ] += popWeight * c.pvGet(eWaterDays           );
                    mPV[eSuppliedWater       ] += popWeight * c.pvGet(eSuppliedWater       );
                    mPV[eFractionNoWater     ] += popWeight * c.pvGet(eFractionNoWater     );
                    mPV[eFractionInfected    ] += popWeight * c.pvGet(eFractionInfected    );
                    mPV[eFractionRecovered   ] += popWeight * c.pvGet(eFractionRecovered   );
                    mPV[eFractionCrimeVictims] += popWeight * c.pvGet(eFractionCrimeVictims);
                    mPV[eFractionNoMedical   ] += popWeight * c.pvGet(eFractionNoMedical   );
                    mPV[eFractionNoWork      ] += popWeight * c.pvGet(eFractionNoWork      );
                    mPV[eEthnicTension       ] += popWeight * c.pvGet(eEthnicTension       );
                    mPV[eInfrastructure      ] += popWeight * c.pvGet(eInfrastructure      );

                    mPD[eDPolarization       ] += popWeight * c.pdGet(eDPolarization) * c.pdGet(eDPolarization);
                    mPD[eDAvailableFood      ] += popWeight * c.pdGet(eDAvailableFood      );
                    mPD[eDFoodDeprivation    ] += popWeight * c.pdGet(eDFoodDeprivation    );
                    mPD[eDWaterSurplusDeficit] += popWeight * c.pdGet(eDWaterSurplusDeficit);
                    mPD[eDWaterDeprivation   ] += popWeight * c.pdGet(eDWaterDeprivation   );

//                     // Stance Layers
//                     for (int i = 0; i < mGDH.stanceLayers(); ++i) {
//                          mPS[i] += popWeight * mGDH.ps(c.index(), i);
//                     }
               }
          }
          // Weighted root-mean-square of polarization
          mPD[eDPolarization] = sqrt(pdGet(eDPolarization));
     }
     handleRoundOffErrors();
}

/**
 * \brief Sets the weight of the member cells so that a cell
 * overlapped by n regions gets the weight 1/n.
 */
void CellGroup::updateWeights()
{
     vector<pair<double, const GridCell*> >::iterator it;
     for(it = mMembers.begin(); it != mMembers.end(); it++) {
          it->first = 1.0 / static_cast<double>(it->second->numOverlappingRegions());
     }
}

/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param c The CellGroup to print.
 * \return The stream with the CellGroup written to it.
 */
ostream& operator << (ostream& o, const CellGroup& c)
{
//      vector<pair<double, const GridCell*> >::const_iterator it;
//      for(it = c.mMembers.begin(); it != c.mMembers.end(); ++it) {
//           o << it->second->row() << ", "  << it->second->col() << ": " << it->first << endl;
//      }
     for (int i = 0; i < eNumWithFac; ++i) {
          ePVF pv = static_cast<ePVF>(i);
          o << PVHelper::pvfName(pv) << "(type = " << PVHelper::pvfType(pv) << "):" << endl;
          for (int f = 0; f < c.mFactions + 1; ++f) {
               o << "   " << c.pvfGet(pv, f) << endl;
          }
     }
     for (int i = 0; i < eNumNoFac; ++i) {
          ePV pv = static_cast<ePV>(i);
          o << left << setw(22) << PVHelper::pvName(pv);
          o << right << " = " << c.pvGet(pv) << endl;
     }
     for (int i = 0; i < eDNumDerivedF; ++i) {
          eDerivedF pv = static_cast<eDerivedF>(i);
          o << PVHelper::pdfName(pv) << "(type = " << PVHelper::pdfType(pv) << "):" << endl;
          for (int f = 0; f < c.mFactions + 1; ++f) {
               o << "   " << c.pdfGet(pv, f) << endl;
          }
     }
     for (int i = 0; i < eDNumDerived; ++i) {
          eDerived pv = static_cast<eDerived>(i);
          o << left << setw(22) << PVHelper::pdName(pv);
          o << right << " = " << c.pdGet(pv) << endl;
     }
     return o;
}

