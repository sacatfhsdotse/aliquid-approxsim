/*
 *  Cluster.h - Identifies clusters in the data. Each cluster is an ellipse in
 *  general position.
 */
#ifndef STRATMAS_CLUSTERSET
#define STRATMAS_CLUSTERSET


// Forward Declarations
class Ellipse;

/**
 * \brief Performs clustering on a multivariate dataset. Each
 * cluster is an ellipse in general position.
 *
 * This class is almost identical to its counterpart in older
 * versions of Stratmas. Some name changes have been made in order
 * to match naming conventions in other source code files.
 *
 * \author   Loren Cobb - Modified by Per Alexius
 * \date     $Date: 2005/06/03 17:14:03 $
 */
class ClusterSet {
public:
     int       mNumClusters;   ///< Number of clusters found
     int       mMaxClusters;   ///< Maximum number of clusters to look for        
     Ellipse   *mCluster;      ///< The array of cluster ellipses
        
     ClusterSet(int max);
     ~ClusterSet();
        
     int    FitToData(int n, int *index, const double* const xp, const double* const yp, double *wp);
     void   MakeSigmaBoxes(double k);
};

#endif   // STRATMAS_CLUSTERSET
