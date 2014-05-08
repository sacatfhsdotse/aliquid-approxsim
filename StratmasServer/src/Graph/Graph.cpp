#include "Graph.h"

template<class T>
void Graph<T>::print(std::ostream& o, std::string indent){
	o << endl;
	o << indent << "graph: {" << endl << indent <<  "  nodes: [" <<endl;
	for(int i= 0; i <numNodes; i++){
		o << indent << "    ";
		nodes[i].print(o);
		o << endl;
	}
	o << indent << "  ]," << endl;
	o << indent << "  edges: ["<<endl;
	for(int i=0; i <numEdges; i++){
		o << indent << "    ";
		edges[i].print(o);
		o << endl;
	}
	o << indent << "  ]"<< endl << indent << "}";
}
template void Graph<PathData>::print(std::ostream& o, std::string indent);

template<>
NavigationPlan Graph<PathData>::getPath(LatLng start, LatLng end){
	//TODO: 
	// Simon titta hit!
	// vett inte om du föredrar arrays eller std::vector
	// vi kan annars ändra typen för nodes och edges
	NavigationPlan result;
	return result;
}

template<class T>
Node<T>::Node(){

}
template Node<PathData>::Node();

template<class T>
void Node<T>::print(std::ostream& o){
	o << "Node: {" << pos.lat() << ", " << pos.lng() << "}";
}
template void Node<PathData>::print(std::ostream& o);

template<class T>
Edge<T>::Edge() {

}
template Edge<PathData>::Edge();

template<class T>
void Edge<T>::print(std::ostream& o){
	o << "Edge: { o: " << origin 
	  << ", t: " << target  
	  << ", con:" << isConnected << "}";
}
template void Edge<PathData>::print(std::ostream& o);
// vim: ts=4 sw=4 expandtab:
