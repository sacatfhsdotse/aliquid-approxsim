#ifndef _APPROXSIMCONSTANTS_H
#define _APPROXSIMCONSTANTS_H

/**
 *   \file ApproxsimConstants.h
 *
 *   \brief This file contains some constants that are used by the
 *   Approxsim simulation.
 */

const double kMinPopulation  = 0.5;       ///<   Minimum population a cell must have for the simulation to update it.
const double kMaxStoredFood  = 15.0;      ///<   Maximum days of locally stored food
const double kMaxStoredWater = 5.0;       ///<   Maximum days of locally stored clean water
const double kNoCampZone     = 100000.0;  ///<   Do not build a new camp within this distance (m) of any other


/**
 *   \brief This is the fraction of the protected people in each cell
 *   that resettles every day. The resettling destination is based on
 *   the initial population of each cell.
 */
const double kFractionProtectedResettling        =  0.005;

/**
 *   \brief If the threat level rises above
 *   hcDiffusionThreatThreshold, people start moving to neighboring
 *   cells with lower threat.
 */
const double kDiffusionThreatThreshold           = 65.0  ;

/**
 *   \brief This is the fraction of the unsheltered people that moves
 *   when the threat level rises above kDiffusionThreatThreshold
 *   moves.
 */
const double kDiffusionFractionUnshelteredMoving =  0.20 ;

/**
 *   \brief This is the fraction of the people still living at home
 *   that moves when the threat level rises above
 *   kDiffusionThreatThreshold moves.
 */
const double kDiffusionFractionAtHomeMoving      =  0.10 ;

/**
 *   \brief Mean speed of refugee movements in m/day.
 */
const double kRefugeeMeanSpeed                   = 30000.0  ;

/**
 *   \brief Standard deviation of refugee movement speed. 
 */
const double kRefugeeSpeedStandardDeviation      = 15000.0  ;

/**
 *   \brief This is the fraction of refugees that moves towards a
 *   camp.
 */
const double kFractionMovingRefugees             =  0.40 ;

/**
 * \brief Food consumed per person per day in kg.
 */
const double kFoodPPPDKg = 1.0;

#endif   // _APPROXSIMCONSTANTS_H
