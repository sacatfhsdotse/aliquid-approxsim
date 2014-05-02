#ifndef STRATMAS_GRAPH_H
#define STRATMAS_GRAPH_H


#include <string>
#include <list>
#include <LatLng.h>
#include <iostream>

using namespace std;

struct PathData {
	double travelSpeed;
};

struct EffectData {
};

/**
 * \brief This class represents a Node in a graph.
 *
 * \author   Daniil Pintjuk
 * \date     $Date: 2014/04/25 20:14:00 $
 */
template<class T>
struct Node
{
	LatLng pos;
	T content;

	Node(LatLng p, T content): pos(p), content(content){}
	Node();

	void print(std::ostream& o);
};

/**
 * \brief This class represents an Edge in a graph.
 *
 * \author    Daniil Pintjuk
 * \date     $Date: 2014/04/25 20:14:00 $
 */
template<class T>
struct Edge
{
	Node<T>* origin;
	Node<T>* target;
	bool isConnected;
	T content;

	Edge(Node<T>* o, Node<T>* t, bool con, T content):
		origin(o), target(t), isConnected(con), content(content){}
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
	std::list<Edge<PathData>*> path;
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
template<class T>
class Graph
{
protected:
	int numNodes;
	Node<T>* nodes;
	int numEdges;
	Edge<T>* edges;
public:
	Graph(int numNodes, Node<T>* nodes, int numEdges, Edge<T>* edges):
		numNodes(numNodes), nodes(nodes), numEdges(numEdges), edges(edges){}

	~Graph() {delete edges; delete nodes;}

	void print(std::ostream& o, std::string indent="");

	NavigationPlan getPath(LatLng start, LatLng end);
};

#endif   // STRATMAS_GRAPH_H

// vim: ts=4 sw=4 expandtab:
