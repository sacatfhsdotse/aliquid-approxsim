#ifndef STRATMAS_GRAPH_H
#define STRATMAS_GRAPH_H


#include <string>
#include <LatLng.h>

/**
 * \brief This class represents a Node.
 *
 * \author   Daniil Pintjuk
 * \date     $Date: 2014/04/25 20:14:00 $
 */
class Node
{
protected:
	LatLng pos;
public:
	Node(LatLng p):pos(p){

	}
	Node(){

	}
};

/**
 * \brief This class represents a Edg.
 *
 * \author    Daniil Pintjuk
 * \date     $Date: 2014/04/25 20:14:00 $
 */
class Edge
{
protected:
	int origin;
	int target;
	bool conected;
	double travalspeed;
public:
	Edge(int o, int t, bool con, double tsped):
		origin(o), target(t), conected(con), travalspeed(tsped){

	}
	Edge(){

	}
};



/**
 * \brief This class represents a Graph.
 *
 * \author   Johannesd Olegård, Daniil Pintjuk
 * \date     $Date: 2014/04/25 20:14:00 $
 */
class Graph
{
protected:
	int numNodes;
	Node* nodes;
	int numEdges;
	Edge* edges;
public:
	Graph(
		int NumNodes,
		Node* Nodes,
		int NumEdges,
		Edge* Edges):
		numNodes(NumNodes),
		nodes(Nodes),
		numEdges(NumEdges),
		edges(Edges){}
};




#endif   // STRATMAS_GRAPH_H

// vim: ts=4 sw=4:
