// Own
#include "BasicGrid.h"
#include "Buffer.h"
#include "DataObject.h"
#include "Distribution.h"
#include "Error.h"
#include "GridPos.h"
#include "LatLng.h"
#include "random.h"
#include "Reference.h"
#include "Update.h"

using namespace std;


/**
 * \brief Default constructor.
 */
Distribution::Distribution() : SimulationObject(Reference::nullRef())
{
}

/**
 * \brief Calculates the fraction (of some entity) that goes to each
 * of the cells in the provided list based on the distribution
 * function and the distance between each cell and the specified
 * center point.
 *
 * \param center The coorinate to center the Distribution around.
 * \param cells A list of cell positions for which to calculate the
 * fraction.
 * \param g The grid that the cells belongs to.
 * \param outAmount On return this vector has the same size as the
 * cells list and outAmount[i] contains the fraction for the cell
 * specified by cells[i].
 */
void Distribution::amount(LatLng center,
			  const list<GridPos>& cells,
			  const BasicGrid& g,
			  vector<double>& outAmount) const
{
     outAmount.clear();
     outAmount.reserve(cells.size());
     double ff;
     double sum = 0;
     for (list<GridPos>::const_iterator it = cells.begin(); it != cells.end(); it++) {
	  ff = f(sqrt(center.squDistanceTo(g.center(it->r, it->c))));
	  sum += ff;
	  outAmount.push_back(ff);
     }

     double oneOverSum = 1 / sum;
     for (vector<double>::iterator it = outAmount.begin(); it != outAmount.end(); it++) {
	  *it *= oneOverSum;
     }
}

/**
 * \brief Based on the distribution function and the distance between
 * each cell and the specified center point this function fills the
 * outAmount list with values so that the mean value is 1.
 *
 * \param center The coorinate to center the Distribution around.
 * \param cells A list of cell positions for which to calculate the
 * fraction.
 * \param g The grid that the cells belongs to.
 * \param outAmount On return this list has the same size as the cells
 * list and outAmount[i] contains the amount for the cell specified
 * by cells[i].
 */
void Distribution::amountMean1(LatLng center,
			       const list<GridPos>& cells,
			       const BasicGrid& g,
			       vector<double>& outAmount) const
{
     outAmount.clear();
     outAmount.reserve(cells.size());
     double ff;
     double sum = 0;
     for (list<GridPos>::const_iterator it = cells.begin(); it != cells.end(); it++) {
	  ff = f(sqrt(center.squDistanceTo(g.center(it->r, it->c))));
	  sum += ff;
	  outAmount.push_back(ff);
     }

     double numOverSum = static_cast<double>(outAmount.size()) / sum;
     for (vector<double>::iterator it = outAmount.begin(); it != outAmount.end(); it++) {
	  *it *= numOverSum;
     }
}





/**
 * \brief Creates a CityDistribution from the specified DataObject.
 *
 * \param d The DataObject to create this object from.
 */
CityDistribution::CityDistribution(const DataObject& d)
     : Distribution(d),
       mSigma(d.getChild("sigmaMeters")->getDouble()),
       mK(-0.5 / (mSigma * mSigma))
{
}

/**
 * \brief Updates this object.
 *
 * \param u The Update to update this object with.
 */
void CityDistribution::update(const Update& u)
{
     if (u.getType() == Update::eModify && u.getReference().name() == "sigmaMeters") {
	  mSigma = u.getObject()->getDouble();
	  mK = -0.5 / (mSigma * mSigma);
     }
     else {
	  Error e;
	  e << "No updatable attribute '" << u.getReference().name() << "' in '" << ref() << "'";
	  throw e;
     }
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void CityDistribution::extract(Buffer &b) const
{
     DataObject& me = *b.map(ref());
     me.getChild("sigmaMeters")->setDouble(mSigma);
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void CityDistribution::reset(const DataObject& d)
{
     mSigma = d.getChild("sigmaMeters")->getDouble();
     mK = -0.5 / (mSigma * mSigma);
}



/**
 * \brief Gets the value of the distribution at distance x.
 *
 * \param x The distance.
 * \return The value of the distribution at distance x.
 */
double RandomUniformDistribution::f(double x) const
{
     return RandomUniform();
}



/**
 * \brief Constructor.
 *
 * \param sigma Diffusion measure.
 */
NormalDistribution::NormalDistribution(double sigma)
     : Distribution(),
       mSigma(sigma),
       mK1( 1 / (mSigma * sqrt(2*kPi)) ),
       mK2(-0.5 / (mSigma * mSigma))
{
}

/**
 * \brief Creates a NormalDistribution from the specified DataObject.
 *
 * \param d The DataObject to create this object from.
 */
NormalDistribution::NormalDistribution(const DataObject& d)
     : Distribution(d),
       mSigma(d.getChild("sigmaMeters")->getDouble()),
       mK1( 1 / (mSigma * sqrt(2*kPi)) ),
       mK2(-0.5 / (mSigma * mSigma))
{
}

/**
 * \brief Updates this object.
 *
 * \param u The Update to update this object with.
 */
void NormalDistribution::update(const Update& u)
{
     if (u.getType() == Update::eModify && u.getReference().name() == "sigmaMeters") {
	  mSigma = u.getObject()->getDouble();
	  mK1 = 1 / (mSigma * sqrt(2*kPi)); 
	  mK2 = -0.5 / (mSigma * mSigma);
     }
     else {
	  Error e;
	  e << "No updatable attribute '" << u.getReference().name() << "' in '" << ref() << "'";
	  throw e;
     }
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void NormalDistribution::extract(Buffer &b) const
{
     DataObject& me = *b.map(ref());
     me.getChild("sigmaMeters")->setDouble(mSigma);
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void NormalDistribution::reset(const DataObject& d)
{
     mSigma = d.getChild("sigmaMeters")->getDouble();
     mK1 = 1 / (mSigma * sqrt(2*kPi)); 
     mK2 = -0.5 / (mSigma * mSigma);
}
