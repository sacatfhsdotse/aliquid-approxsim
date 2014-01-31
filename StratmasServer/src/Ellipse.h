/*
 *		DEllipse.h - A general purpose ellipse class.
 */
// For inlined ProbDensity - calls exp()

// System
#include <cmath>

/**
 * \brief Calculates the concentration ellipse for a series of
 * weighted observations of two variables.
 *
 * This class is almost identical to its counterpart in older
 * versions of Stratmas. Some name changes have been made in order
 * to match naming conventions in other source code files.
 *
 * \author    Loren Cobb - Modified by Per Alexius.
 * \date     $Date: 2005/06/13 11:19:05 $
 */
class Ellipse {
public:
     int  nCases;	///<	Number of observations for this ellipse

     double mx,         ///<	x-coordinate of center of the ellipse
	  my,   	///<	y-coordinate of center of the ellipse
	  dMajor,	///<	Half the length of the major axis
	  dMinor,	///<	Half the length of the minor axis
	  angle,	///<	Angle of major axis to the origin
	  alpha,	///<	Arctan of d2/d1 ( = ¹/4 if circle)
	  beta;		///<	First component of first eigenvector
     
     double Vx,         ///<	Variance along x coordinate.
	  Vy,	        ///<	Variance along y coordinate.
	  Cxy,		///<	Covariance 
	  Disc,		///<	Discriminant
	  InvSqrtDisc,	///<	Inverse of the square root of the discriminant
	  Weight;	///<	Total weight of the ellipse
	
     double vx[4],	///<	x-coordinates of the vertex rectangle
	  vy[4];	///<	y-coordinates of the vertex rectangle
	
     Ellipse(); 	///<	Constructor
				
     bool		SetStats( double inW, double inX, double inY, double inX2, double inY2, double inXY );
     bool		SummarizeData(int n, int which, int* cluster, const double* const xp, const double* const yp, double *wp );
     void		FindEllipse();
     void		GetSigmaBox( double k );
     double		Distance2( double x, double y );
     inline double	ProbDensity( double x, double y );
     inline double	square( double x ) { return x*x; }   ///< Calculates the square of x
};
	
/**
 *   \brief Returns the value of the probability density function of
 *   the multivariate normal at the point (x,y).
 *
 *   \param x   x-coordinate
 *   \param y   y-coordinate
 *   \return    The value of the probability density function of
 *              the multivariate normal at the point (x,y).
 */
inline double Ellipse::ProbDensity( double x, double y ) {
     if ( Disc <= 0.0 ) {
	  return 0.0;
     }
     x -= mx;
     y -= my;
     return InvSqrtDisc * exp( -0.5 * ( Vy*x*x - 2.0*Cxy*x*y + Vx*y*y ) / Disc );
}



