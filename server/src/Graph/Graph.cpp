#include "Graph.h"
#include <queue>
#include <vector>
#include <utility>
#include <tuple>
#include <map>
using std::vector;
using std::string;

template<class T>
void Graph<T>::print(std::ostream& o, string indent) {
    o << endl;
    o << indent << "graph: {" << endl << indent << "  nodes: [" << endl;
    for (int i = 0; i < numNodes; i++) {
        o << indent << "    ";
        nodes[i].print(o);
        o << endl;
    }
    o << indent << "  ]," << endl;
    o << indent << "  edges: [" << endl;
    for(int i = 0; i < numEdges; i++){
        o << indent << "    ";
        edges[i].print(o);
        o << endl;
    }
    o << indent << "  ]" << endl << indent << "}";
}
template void Graph<PathData>::print(std::ostream& o, string indent);
template void Graph<EffectData>::print(std::ostream& o, string indent);

template<class T>
Graph<T>* Graph<T>::getGraph(string identifier) {
    return Graph<T>::getSavedGraphs()[identifier];
}
template<class T>
static double weight(const Node<T>& a, const Node<T>& b, double speed) {
    return a.pos.distanceTo(b.pos) / speed;
}

NavigationPlan pathfind(LatLng start, LatLng end) {
    typedef ::Graph<PathData> Graph;
    typedef ::Node<PathData> Node;
    typedef ::Edge<PathData> Edge;

    const Graph& gr = *Graph::getGraph("");
    Node* startNode = new Node(start, PathData());
    Node* endNode = new Node(end, PathData());

    // Generate an adjacency list for the graph, including the pseudo-edges
    // from start to everywhere, and from everywhere to end.
    typedef std::tuple<Node*, double, Edge*> Tup;
    std::map<Node*, std::vector<Tup>> adj;
    for (int i = 0; i < gr.numEdges; i++) {
        Edge& e = gr.edges[i];
        if (!e.isConnected) {
            continue;
        }
        adj[e.origin].emplace_back(e.target, weight(*e.origin, *e.target, e.content.travelSpeed), &e);
    }
    for (int i = 0; i < gr.numNodes; i++) {
        Node* node = &gr.nodes[i];
        adj[startNode].emplace_back(node, weight(*startNode, *node, 1), nullptr);
        adj[node].emplace_back(endNode, weight(*node, *endNode, 1), nullptr);
    }
    adj[startNode].emplace_back(endNode, weight(*startNode, *endNode, 1), nullptr);

    // Run Dijkstra and keep a path backwards.
    std::priority_queue<std::tuple<double, Node*, pair<Node*, Edge*> >> q;
    q.emplace(0, startNode, make_pair(nullptr, nullptr));
    std::map<Node*, pair<Node*, Edge*> > backwards;

    while (!q.empty()) {
        double negTime;
        Node* cur;
        pair<Node*, Edge*> prev;
        std::tie(negTime, cur, prev) = q.top();
        q.pop();
        if (backwards.count(cur)) {
            continue;
        }
        backwards[cur] = prev;
        for (auto ed : adj[cur]) {
            Node* to;
            double weight;
            Edge* e;
            std::tie(to, weight, e) = ed;
            if (backwards.count(to)) {
                continue;
            }
            q.emplace(negTime - weight, to, make_pair(cur, e));
        }
    }

    NavigationPlan result;
    Node* node = endNode;
    while (backwards[node].first) {
        auto prev = backwards[node];
        node = prev.first;
        if (prev.second) {
            result.path.push_front(prev.second);
        }
    }

    delete startNode;
    delete endNode;
    return result;
}

template<class T>
Node<T>::Node() {

}
template Node<PathData>::Node();
template Node<EffectData>::Node();

template<class T>
void Node<T>::print(std::ostream& o) {
    o << "Node: {" << pos.lat() << ", " << pos.lng() << "}";
}
template void Node<PathData>::print(std::ostream& o);
template void Node<EffectData>::print(std::ostream& o);

template<class T>
Edge<T>::Edge() {

}
template Edge<PathData>::Edge();
template Edge<EffectData>::Edge();

template<class T>
void Edge<T>::print(std::ostream& o) {
    o << "Edge: { o: " << origin
      << ", t: " << target
      << ", con:" << isConnected << "}";
}
template void Edge<PathData>::print(std::ostream& o);
template void Edge<EffectData>::print(std::ostream& o);
// vim: ts=4 sw=4 expandtab:
