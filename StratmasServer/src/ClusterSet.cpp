/*
=================================================================================
        Methods of the ClusterSet class.
                
        Performs clustering on a multivariate dataset.
        Each cluster is an ellipse in general position.

        First written October 1997, by Loren Cobb.
        This code was not funded from any external source.

        Copyright  1997-2001 by Loren Cobb. All rights reserved.

        Warning: Material protected by Lagen Om Forsvarsuppfinnigar 1971:1078.
=================================================================================
*/
// System
#include <cmath>

// Own
#include "ClusterSet.h"
#include "Ellipse.h"


static const int minClusterSize = 6;


/**
 * Constructor.
 *
 * \param n The maximal number of clusters.
 */
ClusterSet::ClusterSet(int n) : mNumClusters(0), mMaxClusters(n), mCluster(0)
{
     mMaxClusters = n;
     if (n > 0) {
          mCluster = new Ellipse[mMaxClusters];
     }
     mNumClusters = 0;
}


/**
 * Destructor.
 */
ClusterSet::~ClusterSet()
{
     if (mMaxClusters > 0 && mCluster) {
          delete [] mCluster;
     }
}


/**
 *   \brief Cluster this weighted data set, using the k-means
 *   algorithm.
 *
 *   \param n       Length of data vectors
 *   \param index   Vector of cluster ids
 *   \param xp      Vector of x-coords   
 *   \param yp      Vector of y-coords   
 *   \param wp      Vector of weights    
 *   \return        The number of clusters found.
 */
int ClusterSet::FitToData(
     int        n,                        //        length of data vectors
     int *        index,                        //        vector of cluster ids
     const double* const        xp,        //        vector of x-coords 
     const double* const        yp,        //        vector of y-coords
     double *        wp )                    //        vector of weights
{
     double maxAxis, A, B, C, mx, my, x, y;
     double density, maximum;
     int     i, j, k, longest, newest, likeliest;
     bool    okay, okay_newest, okay_longest, bail;
        
     if ( n < minClusterSize || mMaxClusters < 1 ) {
          mNumClusters = 0;
          return mNumClusters;
     }

     /*
       Initialize the clustering algorithm by
       creating one cluster consisting of all the data:
     */
     for ( i = 0; i < n; i++ ) {
          index[i] = 0;        //        all cases initially belong to cluster 0
     }

     okay = mCluster[0].SummarizeData( n, 0, index, xp, yp, wp );
     if ( okay ) {
          mNumClusters = 1;
     }
     else {        //        problem summarizing the data
          mNumClusters = 0;
          return mNumClusters;
     }
     /*
       Loop:        Find a new cluster within the current set:
     */
     while ( mNumClusters < mMaxClusters ) {
          /*
            Step 1:        Identify the cluster with the longest major axis:
          */
          maxAxis = 0.0;
          longest = 0;
          for ( k = 0; k < mNumClusters; k++ ) {
               if ( maxAxis < mCluster[k].dMajor ) {
                    maxAxis = mCluster[k].dMajor;
                    longest = k;
               }
          }
                
          //        Sanity check: stop if too few cases in the longest cluster:
          if ( mCluster[longest].nCases <= 2*minClusterSize ) {
               break;
          }
                
          /*
            Step 2:        Divide this cluster along its major axis, and then
            reassign each of its cases to the two new clusters:
          */
          newest = mNumClusters;                //        the index of the newest cluster
          A = mCluster[longest].beta;
          B = sqrt( 1.0 - A*A );
          mx = mCluster[longest].mx;
          my = mCluster[longest].my;
          for ( i = 0; i < n; i++ ) {
               if ( index[i] == longest ) {
                    C = A*(xp[i] - mx) + B*(yp[i] - my);
                    if ( C > 0.0 ) {
                         index[i] = newest;        //        reassign this case
                    }
               }
          }
                
          /*
            Step 3:        Calculate an ellipse for each of the new clusters:
          */
          okay_longest = mCluster[longest].SummarizeData( n, longest, index, xp, yp, wp );
          okay_newest  = mCluster[ newest].SummarizeData( n,  newest, index, xp, yp, wp );
                
          if ( okay_longest && okay_newest ) {
               mNumClusters++;        //        increment the number of clusters
          }
          else {
               break;                        //        cannot add another cluster
          }
                
          /*
            Step 4:        Reassign EVERY case to its most likely cluster:
          */
          for ( i = 0; i < n; i++ ) {
               x = xp[i];
               y = yp[i];
               likeliest = index[i];
               maximum = mCluster[likeliest].ProbDensity( x, y );
               for ( k = 0; k < mNumClusters; k++ ) {
                    if ( k != index[i] ) {
                         density = mCluster[k].ProbDensity( x, y );
                         if ( maximum < density ) {
                              maximum = density;
                              likeliest = k;
                         }
                    }
               }
               if ( index[i] != likeliest ) {
                    //        Move this case to the likeliest cluster:
                    index[i] = likeliest;
               }
          }
                
          /*
            Step 5:        Recalculate the ellipse for each cluster.
            Remove all clusters for degenerate ellipses. If
            any ellipse is degenerate, then stop clustering.
          */
          bail = false;
          k = 0;
          while ( k < mNumClusters )        {
               okay = mCluster[k].SummarizeData( n, k, index, xp, yp, wp );
               if ( ! okay ) {
                    //        Move all subsequent clusters down one position,
                    //        and reduce the number of clusters by one:
                    mNumClusters -= 1;
                    for ( j = k; j < mNumClusters; j++ ) {
                         mCluster[j] = mCluster[j+1];
                    }
                    bail = true;
               }
               ++k;
          }
          if ( bail ) {
               break;
          }
                
     }        //        End of primary loop.
                
     return mNumClusters;
}


/**
 * \brief Undocumented by Loren Cobb.
 *
 * \param k Undocumented by Loren Cobb.
 */
void ClusterSet::MakeSigmaBoxes( double k ) {
     for ( int j = 0; j < mNumClusters; j++ ) {
          mCluster[j].GetSigmaBox( k );
     }
}


#if 0
/*
  =========================================
  Report
  -------------------------------------------------
*/
void
ClusterSet::Report( ofstream * info )
{
     if ( info == NULL )
          return;
        
     *info << mNumClusters << " have been found.\n";
        
     for ( int k = 0; k < mNumClusters; k++ )
     {
          *info << "\nCluster #" << k << ":\n";
          mCluster[k].Report( info );
     }
}
#endif
