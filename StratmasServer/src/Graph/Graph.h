#ifndef STRATMAS_GRAPH_H
#define STRATMAS_GRAPH_H


#include <string>
#include <LatLng.h>
#include <iostream>
/**
 * \brief This class represents a Node.
 *
 * \author   Daniil Pintjuk
 * \date     $Date: 2014/04/25 20:14:00 $
 */

 using namespace std;
class Node
{
protected:
	LatLng pos;
public:
	Node(LatLng p):pos(p){

	}
	Node(){

	}
	void print(std::ostream& o){
		o<<"Node: {" << pos.lat() <<", " << pos.lng() << "}";
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
	void print(std::ostream& o){
		o<<"Edge: { o: " << origin 
		 << ", t: " << target  
		 << ", con:" << conected
		 << ", sp:" << travalspeed <<"}";
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

	void print(std::ostream& o){
		o << endl;
		o << "graph: {" << endl <<  "  nodes: [" <<endl;
		for(int i= 0; i <numNodes; i++){
			o << "    ";
			nodes[i].print(o);
			o << endl;
		}
		o << "  ]," << endl;
		o << "  edges: ["<<endl;
		for(int i=0; i <numEdges; i++){
			o << "    ";
			edges[i].print(o);
			o << endl;
		}
		o << "  ]"<< endl <<"}";
	}
};




#endif   // STRATMAS_GRAPH_H

// vim: ts=4 sw=4:
