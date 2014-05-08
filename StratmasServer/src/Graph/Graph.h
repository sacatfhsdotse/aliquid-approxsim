#ifndef APPROXSIM_GRAPH_H
#define APPROXSIM_GRAPH_H


#include <map>
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
    std::list<Edge<PathData>*> path;
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
private:
    static std::map<std::string, Graph<T>*>& getSavedGraphs();
    int numNodes;
    Node<T>* nodes;
    int numEdges;
    Edge<T>* edges;
public:
    Graph(std::string identifier, int numNodes, Node<T>* nodes, int numEdges, Edge<T>* edges):
        numNodes(numNodes), nodes(nodes), numEdges(numEdges), edges(edges)
    {
        getSavedGraphs()[identifier] = this;
    }

    ~Graph() {delete edges; delete nodes;}

    void print(std::ostream& o, std::string indent="");

    static Graph<T>* getGraph(std::string identifier);

    friend NavigationPlan pathfind(LatLng start, LatLng end);
};

NavigationPlan pathfind(LatLng start, LatLng end);

#endif   // APPROXSIM_GRAPH_H

// vim: ts=4 sw=4 expandtab:
