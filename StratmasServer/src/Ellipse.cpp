/*
  =================================================================================
  Methods of the Ellipse class.
                
  Calculates the concentration ellipse for a series of
  weighted observations of two variables.

  First written October 1997, by Loren Cobb.
  This code was not funded from any external source.

  Copyright ©1997 Loren Cobb. All rights reserved.

  Warning: Material protected by Lagen Om Forsvarsuppfinnigar 1971:1078.
  =================================================================================
*/

// Own
#include "debugheader.h"
#include "Ellipse.h"
#include "GoodStuff.h"


using namespace std;


/*
  =========================================
  Ellipse constructor
  -------------------------------------------------
*/
Ellipse::Ellipse()
{
     nCases = 0;
     mx = my = 0.0;
     Vx = Vy = 0.0;
     Cxy = Disc = 0.0;
     Weight = 0.0;
        
     Disc = 0.0;
     InvSqrtDisc = HUGE_VAL;

     dMajor = dMinor = 0.0;
     angle = alpha = beta = 0.0;
}


/**
 *   \brief Given input summary statistics, calculates: Weight, mx, my, Vx,
 *   Vy, Cxy, Disc
 *
 *   \param inW    Sum of weights                 
 *   \param inX    Sum of x-coords                 
 *   \param inY    Sum of y-coords                 
 *   \param inX2   Sum of squared x-coords         
 *   \param inY2   Sum of squared y-coords         
 *   \param inXY   Sum of x-y coord products
 */
bool
Ellipse::SetStats(
     double        inW,                        //        sum of weights
     double        inX,                        //        sum of x-coords
     double        inY,                        //        sum of y-coords
     double        inX2,                        //        sum of squared x-coords
     double        inY2,                        //        sum of squared y-coords
     double        inXY )                        //        sum of x-y coord products
{
//P Per's comment: Could do this in order to reduce sensitivity to
//roundoff errors. At least useful for testing purposes.
//      inW = static_cast<float>(inW);
//      inX = static_cast<float>(inX);
//      inY = static_cast<float>(inY);
//      inX2 = static_cast<float>(inX2);
//      inY2 = static_cast<float>(inY2);
//      inXY = static_cast<float>(inXY);

     //        Default values:

     mx = my = 0.0;
     dMajor = dMinor = 0.0;
     angle = alpha = beta = 0.0;

     Vx = Vy = 0.0;
     Cxy = Disc = 0.0;
     InvSqrtDisc = HUGE_VAL;
     Weight = 0.0;
        
     if ( inW <= 0.0 ) {
          return false;
     }

     Weight = inW;                                //        Total weight of this ellipse

     mx   = inX / inW;                        //        Weighted mean of y
     my   = inY / inW;                        //        Weighted mean of x
     Vx   = inX2/inW - mx*mx;        //        Weighted variance of x
     Vy   = inY2/inW - my*my;        //        Weighted variance of y
        
     Cxy  = inXY/inW - mx*my;        //        Weighted covariance of x and y
     Disc = Vx*Vy - Cxy*Cxy;                //        Discriminant of the covariance matrix

     if ( Disc <= 0.0 )
     {
          Disc = 0.0;
          return false;
     }
        
     InvSqrtDisc = 1.0 / sqrt(Disc);        //        Inverse square root of discriminant
        
     if ( isnan(InvSqrtDisc) || InvSqrtDisc >= HUGE_VAL ) {
          return false;
     }
                
     if ( Vx < 0.0 ) {
          Vx = 0.0;
          return false;
     }
        
     if ( Vy < 0.0 ) {
          Vy = 0.0;
          return false;
     }
        
     if ( Vx == 0.0 || Vy == 0.0 ) {
          return false;
     }
        
     FindEllipse();
        
     return true;
}


/**
 *   \brief This function receives a weighted set of n points, and
 *   fits a one-sigma ellipse of inertia to the data.
 *        
 *   If the data are bivariate normal, then the ellipse of inertia
 *   will contain 67% of the data points.
 */
bool Ellipse::SummarizeData(
     int        n,
     int        which,
     int        *cluster,
     const double * const xp,
     const double * const yp,
     double        *wp )
{
     double                sx  = 0.0, sy  = 0.0,
          sx2 = 0.0, sy2 = 0.0,
          sxy = 0.0, sw  = 0.0,
          w, x, y;
     int                        k, hasWeights;
     bool                okay;

     //        Default values:

     mx = my = 0.0;
     dMajor = dMinor = 0.0;
     angle = alpha = beta = 0.0;

     Vx = Vy = 0.0;
     Cxy = Disc = 0.0;
     InvSqrtDisc = HUGE_VAL;
     Weight = 0.0;
        
     nCases = 0;
        
     if ( xp == NULL || yp == NULL )
     {
          //        Invalid data pointer!
          return false;
     }
        
     if ( which < 0 || which >= n )
     {
          //        Invalid cluster index!
          return false;
     }
     else if ( cluster == NULL )
     {
          //        Invalid cluster index vector!
          return false;
     }
        
     hasWeights = ( wp != NULL );

     //        Calculate the first and second moments:
        
     for ( k = 0; k < n; k++ )
     {
          if ( which == cluster[k] )
          {
               //        This case is a member of the specified cluster:
                
               ++nCases;
               x = xp[k];                //        x-coordinate
               y = yp[k];                //        y-coordinate
               w = ( hasWeights ? wp[k] : 1.0 );        //        Weight
          }
          else        //        NOT a member of the specified cluster,
          {
               continue;        //        so skip this case
          }
                
          if ( hasWeights )
          {
               sw  += w;                //        sum of weights
               sx  += w*x;                //        weighted sum of x
               sy  += w*y;                //        weighted sum of y
               sx2 += w*x*x;
               sy2 += w*y*y;
               sxy += w*x*y;
          }
          else        
          {
               sw  += 1.0;                //        sum of weights
               sx  += x;                //        sum of x
               sy  += y;                //        sum of y
               sx2 += x*x;
               sy2 += y*y;
               sxy += x*y;
          }
     }
        
     if ( sw <= 0.0 || nCases == 0 )
     {
          return false;                //        Zero weight for these data!
     }

     mx  = sx/sw;                        //        Weighted mean of x
     my  = sy/sw;                        //        Weighted mean of y
     Weight = sw;                        //        Total weight of this ellipse
        
     if ( nCases == 1 )
     {
          return false;                //        Only one case, so stop now!
     }
        
     okay = SetStats( sw, sx, sy, sx2, sy2, sxy );

     return okay;
}


/**
 *   \brief Given summary statistics, calculate the parameters of the
 *   best-fitting ellipse.
 *
 *   Calculates: dMajor, dMinor,angle, alpha, beta
 */
void
Ellipse::FindEllipse()
{
     double                L1, L2, a, b, c;
        
     //  Compute eigenvalues:
        
     b = Vx + Vy;
     c = sqrt( b*b - 4.0*Disc );
     L1 = 0.5*( b + c );
     L2 = 0.5*( b - c );
     if ( L1 < L2 )
     {
          a = L1;
          L1 = L2;
          L2 = a;
     }
     if ( L2 <= 0.0 )
     {
          L2 = 0.0;                //        Constrain to non-negative
     }
     dMajor = sqrt( L1 );        //        Half the length of the major axis
     dMinor = sqrt( L2 );        //        Half the length of the minor axis
        
     //        Compute the first component of the largest eigenvector:
        
     beta = Cxy / sqrt( square(Vx-L1) + Cxy*Cxy );
        
     //        Compute the angle between the x-axis
     //        and the major ellipse axis in radians:
        
//        if ( Vx == Vy )
//                angle = 0.25 * kPi;
//        else
//                angle = 0.5 * atan2( 2.0*Cxy, Vy-Vx );

     if ( Cxy < 0.0 )
          angle = atan2( -Vy, Vx );
     else
          angle = atan2( Vy, Vx );
     if ( angle < 0.0 )
          angle += kPi;
        
     //        Compute the angle between the major axis 
     //        and the top-right corner of the enclosing box.
        
     alpha = atan2( dMinor, dMajor );
     if ( alpha < 0.0 )
          alpha += kPi;
}


/**
 *   \brief Returns the Mahalanobis distance of a given point to the
 *   center of the ellipse.
 *        
 *   Note: units are in squared values!
 */
double Ellipse::Distance2( double x, double y )
{
     double        result;
        
     x -= mx;
     y -= my;
        
     result = ( Vy*x*x - 2.0*Cxy*x*y + Vx*y*y ) / Disc;
        
     return result;
}


/**
 *   \brief Returns the vertices of a rectangle in general position
 *   that contains the k-sigma ellipse of inertia for the data.
 */
void Ellipse::GetSigmaBox( double k ) {
     if ( vx == NULL || vy == NULL ) {
          return;
     }
        
     double length = k * dMajor;                //        k-sigma ellipse

     vx[0] = mx + length * cos( angle - alpha );
     vy[0] = my + length * sin( angle - alpha );
        
     vx[1] = mx + length * cos( angle + alpha );
     vy[1] = my + length * sin( angle + alpha );
        
     vx[2] = mx + length * cos( angle + kPi - alpha );
     vy[2] = my + length * sin( angle + kPi - alpha );
        
     vx[3] = mx + length * cos( angle + kPi + alpha );
     vy[3] = my + length * sin( angle + kPi + alpha );
}
