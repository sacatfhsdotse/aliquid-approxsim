// Own
#include "CellGroup.h"
#include "CombatGrid.h"
#include "Disease.h"
#include "EpidemicsWeights.h"
#include "Faction.h"
#include "GoodStuff.h"
#include "Grid.h"
#include "GridCell.h"
#include "ModelParameters.h"
#include "random.h"
#include "StratmasConstants.h"

// Temporary
#include "Reference.h"


// Static Definitions
// Only for the FractionInfected PV
int     EpidemicsWeights::mRows              = 0;
int     EpidemicsWeights::mCols              = 0;
int     EpidemicsWeights::mHalfNumberOfCells = 0;
double  EpidemicsWeights::mCellSideMeters    = 0;
double* EpidemicsWeights::mEpidemicsWeight   = 0;


double GridCell::bestFoodStorage() const
{
     double logDensityPerKm2 = log10( 1.0 + popDensity());  // km to m
     return 3.0 + (kMaxStoredFood - logDensityPerKm2) * mGrid.HDI();
}

double GridCell::bestWaterCapacity() const
{
     double ret = kMaxStoredWater * 100.0 / popDensity() * mGrid.HDI();
     ret = between(ret, 2.0, kMaxStoredWater);
     return ret;
}

double GridCell::expectedTension() const
{
     double maxViolence = pvfGet(eViolence, 1);
     for (int i = 2; i < sFactions + 1; ++i) {
          maxViolence = max(maxViolence, pvfGet(eViolence, i));
     }

     return min((0.01 * (maxViolence + pdGet(eDPolarization)) + pvGet(eFractionNoWork)) / 3.0, 1.0);
}

double GridCell::pvrGet(eRegionPV pvr)
{
     double ret = 0;
     for (vector<const Region*>::iterator it = mRegions.begin(); it != mRegions.end(); ++it) {
          ret += (*it)->pvrGet(pvr);
     }
     return ret / static_cast<double>(mRegions.size());
}

double GridCell::regionParam(eRegionParameter param)
{
     double ret = 0;
     for (vector<const Region*>::iterator it = mRegions.begin(); it != mRegions.end(); ++it) {
          ret += (*it)->rp(param);
     }
     return ret / static_cast<double>(mRegions.size());
}

double GridCell::regionPVFGet(ePVF pvf, int f)
{
     double ret = 0;
     for (vector<const Region*>::iterator it = mRegions.begin(); it != mRegions.end(); ++it) {
          ret += (*it)->cellGroup().pvfGet(pvf, f);
     }
     return ret / static_cast<double>(mRegions.size());
}

void GridCell::doPopulation(double* data)
{
     double bodyCount = 0;       // Number of dead people
     double* dPop = new double[sFactions + 1];   // Sum the delta from different 'models'

     // Reset delta population variables
     memset(dPop, 0, (sFactions + 1) * sizeof(double));

     // Dead from disease
     for (int i = 1; i < sFactions + 1; ++i) {
          double dead = pcfGet(ePHomeDead, i) + pcfGet(ePShelteredDead, i) + pcfGet(ePUnshelteredDead, i);
          dPop[i]   -= dead;
          bodyCount += dead;
     }
     
     // Dead from violence
     for (int i = 1; i < sFactions + 1; ++i) {
          double killed = pcfGet(ePHousedDeadDueToViolence, i) + pcfGet(ePIDPDeadDueToViolence, i);
          dPop[i]   -= killed;
          bodyCount += killed;
     }
                
     // Dead from military engagement
     /*
       Civilian casualties due to a military engagement
       are proportional to the population density and the
       number of shots fired. Unsheltered IDPs are killed
       in preference to sheltered IDPs.
     */
     double nKilled;
     double aveKilled = 0.001 * sCellAreaKm2 / pvfGet(ePopulation) * dailyShots();
                        
     if (aveKilled > kMinPopulation) {
          nKilled = Poisson( aveKilled );
          if (nKilled > 0.0) {
               for (int i = 1; i < sFactions + 1; ++i) {
                    if (pvfGet(ePopulation, i) > 0.0) {
                         double dead = pvfGet(ePopulation, i) / pvfGet(ePopulation) * nKilled;
                         dPop[i]   -= dead;
                         bodyCount += dead;
                    }
               }
               
          }
     }
        
     // Delta from IDP:s moving towards camps
     for (int i = 1; i < sFactions + 1; ++i) {
          dPop[i] += pcfGet(ePTowardsCampDelta, i);
     }

     // Delta from diffusion e.g. threat driven movements
     for (int i = 1; i < sFactions + 1; ++i) {
          dPop[i] += pcfGet(ePDiffusionDelta, i);
     }

     // Delta from resettling
     for (int i = 1; i < sFactions + 1; ++i) {
          if (mGrid.totalInitialPopulation(i) > 0) {
               dPop[i] += mGrid.resettlers(i) * mGrid.initialPopulation(mIndex, i) / mGrid.totalInitialPopulation(i);
          }
          dPop[i] -= pvfGet(eProtected, i) * kFractionProtectedResettling;
     }     

     // Update bodycount...
     pvSet(eDailyDead, bodyCount);
     pvSet(eTotalDead, pvGet(eTotalDead) + bodyCount);
     const double kSmoothAlpha = 0.75;
     const double kSmoothBeta  = 1 - kSmoothAlpha;
     pvSet(eSmoothedDead, kSmoothAlpha * pvGet(eSmoothedDead) + kSmoothBeta * bodyCount);

     for (int i = 1; i < sFactions + 1; ++i) {
          dPop[0] += dPop[i];
     }

     // Do the update...
     for (int i = 0; i < sFactions + 1; ++i) {
          data[i] = pvfGet(ePopulation, i) + dPop[i];
     }

     delete [] dPop;
     handleRoundOffErrorsPositive(data, sFactions + 1);
}

void GridCell::doDisplaced(double* data)
{
     double* dPop = new double[sFactions + 1];
     // Reset delta population variables
     memset(dPop, 0, (sFactions + 1) * sizeof(double));

     // Dead from disease
     for (int i = 1; i < sFactions + 1; ++i) {
          dPop[i] -= ( pcfGet(ePShelteredDead, i) + pcfGet(ePUnshelteredDead, i) );
     }
     
     // Delta from ethnic violence
     for (int i = 1; i < sFactions + 1; ++i) {
          double delta = pcfGet(ePNewIDPDueToViolence, i) - pcfGet(ePIDPDeadDueToViolence, i);
          dPop[i] += delta;
     }
                
     // Delta from military engagement
     double aveKilled = 0.001 * sCellAreaKm2 / pvfGet(ePopulation) * dailyShots();
     double nKilled   = 0.0;
                        
     if (aveKilled > kMinPopulation) {
          nKilled = Poisson( aveKilled );
          if (nKilled > 0.0) {
               for (int i = 1; i < sFactions + 1; ++i) {
                    if (pvfGet(ePopulation, i) > 0.0) {
                         double prop = pvfGet(ePopulation, i) / pvfGet(ePopulation);
                         dPop[i] -= (prop * nKilled * pvfGet(eDisplaced, i) / pvfGet(ePopulation, i));
                         debug((prop * nKilled * pvfGet(eDisplaced, i) / pvfGet(ePopulation, i))
                              << " displaced people of faction " << i << " dies from military engagement");
                    }
               }
          }
     }

     // Delta from IDP:s moving towards camps
     for (int i = 1; i < sFactions + 1; ++i) {
          dPop[i] += pcfGet(ePTowardsCampDelta, i);
     }

     // Delta from diffusion e.g. threat driven movements
     for (int i = 1; i < sFactions + 1; ++i) {
          dPop[i] += pcfGet(ePDiffusionDelta, i);
     }

     data[0] = 0;
     // Make the update...
     for (int i = 1; i < sFactions + 1; ++i) {
          data[i] = pvfGet(eDisplaced, i) + dPop[i];
          data[0] += data[i];
     }

     delete [] dPop;
     handleRoundOffErrorsPositive(data, sFactions + 1);
}

void GridCell::doSheltered(double* data)
{
     double* dPop = new double[sFactions + 1];
     memset(dPop, 0, (sFactions + 1) * sizeof(double));

     // Dead from disease
     for (int i = 1; i < sFactions + 1; ++i) {
          dPop[i] -= pcfGet(ePShelteredDead, i);
          dPop[0] -= pcfGet(ePShelteredDead, i);
     }
     
     // Make the update...
     for (int i = 0; i < sFactions + 1; ++i) {
          data[i] = pvfGet(eSheltered, i) + dPop[i];
     }

     delete [] dPop;
     handleRoundOffErrorsPositive(data, sFactions + 1);
}

void GridCell::doProtected(double* data)
{
     data[0] = 0;
     for (int i = 1; i < sFactions + 1; ++i) {
          data[i] = pvfGet(eProtected, i) * (1 - kFractionProtectedResettling);
          data[0] += data[i];
     }
}

void GridCell::doViolence(double* data)
{
     double meanViolence = 0.0;
     for (int i = 1; i < sFactions + 1; ++i) {
          data[i] = pvfGet(eViolence, i) * 0.75 + Poisson(0.20 * pdfGet(eDDisaffection, i));
          meanViolence += data[i] * pvfGet(ePopulation, i);
     }

     // Mean violence
     data[0] = meanViolence / pvfGet(ePopulation);

     handleRoundOffErrorsPercent(data, sFactions + 1);
}

void GridCell::doPerceivedThreat(double* data)
{
     // ------------------------
     //          PERCEIVED THREAT
     // ------------------------

     /*
       Perceived threat to civilians is calculated
       as a weighted average of three indicators:
       local recent mortality, local recent fighting,
       and danger due to minority status & ethnic violence.
     */
        
     double meanThreat = 0.0;

     //        Mortality = smoothed death rate per 1000:
     double mortality = 1000.0 * pvGet(eSmoothedDead) / pvfGet(ePopulation);
        
     //        Fighting = logarithm of shots fired:
     double fighting = 10.0 * log10( 1.0 + smoothedShots() );
        
     //        Danger = (ethnic violence) x (minority status)
     for (int i = 1; i < sFactions + 1; ++i) {
          double threat = pvfGet(ePerceivedThreat, i);
          double danger = 0.1 * pvfGet(eViolence, i) * sqrt(1.0 - 0.01 * pvfGet(ePopulation, i) / pvfGet(ePopulation) );
             
          double delta_threat = 0.5 * danger + fighting + mortality - 0.10 * threat;
             
          // Population density reduces the perceived threat:
          delta_threat -= log10( 1.0 + popDensity());  // km to m
             
          // Do the update
          data[i] = threat + delta_threat;

          meanThreat += data[i] * pvfGet(ePopulation, i);
     }

     // Mean threat
     data[0] = meanThreat / pvfGet(ePopulation);

     handleRoundOffErrorsPercent(data, sFactions + 1);
}

void GridCell::doInsurgents(double* data)
{
     data[0] = 0;
     for (int i = 1; i < sFactions + 1; ++i) {
          double maxInsurg = mGrid.mp().mp(eFractionPotentialInsurgents) * pvfGet(ePopulation, i);
          double potNewInsurg = maxInsurg - pvfGet(eInsurgents, i);
          double delta = RandomUniform(0, 0.01) * mGrid.mp().mp(eInsurgentGenerationCoefficient) *
               (pdfGet(eDDisaffection, i) - mGrid.mp().mp(eInsurgentDisaffectionThreshold)) * 
               potNewInsurg;
          data[i] = max(0.0, pvfGet(eInsurgents, i) + delta);
          data[0] += data[i];
     }
     handleRoundOffErrorsPositive(data, sFactions + 1);
}

void GridCell::doEthnicTension(double* data)
{
     if (sFactions == 1) {   // Ethnic tension between groups is not  
          *data = 0;
          return;                  // interesting when there's only one group                       
     }              

     double expectedTension = 0.0;

     //        Calculate the maximum level of violence in this cell:
     double maxViolence = pvfGet(eViolence, 1);
     for (int i = 2; i < sFactions + 1; ++i) {
          maxViolence = max(maxViolence, pvfGet(eViolence, i));
     }

     expectedTension = (0.01 * (maxViolence + pdGet(eDPolarization)) + pvGet(eFractionNoWork)) / 3.0;
     expectedTension = min(expectedTension, 1.0);
     
     *data = pvGet(eEthnicTension) + 0.05 * ( expectedTension - pvGet(eEthnicTension) );

     handleRoundOffErrorsFraction(data);
}

void GridCell::doFractionCrimeVictims(double* data)
{
     double expectedCrime = (0.01 * ( pdfGet(eDDisaffection) + pvfGet(eViolence) ) + pvGet(eFractionNoWork)) / 3.0;
     
     //        Higher densities of population lead to higher crime rates:
     if (popDensity() > 100.0) {
          expectedCrime *= (popDensity() / 500.0);
          if ( expectedCrime > 1.0 ) {
               expectedCrime = 1.0;
          }
     }
     
     *data = pvGet(eFractionCrimeVictims) + 0.20 * (expectedCrime - pvGet(eFractionCrimeVictims));

     handleRoundOffErrorsFraction(data);
}

void GridCell::doHousingUnits(double* data)
{
     double req = pvfGet(ePopulation) / mGrid.mp().mp(eMeanFamilySize);
     double destroyed = mGrid.cg()->value(index(), mGrid.cg()->casualtySumLayer()) * mGrid.mp().mp(eHousesDestroyedPerCasualty);
     double need = 1.0 - pvGet(eHousingUnits) / req;
     double constructionFactor = mGrid.mp().mp(eConstructionWorkforceFraction) * req * mGrid.HDI();
     double deltaHU = constructionFactor * need * (1.0 - pvfGet(ePerceivedThreat) / 100.0) - destroyed;
     *data = pvGet(eHousingUnits) + deltaHU;
     handleRoundOffErrorsPositive(data);
}

void GridCell::doStoredFood(double* data)
{
     *data = pdGet(eDAvailableFood) - pvGet(eFoodConsumption) / 1000.0 * pvfGet(ePopulation);
     handleRoundOffErrorsPositive(data);
}

void GridCell::doFoodConsumption(double* data)
{
     //                                   0  1   2     3     4     5    6    7     8     9    10
     static const double consumption[] = {0, 0, 0.25, 0.25, 0.25, 0.5, 0.5, 0.5, 0.75, 0.75, 1.0};
     int index = between(static_cast<int>(pdGet(eDAvailableFood) * 1000.0 / pvfGet(ePopulation)), 0, 10);
     *data = consumption[index] * kFoodPPPDKg;
     handleRoundOffErrorsPositive(data);
}

void GridCell::doFarmStoredFood(double* data)
{
     *data = pcGet(ePFoodProduction) * pvfGet(ePerceivedThreat) / 100.0;
     handleRoundOffErrorsPositive(data);
}

void GridCell::doMarketedFood(double* data)
{
     *data = pcGet(ePFoodProduction) * (1 - pvfGet(ePerceivedThreat) / 100.0) + pvGet(eFarmStoredFood);
     handleRoundOffErrorsPositive(data);
}

void GridCell::doFoodDays(double* data)
{
     double old = pvGet(eFoodDays);
     if (old >= 2.0) {
          old -= 1.0;
     }
     else {   // Less than 2 days of food remain, so people eat only half:
          old *= 0.5;
     }
     *data = old + pvGet(eInfrastructure) * (bestFoodStorage() - old);

     handleRoundOffErrorsPositive(data);
}

void GridCell::doWaterConsumption(double* data)
{
     //                                   0  1  2  3  4  5  6  7  8  9  10  11  12  13  14  15  16  17  18  19  20
     static const double consumption[] = {0, 0, 0, 0, 0, 5, 5, 5, 5, 5, 10, 10, 10, 10, 10, 15, 15, 15, 15, 15, 20};
     int index = between(static_cast<int>(pcGet(ePWaterProduction) / pvfGet(ePopulation)), 0, 20);
     *data = consumption[index];
}

void GridCell::doWaterDays(double* data)
{
     double shortfall = bestWaterCapacity() - pvGet(eWaterDays);
     
     if ( shortfall > 0.0 ) {
          *data = pvGet(eWaterDays) + (0.10 * mGrid.HDI() * mGrid.HDI() * shortfall);
     }
     else {
          *data = pvGet(eWaterDays);
     }
     handleRoundOffErrorsPositive(data);
}

void GridCell::doSusceptible(double* data)
{
     *data = pvGet(eSusceptible) - mGrid.disease().infectionRate() * pvGet(eSusceptible) / pvfGet(ePopulation) * pvGet(eInfected);
     handleRoundOffErrorsPositive(data);
}

void GridCell::doInfected(double* data)
{
     *data = pvGet(eInfected) * (1 + mGrid.disease().infectionRate() * pvGet(eSusceptible) / pvfGet(ePopulation)
                                 - mGrid.disease().recoveryRate() - mGrid.disease().mortalityRate());
     handleRoundOffErrorsPositive(data);
}

void GridCell::doRecovered(double* data)
{
     *data = pvGet(eRecovered) + mGrid.disease().recoveryRate() * pvGet(eInfected);
     handleRoundOffErrorsPositive(data);
}

void GridCell::doDeadDueToDisease(double* data)
{
     *data = pvGet(eDeadDueToDisease) + mGrid.disease().mortalityRate() * pvGet(eInfected);
     handleRoundOffErrorsPositive(data);
}

void GridCell::doFractionInfected(double* data)
{
     int numNearCols;
     int top, left, bottom, right, row, col;
     double nearby_infectious = 0.0;
     double recoveryRate      = mGrid.disease().recoveryRate();
     double infectionRate;
     GridCell* near;

     EpidemicsWeights::limits(this->row(), this->col(), top, left, bottom, right);
     numNearCols = right - left + 1;
     
     for (row = top; row <= bottom; row++ ) {
          for ( col = left; col <= right; col++ ) {
               near = mGrid.cell(row, col);
               if (near && near != this) {
                    //        Probability of contact from nearby cells is exponentially distributed
                    nearby_infectious += EpidemicsWeights::weight(col - left + (row - top) * numNearCols) *
                         near->pvGet(eFractionInfected) * near->pvfGet(ePopulation);
               }
          }
     }


     // The MixingRate was previously defined in DSociety but since it is only
     // used in this model it has been placed here for now.

     /* Loren's comment
       The mixing rate between neighboring cells is crucial
       for the epidemic model. We scale this rate down as the
       size of the cells increase, so that the speed of the
       epidemic will not be dependent on the cell size.
       
       Maybe this also needs to vary according to the HDI?
     */
     const double kRateOfContact = 0.6;  // Effect of infection in neighboring cells
     double       mixingRate = kRateOfContact * (10.0 / (mGrid.cellSideMeters() / 1000));   // km to m

     double p_infectious = (pvGet(eFractionInfected) * pvfGet(ePopulation) +
                            mixingRate * nearby_infectious) / pvfGet(ePopulation);

     infectionRate = mGrid.disease().infectionRate() * p_infectious;
                
     //        Make sure that the infection rate stays within rational bounds:
     infectionRate = min(infectionRate, 0.50);

     double noWater = pvGet(eFractionNoWater) * pvfGet(ePopulation);

     double at_home             = pcfGet(ePAtHome);
     double sheltered           = pvfGet(eSheltered);
     double unsheltered         = pcfGet(ePUnsheltered);
     
     double home_ill            = pcfGet(ePHomeIll);
     double sheltered_ill       = pcfGet(ePShelteredIll);
     double unsheltered_ill     = pcfGet(ePUnshelteredIll);

     double home_immune         = pcfGet(ePHomeImmune);
     double sheltered_immune    = pcfGet(ePShelteredImmune);
     double unsheltered_immune  = pcfGet(ePUnshelteredImmune); 

     double home_at_risk        = min(noWater, max(0.0, at_home     - home_ill        - home_immune));
     double sheltered_at_risk   = min(noWater, max(0.0, sheltered   - sheltered_ill   - sheltered_immune));
     double unsheltered_at_risk = min(noWater, max(0.0, unsheltered - unsheltered_ill - unsheltered_immune));

     double home_caught         = XPoisson( infectionRate * home_at_risk       , home_at_risk        );
     double sheltered_caught    = XPoisson( infectionRate * sheltered_at_risk  , sheltered_at_risk   );
     double unsheltered_caught  = XPoisson( infectionRate * unsheltered_at_risk, unsheltered_at_risk );
     
     home_ill        += ( home_caught        - pcfGet(ePHomeDead)        - recoveryRate * home_ill        );
     sheltered_ill   += ( sheltered_caught   - pcfGet(ePShelteredDead)   - recoveryRate * sheltered_ill   );
     unsheltered_ill += ( unsheltered_caught - pcfGet(ePUnshelteredDead) - recoveryRate * unsheltered_ill );
                          
     *data = (home_ill + sheltered_ill + unsheltered_ill) / pvfGet(ePopulation);

     handleRoundOffErrorsFraction(data);
}

void GridCell::doFractionRecovered(double* data)
{
     double recoveryRate        = mGrid.disease().recoveryRate();
     double home_immune         = pcfGet(ePHomeImmune) + recoveryRate * pcfGet(ePHomeIll);
     double sheltered_immune    = pcfGet(ePHomeImmune) + recoveryRate * pcfGet(ePShelteredIll);
     double unsheltered_immune  = pcfGet(ePHomeImmune) + recoveryRate * pcfGet(ePUnshelteredIll);
     
     *data = (home_immune + sheltered_immune + unsheltered_immune) / pvfGet(ePopulation);

     handleRoundOffErrorsFraction(data);
}

void GridCell::doFractionNoWork(double* data)
{
//      double ret = 0;
//      if (pvfGet(ePopulation) > kMinPopulation) {
//           // The theoretical maximum workforce:
//           double maxWorkforce = pvfGet(ePopulation) * mGrid.mp().mp(eWorkforceFraction);
          
//           // Number of employed people:
//           double employed =  maxWorkforce * (1 - pvGet(eFractionNoWork));
          
//           // The number of new hired workers under normal conditions.
//           double normalHired = mGrid.mp().mp(eFractionNewHired) * (maxWorkforce - employed);
          
//           // Net number of hired/fired workers:
//           double deltaWorkers = normalHired * (mGrid.mp().mp(eEmployerRiskTakingFactor) - pvfGet(ePerceivedThreat) / 100.0);
//           deltaWorkers = max(-employed, deltaWorkers);
          
//           // Current unemployment:
//           ret = 1 - ((employed + deltaWorkers) / maxWorkforce);
//      }
//      *data = ret;
     *data = pvGet(eFractionNoWork);
}

void GridCell::doFractionNoFood(double* data)
{
     double foodDays = pvGet(eFoodDays);
     *data = (foodDays > 0.0 ? pow(1.3, -foodDays) : 1.0);
     handleRoundOffErrorsFraction(data);
}

void GridCell::doFractionNoWater(double* data)
{
     double totalWater = pvGet(eWaterDays) + (pvGet(eSuppliedWater) / pvfGet(ePopulation));
     *data = (totalWater > 0.0 ? pow(3.0, -totalWater) : 1.0);
     handleRoundOffErrorsFraction(data);
}

void GridCell::doInfrastructure(double* data)
{
//      // The theoretical maximum workforce:
//      double maxWorkforce = pvfGet(ePopulation) * mGrid.mp().mp(eWorkforceFraction);
     
//      // Number of employed people:
//      double employed =  maxWorkforce * pvGet(eFractionNoWork);

//      // The number of new hired workers under normal conditions.
//      double normalHired = mGrid.mp().mp(eFractionNewHired) * (maxWorkforce - employed);
     
//      // Net number of hired/fired workers:
//      double deltaWorkers = normalHired * (mGrid.mp().mp(eEmployerRiskTakingFactor) - pvfGet(ePerceivedThreat) / 100.0);
//      deltaWorkers = max(-employed, deltaWorkers);

     // Infrastructure is reduced with one thenth of a percent by a
     // faction if that faction has more than 0.1 persons per km2, its
     // population is more than one percent of the blue force
     // personnel in this cell.
     double part = 1;
     for (int i = 1; i < sFactions + 1; ++i) {
          double ins = pvfGet(eInsurgents, i);
          if (ins / sCellAreaKm2 > 0.1 &&
              ins / mGrid.cg()->value(index(), mGrid.cg()->blueLayer()) > 0.01) {
               part -= 0.001;
          } 
     }
     *data = pvGet(eInfrastructure) * part;
//      *data = pvGet(eInfrastructure) * part * (1 - mGrid.mp().mp(eInfrastructureDecay)) +
//           (mGrid.mp().mp(eFractionInfrastructureWorkers) * deltaWorkers) / employed;

     handleRoundOffErrorsFraction(data);
}


void GridCell::doTEST(double* data)
{
     *data = 0;
//     *data = pvrGet(eRFoodSurplusDeficit);
//     *data = mRegions.size();
}


void GridCell::doDDisaffection(double* data)
{
     double pNoShelter;
     double temp;
     double mean = 0;

     for (int i = 1; i < sFactions + 1; ++i) {
          if (pvfGet(ePopulation, i) > 1.0) {
               pNoShelter = between(pcfGet(ePUnsheltered, i) / pvfGet(ePopulation, i), 0.0, 1.0);
               temp       = (1.0 - (pvGet(eFractionNoFood) + pvGet(eFractionNoWork) +
                                    pNoShelter + 0.01 * pvfGet(ePerceivedThreat, i)) / 5.0 );
               temp      *= (1.0 - (pvGet(eFractionNoWater) + pvGet(eFractionNoMedical) + 
                                    pvGet(eFractionCrimeVictims)) / 4.0 );
               
               data[i] = 100.0 * (1.0 - temp);
               mean += data[i] * pvfGet(ePopulation, i);
          }
          else {
               data[i] = 0;
          }
     }

     data[0] = mean / pvfGet(ePopulation);

     handleRoundOffErrorsPercent(data, sFactions + 1);
}

void GridCell::doDPolarization(double* data)
{
     double disaffect, groupPop, mean1, mean2, var;
     double groups = 0.0, sum0 = 0.0, sum1 = 0.0, sum2 = 0.0;
        
     if (sFactions <= 1) {
          *data = 0.0;
          return;
     }
     else {
          for (int i = 1; i < sFactions + 1; ++i)  {
               groupPop = pvfGet(ePopulation, i);
               disaffect = pdfGet(eDDisaffection, i);

               sum0 += disaffect * groupPop;                   //        pop-weighted disaffection

               if ( groupPop >= 1.0 ) {        
                    groups += 1.0;
                                
                    sum1 += disaffect;                        //        unweighted disaffection
                    sum2 += disaffect * disaffect;        //        second unweighted moment
               }
          }                
          if ( groups > 1.0 ) {
               mean1 = sum1 / groups;
               mean2 = sum2 / groups;
               var = mean2 - mean1 * mean1;

               // The update
               *data = (var > 0.0 ? sqrt(var) : 0.0);
          }
          else {
               *data = 0.0;
          }
     }
     handleRoundOffErrorsPercent(data);
}

void GridCell::doDHoused(double* data)
{
     double atHome = pvfGet(ePopulation) - pvfGet(eDisplaced) - pvfGet(eProtected);
     *data = min(pvGet(eHousingUnits) * mGrid.mp().mp(eMeanFamilySize), atHome);
     handleRoundOffErrorsPositive(data);
}

void GridCell::doDAvailableFood(double* data)
{
//      static double fracTot = 0;
//      static double fracTotN = 0;
//      static double totImp = 0;
//      static double totImpN = 0;
//      if (row() == 1 && col() == 1) {
//           fracTot = 0;
//           fracTotN = 0;
//           totImp = 0;
//           totSuD = 0;
//           totImpN = 0;
//           totSuDN = 0;
//      }
//      if (pvfGet(ePopulation) > 0) {
//           debug(row() << ", " << col());
//      }

     double cellImport = 0;
     for (vector<const Region*>::iterator it = mRegions.begin(); it != mRegions.end(); ++it) {
          const Region& r = **it;
          double pop = pvfGet(ePopulation) * weight();
          double fracOfRegion =  pop / r.cellGroup().pvfGet(ePopulation);
          double fracOfTotal = pop / mGrid.totalPopulation();
          double prodShare = fracOfRegion * r.pvrGet(eRFoodProduction);
          double importFromAbroad = fracOfTotal * mGrid.mp().mp(eFoodImportFromAbroad);
          double ownSurpDef = prodShare + importFromAbroad - pop * kFoodPPPDKg / 1000.0;
          double regSurpDef = r.pvrGet(eRFoodSurplusDeficit);
          double tot = (regSurpDef > 0 ? mGrid.regionFoodSurplus() : -mGrid.regionFoodDeficit());
          double regImport = (tot != 0 ? -regSurpDef / tot * min(mGrid.regionFoodSurplus(), -mGrid.regionFoodDeficit()) : 0);
          cellImport += (regSurpDef != 0 ? regImport * ownSurpDef / regSurpDef : 0);          

//           if (ownSurpDef > 0) {
//                fracTot += ownSurpDef / regSurpDef;
//           }
//           else {
//                fracTotN += ownSurpDef / regSurpDef;
//           }
           if (pvfGet(ePopulation) > 0) {
//                debug("  prod        : " << prodShare << " (" << prodShare / r.pvrGet(eRFoodProduction) << " of tot for region)");
//                debug("  weighted pop: " << pvfGet(ePopulation) * weight() << " ("
//                      << pvfGet(ePopulation) * weight() / r.cellGroup().pvfGet(ePopulation) << " of tot for region)");
//                debug("  ownSurpDef  : " << ownSurpDef << " (" << ownSurpDef / regSurpDef << " of tot for region)");
//                debug("  contrib from " << r.ref().name() << ": " << (regSurpDef != 0 ? regImport * ownSurpDef * weight() / regSurpDef : 0));
//                debug("  fracTot     : " << fracTot);
//                debug("  fracTotN    : " << fracTotN);
           }
     }

//      if (cellImport > 0) {
//           totImp += cellImport;
//      }
//      else {
//           totImpN += cellImport;
//      }
     if (pvfGet(ePopulation) > 0) {
//           debug("  cellImport: " << cellImport);
//           debug("  totImp    : " << totImp);
//           debug("  totImpN   : " << totImpN);
     }

     *data = pvGet(eStoredFood) + pvGet(eMarketedFood) + cellImport;
     
     handleRoundOffErrorsPositive(data);
}

void GridCell::doDFoodDeprivation(double* data)
{
     *data = 1 - (pvGet(eFoodConsumption) / kFoodPPPDKg);
     handleRoundOffErrorsFraction(data);
}

void GridCell::doDWaterSurplusDeficit(double* data)
{
     *data = pcGet(ePWaterProduction) - pvfGet(ePopulation) * 20;
}

void GridCell::doDWaterDeprivation(double* data)
{
     *data = 1 - pvGet(eWaterConsumption) / 20.0;
     handleRoundOffErrorsFraction(data);
}


void GridCell::doPrecalculated()
{
     // Epidemics stuff
     double recoveryRate  = mGrid.disease().recoveryRate();
     double hungerFactor  = (pvGet(eFoodDays) >= 2.0  ?  1.0 : 1.0 / (pvGet(eFoodDays) * 0.5));
     double mortalityRate = min(0.5, mGrid.disease().mortalityRate() * hungerFactor);
     double highMortality = min(0.9 * (1.0 - recoveryRate), 5.0 * mortalityRate);

     for (int i = 0; i < sFactions + 1; ++i) {
          pcfSet(ePAtHome     , i, max(0.0, pvfGet(ePopulation, i) - pvfGet(eDisplaced, i) - pvfGet(eProtected, i)));
          pcfSet(ePSheltered  , i, max(0.0, pvfGet(eSheltered, i)));
          pcfSet(ePUnsheltered, i, max(0.0, pvfGet(eDisplaced, i) - pvfGet(eSheltered, i)));

          double sum = pcfGet(ePAtHome, i) + pcfGet(ePSheltered, i) + pcfGet(ePUnsheltered, i) + pvfGet(eProtected, i);
          if (sum > pvfGet(ePopulation, i)) {
               double factor = pvfGet(ePopulation, i) / sum;
               pcfSet(ePAtHome     , i, pcfGet(ePAtHome, i) * factor);
               pcfSet(ePSheltered  , i, pcfGet(ePSheltered, i) * factor);
               pcfSet(ePUnsheltered, i, pcfGet(ePUnsheltered, i) * factor);
               double sum2 = pcfGet(ePAtHome, i) + pcfGet(ePSheltered, i) + pcfGet(ePUnsheltered, i) + pvfGet(eProtected, i);
               if (sum2 > pvfGet(ePopulation, i)) {
                    debug("sum2 > population[" << i << "]: " << sum2 << " > " << pvfGet(ePopulation, i) 
                         << ", factor: " << factor);
               }
          }

          pcfSet(ePHomeIll          , i, max(0.0, pvGet(eFractionInfected) * pcfGet(ePAtHome     , i)));
          pcfSet(ePShelteredIll     , i, max(0.0, pvGet(eFractionInfected) * pcfGet(ePSheltered  , i)));
          pcfSet(ePUnshelteredIll   , i, max(0.0, pvGet(eFractionInfected) * pcfGet(ePUnsheltered, i)));

          pcfSet(ePHomeImmune       , i, max(0.0, pvGet(eFractionRecovered) * pcfGet(ePAtHome     , i)));
          pcfSet(ePShelteredImmune  , i, max(0.0, pvGet(eFractionRecovered) * pcfGet(ePSheltered  , i)));
          pcfSet(ePUnshelteredImmune, i, max(0.0, pvGet(eFractionRecovered) * pcfGet(ePUnsheltered, i)));

          pcfSet(ePHomeDead         , i, max(0.0, mortalityRate * pcfGet(ePHomeIll       , i)));
          pcfSet(ePShelteredDead    , i, max(0.0, mortalityRate * pcfGet(ePShelteredIll  , i)));
          pcfSet(ePUnshelteredDead  , i, max(0.0, highMortality * pcfGet(ePUnshelteredIll, i)));

     }

     // Violence related casualties and displaced people.
     for (int i = 1; i < sFactions + 1; ++i) {
          double v = 0.01 * pvfGet(eViolence, i);   // 0 < v < 1

          if (v > 0.8)        {   //  violence greater than 80
               pcfSet(ePIDPDeadDueToViolence, i, 0.01 * v * pcfGet(ePUnsheltered, i));
               pcfAdd(ePIDPDeadDueToViolence, 0, pcfGet(ePIDPDeadDueToViolence, i));

               pcfSet(ePNewIDPDueToViolence, i, 0.10 * v * pcfGet(ePAtHome, i));
               pcfAdd(ePNewIDPDueToViolence, 0, pcfGet(ePNewIDPDueToViolence, i));

               pcfSet(ePHousedDeadDueToViolence, i, 0.00333 * v * pcfGet(ePAtHome, i));
               pcfAdd(ePHousedDeadDueToViolence, 0, pcfGet(ePHousedDeadDueToViolence, i));
          }
          else if (v > 0.5) {   // violence between 50 and 80
               // People are being made homeless:
               pcfSet(ePNewIDPDueToViolence, i, 0.05 * v * pcfGet(ePAtHome, i));
               pcfAdd(ePNewIDPDueToViolence, 0, pcfGet(ePNewIDPDueToViolence, i));
          }
     }
                
     // Refugee movement stuff
     if (true) {   // Handle for switching model on or off.
          const double limit = 100000;
          const double meanSpeed = kRefugeeMeanSpeed;                // average IDP speed in km per day
          const double stdvSpeed = kRefugeeSpeedStandardDeviation;   // standard deviation of IDP speed
          const double iv = -0.5 / (stdvSpeed * stdvSpeed);

          double distance_m;

          //        Normal movements of displaced persons towards IDP camps:
          for (int i = 1; i < sFactions + 1; ++i ) {
               double dist2, movement, movingPop;
               double moveablePop = pcfGet(ePUnsheltered, i);

               if (moveablePop > kMinPopulation) {
                    GridCell *centroid;
                    GridCell *temp;
                    double centroid_row, centroid_col, adjust, ctrProp, prop[eNumNeighbors];
                                
                    GridCell* target = mGrid.getCellForNearestCamp(center(), distance_m);
                    if (target && target != this) {
                         /*
                           The fraction moving drops exponentially from 40% down to zero.
                           IDPs move to a centroid cell and all of its neighbors, using
                           an approximate 2D normal distribution (to display diffusion).
                         */
                         movingPop    = kFractionMovingRefugees * exp(-distance_m / limit) * moveablePop;
                         if (movingPop > kMinPopulation) {
                              movement     = min(1.0, meanSpeed / distance_m);
                              centroid_row = row() + movement * (target->row() - row());
                              centroid_col = col() + movement * (target->col() - col());
                              centroid     = mGrid.cell(Round(centroid_row), Round(centroid_col));
                              
                              // If centroid cell is outside grid no people will be moved.
                              if (centroid) {
                                   double sum = 1.0;
                                   ctrProp = sum;
                                   for (int d = 0; d < eNumNeighbors; d++) {
                                        temp = centroid->neighbor(d);
                                        if (temp) {
                                             dist2 = temp->squDistanceTo(*centroid);
                                             prop[d] = exp(iv * dist2);
                                             sum += prop[d];
                                        }
                                   }
                                        
                                   adjust = 1.0 / sum;
                                   for (int d = 0; d < eNumNeighbors; d++) {
                                        prop[d] *= adjust;
                                   }
                                   ctrProp *= adjust;
                                        
                                   double delta;
                                   double actualMovingPop = 0;
                                   for (int d = 0; d < eNumNeighbors; d++) {                                        
                                        temp = centroid->neighbor(d);
                                        if (temp) {
                                             delta = prop[d] * movingPop;
                                             // Don't move less than kMinPopulation people.
                                             if (delta > kMinPopulation) {
                                                  temp->pcfAdd(ePTowardsCampDelta, i, delta);
                                                  temp->pcfAdd(ePTowardsCampDelta, 0, delta);
                                                  actualMovingPop += delta;
                                             }
                                        }
                                   }
                                        
                                   // Don't move less than kMinPopulation people.
                                   delta = ctrProp * movingPop;
                                   if (delta > kMinPopulation) {
                                        centroid->pcfAdd(ePTowardsCampDelta, i, delta);
                                        centroid->pcfAdd(ePTowardsCampDelta, 0, delta);
                                        actualMovingPop += delta;
                                   }
                                   if (actualMovingPop > kMinPopulation) { 
                                        pcfAdd(ePTowardsCampDelta, i, -actualMovingPop);
                                        pcfAdd(ePTowardsCampDelta, 0, -actualMovingPop);
                                   }
                              }
                         }
                    }
               }
          }
     }

     // Diffusion stuff e.g. threat driven movement
     if (true) {   // Handle for switching model on or off.
          /*
            PROBLEM: This algorithm diffuses people to neighboring cells.
            If the user changes the cell size, then the rate of diffusion
            changes. This algorithm must be strengthened so that it is
            not dependent on the size of the cell.
          */
          double movingPop, unshelteredPop, homePop;
          GridCell* destination = 0;
          for (int i = 1; i < sFactions + 1; ++i ) {
               // Consequence of high perceived threat: people leave home
               if (pvfGet(ePerceivedThreat, i) > kDiffusionThreatThreshold) {
                    //        Find the nearby cell that has the lowest threat:
                    double bestThreat = pvfGet(ePerceivedThreat, i);
                    for (int j = eN; j < eNumNeighbors; j++) {
                         GridCell* nbor = neighbor(j);
                         if (nbor) {
                              double nborThreat = nbor->pvfGet(ePerceivedThreat, i);
                              if (bestThreat > nborThreat) {
                                   bestThreat = nborThreat;
                                   destination = nbor;
                              }
                         }
                    }

                    /*
                      If there is a nearby cell with a lower threat, then
                      move some people to the cell with the lowest threat:
                    */
                    if (destination) {
                         unshelteredPop = pcfGet(ePUnsheltered, i);   // Should be the same timestep
                         homePop        = pcfGet(ePAtHome     , i);   // Should be the same timestep
                                        
                         // 20% of displaced and unsheltered population moves:
                         movingPop = kDiffusionFractionUnshelteredMoving * unshelteredPop;
                         
                         // Group
                         pcfAdd(ePDiffusionDelta                      , i, -movingPop);
                         pcfAdd(ePDiffusionDisplacedDelta             , i, -movingPop);
                         destination->pcfAdd(ePDiffusionDelta         , i,  movingPop);
                         destination->pcfAdd(ePDiffusionDisplacedDelta, i,  movingPop);

                         // Total
                         pcfAdd(ePDiffusionDelta                      , 0, -movingPop);
                         pcfAdd(ePDiffusionDisplacedDelta             , 0, -movingPop);
                         destination->pcfAdd(ePDiffusionDelta         , 0,  movingPop);
                         destination->pcfAdd(ePDiffusionDisplacedDelta, 0,  movingPop);

                         //        10% of population living at home moves:
                         movingPop = kDiffusionFractionAtHomeMoving * homePop;
                                                                                
                         // Group
                         pcfAdd(ePDiffusionDelta                      , i, -movingPop);
                         destination->pcfAdd(ePDiffusionDelta         , i,  movingPop);
                         destination->pcfAdd(ePDiffusionDisplacedDelta, i,  movingPop);

                         // Total
                         pcfAdd(ePDiffusionDelta                      , 0, -movingPop);
                         destination->pcfAdd(ePDiffusionDelta         , 0,  movingPop);
                         destination->pcfAdd(ePDiffusionDisplacedDelta, 0,  movingPop);

                         //        Note: NONE of the sheltered population moves.
                    }
               }
          }
     }
     
//      // Food production
//      double prod = 0;
//      if (pvfGet(ePopulation) > 0) {
//           for (vector<const Region*>::iterator it = mRegions.begin(); it != mRegions.end(); ++it) {
//                double myShare = pvfGet(ePopulation) * weight() / (*it)->cellGroup().pvfGet(ePopulation);
//                prod += myShare * (*it)->pvrGet(eRFoodProduction);
//           }
//      }
//      pcSet(ePFoodProduction, prod);

//      // Water production
//      double rain = regionParam(eRDailyRainfallMeters) * sCellAreaKm2 * 1e6;
//      double wells = (pdGet(eDWaterSurplusDeficit) < 0 ? mWellWaterFraction * abs(pdGet(eDWaterSurplusDeficit)) : 0);

//      pcSet(ePWaterProduction,
//            (rain * mGrid.mp().mp(eFractionRainfallCollected) * 
//             mGrid.mp().mp(eFractionCollectedPurified) + wells) *
//            (1 - pvfGet(ePerceivedThreat) / 100.0));
}

void GridCell::exposePercent(double* data, const EthnicFaction& fac, double size)
{
     double mean = 0.0;
     for (int i = 1; i < sFactions + 1; ++i) {
          if (fac.isAll() || fac.index() == i) {
               double target = (size > 0.0 ? 100.0 : 0.0);
               data[i] += fabs(size) * (target - data[i]);
          }
     }
     for (int i = 1; i < sFactions + 1; ++i) {
          mean += data[i] * pvfGet(ePopulation, i);
     }                
     data[0] = mean / pvfGet(ePopulation);
}

void GridCell::exposeDisplaced(double* data, const EthnicFaction& fac, double size)
{
     for (int i = 1; i < sFactions + 1; ++i) {
          if (fac.isAll() || fac.index() == i) {
               double target = (size > 0.0 ? pvfGet(ePopulation, i) - pvfGet(eProtected, i) : 0.0);
               double delta = fabs(size) * (target - data[i]);
               data[i] += delta;
               data[0] += delta;
          }
     }
}

void GridCell::exposeSheltered(double* data, const EthnicFaction& fac, double size)
{
     for (int i = 1; i < sFactions + 1; ++i) {
          if (fac.isAll() || fac.index() == i) {
               double target = (size > 0.0 ? pvfGet(eDisplaced, i) : 0.0);
               double delta = fabs(size) * (target - data[i]);
               data[i] += delta;
               data[0] += delta;
          }
     }
}

void GridCell::exposeProtected(double* data, const EthnicFaction& fac, double size)
{
     for (int i = 1; i < sFactions + 1; ++i) {
          if (fac.isAll() || fac.index() == i) {
               double target = (size > 0.0 ? pvfGet(eProtected, i) +
                                pvfGet(eDisplaced, i) - pvfGet(eSheltered) : 0.0);
               double delta = fabs(size) * (target - data[i]);
               delta = min(delta, pvfGet(eDisplaced, i) - pvfGet(eSheltered, i));
               if (delta < kMinPopulation) {
                    continue;
               }

               if (data[i] != pvfGet(eProtected, i)) {
                    debug("data[" << i << "] != pvfGet(eProtected, " << i << ") - "
                         << data[i] << " != " << pvfGet(eProtected, i));
               }

               data[i] = pvfGet(eProtected, i) + delta;
               pvfAdd(eDisplaced, i, -delta);   // Only displaced people gets protected
               pvfAdd(eDisplaced, 0, -delta);   // Only displaced people gets protected

               if (pvfGet(eDisplaced, i) < pvfGet(eSheltered, i) ||
                   pvfGet(eDisplaced, i) + data[i] > pvfGet(ePopulation, i)) {
                    debug("target: " << target << ", delta: " << delta);
                    debug("displaced: " << pvfGet(eDisplaced, i) << ", data[" << i << "]: " << data[i]);
                    debug("sheltered: " << pvfGet(eSheltered, i));
                    debug("displaced + protected: " << pvfGet(eDisplaced, i) + data[i]);
               }
          }
     }
     // 
     data[0] = 0;
     for (int i = 1; i < sFactions + 1; ++i) {
          data[0] += data[i];
     }
}

void GridCell::exposeInsurgents(double* data, const EthnicFaction& fac, double size)
{
     for (int i = 1; i < sFactions + 1; ++i) {
          if (fac.isAll() || fac.index() == i) {
               double target = (size > 0.0 ? pvfGet(ePopulation, i) *
                                mGrid.mp().mp(eFractionPotentialInsurgents) : 0.0);
               double delta = fabs(size) * (target - data[i]);
               data[i] += delta;
               data[0] += delta;
          }
     }
}


void GridCell::exposeFraction(double* data, double size)
{
     double target = (size > 0.0 ? 1.0 : 0.0);
     *data += fabs(size) * (target - *data);
}

void GridCell::exposeFoodDays(double* data, double size)
{
     double target = (size > 0.0 ? bestFoodStorage() : 0.0);
     *data += fabs(size) * (target - *data);
}

void GridCell::exposeWaterDays(double* data, double size)
{
     double target = (size > 0.0 ? bestWaterCapacity() : 0.0);
     *data += fabs(size) * (target - *data);
}

void GridCell::exposeFractionInfected(double* data, double size)
{
     // The upper commented row was the previous model that evidently is
     // corrupt, since we get a lower target the more infected people we have
//     double target = (size < 0.0 ? 0.0 : max(1.0 - pvGet(eFractionRecovered) - *data, 0.0));
     double target = (size < 0.0 ? 0.0 : max(1.0 - pvGet(eFractionRecovered), 0.0));
     double delta = fabs(size) * (target - *data);
     if (fabs(delta) > 0.00001) {
          *data += delta;
          if (delta < 0.0) {
               //        Some infected people have been cured:
               pvAdd(eFractionRecovered, -delta);
          }
     }
}

void GridCell::exposeFractionRecovered(double* data, double size)
{
     if (size > 0.0) {
          double target = max(1.0 - *data - pvGet(eFractionInfected), 0.0);
          double delta = fabs(size) * (target - *data);
          *data += delta;
     }
}

