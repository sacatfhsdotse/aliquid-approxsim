#ifndef APPROXSIM_DISTRIBUTION_H
#define APPROXSIM_DISTRIBUTION_H

// System
#include <cmath>
#include <list>
#include <vector>

// Own
#include "GoodStuff.h"
#include "SimulationObject.h"

// Forward Declarations
class BasicGrid;
class GridPos;
class LatLng;

/**
 * \brief Abstract super class for all distributions.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/04/21 15:54:49 $
 */
class Distribution : public SimulationObject {
private:
public:
     Distribution();

     /**
      * \brief Creates a Distribution from the provided DataObject.
      *
      * \param d The DataObject to use for construction.
      */
     Distribution(const DataObject& d) : SimulationObject(d) {}

     /**
      * \brief Destructor.
      */
     virtual ~Distribution() {}

     /**
      * \brief Gets the value of the distribution at distance x.
      *
      * \param x The distance.
      * \return The value of the distribution at distance x.
      */
     virtual double f(double x) const = 0;

     void amount(LatLng center, const std::list<GridPos>& cells, const BasicGrid& g, std::vector<double>& outAmount) const;
     void amountMean1(LatLng center, const std::list<GridPos>& cells, const BasicGrid& g, std::vector<double>& outAmount) const;
     virtual void update(const Update& u) {}
     virtual void reset(const DataObject& d) {}
};

/**
 * \brief The distibution used to spread population from cities.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/04/21 15:54:49 $
 */
class CityDistribution : public Distribution {
private:
     /// The diffusion measure.
     double mSigma;
     
     /// Distribution constant.
     double mK;

public:
     CityDistribution(const DataObject& d);

     /**
      * \brief Gets the value of the distribution at distance x.
      *
      * \param x The distance.
      * \return The value of the distribution at distance x.
      */
     double f(double x) const { return exp(mK * x * x); }

     void update(const Update& u);
     void extract(Buffer &b) const;
     void reset(const DataObject& d);
};

/**
 * \brief A uniform distribution
 *
 * \author   Per Alexius
 * \date     $Date: 2006/04/21 15:54:49 $
 */
class UniformDistribution : public Distribution {
public:
     /**
      * \brief Creates a UniformDistribution from the provided
      * DataObject.
      *
      * \param d The DataObject to use for construction.
      */
     UniformDistribution(const DataObject& d) : Distribution(d) {}

     /**
      * \brief Gets the value of the distribution at distance x.
      *
      * \param x The distance.
      * \return The value of the distribution at distance x.
      */
     double f(double x) const { return 1; }

     /**
      * \brief Extracts data from this object to the Buffer.
      *
      * \param b The Buffer to extract data to.
      */
     void extract(Buffer &b) const {}

};


/**
 * \brief A random uniform distribution
 *
 * \author   Per Alexius
 * \date     $Date: 2006/04/21 15:54:49 $
 */
class RandomUniformDistribution : public UniformDistribution {
public:
     /**
      * \brief Creates a RandomUniformDistribution from the provided
      * DataObject.
      *
      * \param d The DataObject to use for construction.
      */
     RandomUniformDistribution(const DataObject& d) : UniformDistribution(d) {}
     double f(double x) const;
};


/**
 * \brief Normal distribution.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/04/21 15:54:49 $
 */
class NormalDistribution : public Distribution {
private:
     /// The diffusion measure.
     double mSigma;
     
     /// Distribution constant.
     double mK1;

     /// Distribution constant.
     double mK2;

public:
     NormalDistribution(double sigma);
     NormalDistribution(const DataObject& d);

     /**
      * \brief Destructor.
      */
     virtual ~NormalDistribution() {}

     /**
      * \brief Gets the value of the distribution at distance x.
      *
      * \param x The distance.
      * \return The value of the distribution at distance x.
      */
     double f(double x) const { return mK1 * exp(mK2 * x * x); }
     void update(const Update& u);
     void extract(Buffer &b) const;
     void reset(const DataObject& d);
};

#endif   // APPROXSIM_DISTRIBUTION_H
