#ifndef STRATMAS_RANDOM_H
#define STRATMAS_RANDOM_H

/**
 * \file random.h"
 *
 * \brief This file contains some useful functions for handling random
 * numbers and different probability distributions.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/05 14:18:21 $
 */

// System
#include <cmath>
#include <cstdlib>
#include <ctime>
#include <algorithm>   // For min() and max()

// Temporary
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>


// Own
#include "LogStream.h"

//#define NORANDOM


using namespace std;

// Constants

/// The maximum value that may be returned by random().
const double kRandomMax = static_cast<double>(RAND_MAX);

/**
 * \brief Helper class for handling random numbers that should not
 * interfere with the sequence of random numbers generated during a
 * simlation.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/05 14:18:21 $
 */
class PrivateRandom {
private:
     /// Size of array of random numbers that may not affect random number repeatability.
     static const int kNumRand = 10000;
     
     /// Current index in the random number array.
     static int sRandIndex;

     /**
      * \brief Array of random numbers allowing generation of random
      * numbers that does not affect random number repeatability.
      */
     static long sRandNum[kNumRand];

public:
     /**
      * \brief Initializes an array of random numbers for later use.
      *
      * Should be called once (and only once) at startup.
      */
     static inline void initRandomNumberArray() {
#ifdef __win__
          srand(4711);
#else
           srandom(4711);
#endif
           for (int i = 0; i < kNumRand; i++) {
#ifdef __win__
               sRandNum[i] = rand();
#else
                sRandNum[i] = random();
#endif
          }
     }
     
     /**
      * \brief Returns a random long that is uniformly distributed between
      * zero and kRandomMax. This function does not influence the order of
      * random numbers generated during a simulation.
      *
      * This function exists due to the fact that Shapes use random numbers
      * to keep track of whether they have changed or not and the sequence
      * of Shape creation may differ from one simulation to another
      * (depending on subscriptions for example). If Shapes gets random
      * numbers from RandomUniform() the sequence may not be repeatable.
      *
      * \return A random undigned long that is uniformly distributed
      * between zero and kRandomMax.
      */
     static inline long privateRandomUniform() {
          return sRandNum[(++sRandIndex) % kNumRand];
     }


};


/**
 * \brief Helper class for storing info of the number that is saved by
 * the gaussian random number algorithm.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/05 14:18:21 $
 */
class GaussSaver {
private:
     /// Indicates wheter a number is saved or not.
     static bool smSaved;
public:
     /**
      * \brief Mutator for the saved flag.
      *
      * \param saved The new state of the saved flag.
      */
     inline static void setSaved(bool saved) {
          smSaved = saved;
     }

     /**
      * \brief Accessor for the saved flag.
      *
      * \return The new state of the saved flag.
      */
     inline static bool isSaved() {
          return smSaved;
     }
};

/**
 * \brief Sets the random seed.
 *
 * \param seed The new random seed.
 */
inline void setRandomSeed(unsigned long seed) 
{ 
     GaussSaver::setSaved(false); 
#ifdef __win__
     srand(seed);
#else
     srandom(seed);
#endif
}

/**
 * \brief Creates a seed for srandom based on time().
 */
inline unsigned long createRandomSeed() { return time(0); }

inline double RandomUniform();
inline double RandomUniform(double low, double high);
inline double Gaussian(double scale, double mean = 0.0);
inline int Poisson(double lambda );
inline double XPoisson(double lambda, double clip);
inline double Exponential(double mean);

/**
 * \brief Returns a random double that is uniformly distributed
 * between zero and one.
 *
 * \return A random double that is uniformly distributed between zero
 * and one.
 */
inline double RandomUniform()
{
#ifdef NORANDOM
     static bool firstTime =true;
     if (firstTime) {
          firstTime = false;
          slog << "---------------------- NO RANDOM ----------------------" << logEnd;
     }     
     return 0.5;
#endif     

#ifdef __win__
     double num = static_cast<double>(rand()) / kRandomMax;
#else
     double num = static_cast<double>(random()) / kRandomMax;
#endif
     return num;
}

/**
 * \brief Returns a random double that is uniformly distributed
 * between specified limits.
 *
 * \author Loren Cobb
 *
 * \param low Lower bound
 * \param high Upper bound
 * \return A random double that is uniformly distributed in the
 * intervall [low, high]
 */
inline double RandomUniform(double low, double high)
{
     double num = low + RandomUniform() * (high - low);
     return num;
}

/**
 * \brief Return a random double with a normal density centered on the
 * mean, with standard deviation = scale.
 *
 * \author Loren Cobb
 *
 * \param scale Standard deviation
 * \param mean Mean
 * \return A random double with a normal density.
 */
double Gaussian( double scale, double mean )
{
#ifdef NORANDOM
     static bool firstTime =true;
     if (firstTime) {
          firstTime = false;
          slog << "---------------------- NO RANDOM IN GAUSSIAN----------------------" << logEnd;
     }     
     return mean;
#endif     
     double result = mean;   // NOTE:  default value of mean is zero.

     //        Fast Box-Muller method:
     double U1, U2, V, W;
//     static bool         stored = false;
     static double       save;
     static const double kFactor = 2.0 / double(RAND_MAX);
        
     if (GaussSaver::isSaved()) {
          result = save;
          GaussSaver::setSaved(false);
     }
     else {
          do {
               U1 = kFactor * RandomUniform(0, RAND_MAX) - 1.0;
               U2 = kFactor * RandomUniform(0, RAND_MAX) - 1.0;
               W  = U1*U1 + U2*U2;
          } while ( W > 1.0 );
                
          V = sqrt( -2.0 * log(W) / W );
          result = U1 * V;
          save   = U2 * V; 
          GaussSaver::setSaved(true);
     }
                
     result = mean + scale * result;                //        approximate Normal(mean,scale*scale)

     return result;
}

/**
 * \brief Return a random integer with a Poisson distribution
 *
 * \author Loren Cobb
 *
 * \param lambda The expected value of the distribution.
 * \return A random double with a Poisson distribution.
 */
inline int Poisson(double lambda) {
     double U, f, cf, x;
     int    k, n, result;
        
     if ( lambda <= 0.0 ) {
          result = 0;
     }
     else if ( lambda < 9.0 ) {
          U = RandomUniform();
          f = exp( -lambda );
          if ( U <= f ) {
               result = 0;
          }
          else {
               cf = f;
               n = max( 5, int(lambda+3*sqrt(lambda)) );
               for ( k = 1; k < n; k++ ) {
                    f = lambda * f / k;
                    cf += f;
                    if ( U <= cf )
                         break;
               }
               result = k;
          }
     }
     else {        //        Use Gaussian approximation:
          x = Gaussian( sqrt(lambda), lambda );
          result = max( 0, int(x+0.5) );
     }
        
     return result;
}
/**
 * \brief Return a random integer with a Poisson distribution
 * (truncated).
 *
 * \author Loren Cobb
 *
 * \param lambda The expected value of the distribution.
 * \param clip The point of truncation.
 * \return A random double with a Poisson distribution (truncated).
 */
inline double XPoisson( double lambda, double clip ) {
     double                U, f, cf, x,
          k, n, result;
     
     if ( lambda <= 0.0 ) {
          result = 0.0;
     }
     else if ( lambda < 9.0 ) {
          U = RandomUniform();
          f = exp( -lambda );
          if ( U <= f ) {
               result = 0;
          }
          else {
               cf = f;
               n = max( 5.0, lambda + 3.0*sqrt(lambda) );
               for ( k = 1.0; k < n; k++ ) {
                    f = lambda * f / k;
                    cf += f;
                    if ( U <= cf )
                         break;
               }
               result = k;
          }
     }
     else { //        Use Gaussian approximation:
          x = Gaussian( sqrt(lambda), lambda );
          result = max( 0.0, floor(x+0.5) );
     }
     
     return min( result, clip );
}

/**
 * \brief Return a random integer with an Exponential distribution.
 *
 * \author Loren Cobb
 *
 * \param mean The mean value
 * \return A random double with an Exponential distribution.
 */
inline double Exponential(double mean)
{
     return (-mean) * log(RandomUniform());
}


/**
 * \brief Chooses one element in an array with a probability that is
 * proportional to the size of that element.
 *
 * \param sizes An array of doubles representing the sizes.
 * \param length The number of elements in the array.
 * \return The index of the element that was choosen.
 */
inline int probBySize(const double* sizes, int length)
{
     double sum = 0;
     for (int i = 0; i < length; i++) {
          sum += sizes[i];
     }

     double r = RandomUniform();
     double prob = 0;
     for (int i = 0; i < length; i++) {
          prob += sizes[i] / sum;
          if (r < prob) {
               return i;
          }
     }
     return 0;
}

#endif   // STRATMAS_RANDOM_H
