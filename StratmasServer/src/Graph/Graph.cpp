#include "Graph.h"


void Graph::print(std::ostream& o){
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

NavigationPlan Graph::getPath(LatLng start, LatLng end){
	//TODO: 
	// Simon titta hit!
	// vett inte om du föredrar arrays eller std::vector
	// vi kan annars ändra typen för nodes och edges
	NavigationPlan result;
	return result;
}

Node::Node(){

}

void Node::print(std::ostream& o){
	o << "Node: {" << pos.lat() <<", " << pos.lng() << "}";
}

Edge::Edge(){

}

void Edge::print(std::ostream& o){
	o<<"Edge: { o: " << origin 
	 << ", t: " << target  
	 << ", con:" << conected
	 << ", sp:" << travalspeed <<"}";
}