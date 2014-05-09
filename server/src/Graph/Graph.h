#ifndef APPROXSIM_GRAPH_H
#define APPROXSIM_GRAPH_H


#include <map>
#include <string>
#include <memory>
#include <vector>
#include <list>
#include <LatLng.h>
#include <iostream>

using namespace std;

struct PathData {
    double travelSpeed;
};

struct Effect {
    double radius;
    bool continuous;
};

struct EffectData {
    double power;
    Effect connected, disconnected;
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
    std::shared_ptr<Node<T>> origin;
    std::shared_ptr<Node<T>> target;
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
    static std::map<std::string, Graph<T>*>& getSavedGraphs() {
        static std::map<string, Graph<T>*> savedGraphs;
        return savedGraphs;
    }
    std::vector<std::shared_ptr<Node<T>>> nodes;
    std::vector<std::shared_ptr<Edge<T>>> edges;
public:
    Graph(std::string identifier,
        std::vector<std::shared_ptr<Node<T>>> nodes,
        std::vector<std::shared_ptr<Edge<T>>> edges)
        : nodes(nodes), edges(edges)
    {
        getSavedGraphs()[identifier] = this;
    }

    void print(std::ostream& o, std::string indent="");

    static Graph<T>* getGraph(std::string identifier);

    friend NavigationPlan pathfind(LatLng start, LatLng end);
};

NavigationPlan pathfind(LatLng start, LatLng end);

#endif   // APPROXSIM_GRAPH_H

// vim: ts=4 sw=4 expandtab:
