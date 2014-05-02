#ifndef STRATMAS_GRAPH_H
#define STRATMAS_GRAPH_H


#include <string>
#include <list>
#include <LatLng.h>
#include <iostream>

using namespace std;

/**
 * \brief This class represents a Node in a graph.
 *
 * \author   Daniil Pintjuk
 * \date     $Date: 2014/04/25 20:14:00 $
 */
class Node
{
protected:
	LatLng pos;
public:
	Node(LatLng p) : pos(p) {}
	Node();
	void print(std::ostream& o);
};

/**
 * \brief This class represents an Edge in a graph.
 *
 * \author    Daniil Pintjuk
 * \date     $Date: 2014/04/25 20:14:00 $
 */
class Edge
{
protected:
	int origin;
	int target;
	bool connected;
	double travelspeed;
public:
	Edge(int o, int t, bool con, double tsped):
		origin(o), target(t), connected(con), travelspeed(tsped) {}
	Edge();
	void print(std::ostream& o);
};

/**
 * \brief This class represents a path through a graph.
 *
 * \author   Daniil Pintjuk
 * \date     $Date: 
 */
struct NavigationPlan {
	/// no  chour that this should be a list and not an array or vector TODO: decide
	std::list<Edge*> path;
	/// point on the map, wher the path starts, tupicaly inbetween two nodes on an edge.
	LatLng on;
	/// point on the map, where the path ends, tupicaly inbetween two nodes on an edge.
	LatLng off;

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
	Graph(int NumNodes, Node* Nodes, int NumEdges, Edge* Edges) :
		numNodes(NumNodes), nodes(Nodes), numEdges(NumEdges), edges(Edges) {}
	void print(std::ostream& o);
	NavigationPlan getPath(LatLng start, LatLng end);
};




#endif   // STRATMAS_GRAPH_H

// vim: ts=4 sw=4 expandtab:
