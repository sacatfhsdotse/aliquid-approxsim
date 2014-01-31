#ifndef STRATMAS_REGIONFILE_H
#define STRATMAS_REGIONFILE_H

// System
#include <ostream>
#include <map>
#include <string>
#include <vector>


class RegionFile {
private:
     std::map<std::string, std::vector<double>*> mRegionToFactionFraction;
     std::map<std::string, int> mFactionToIndex;

     std::string trim(std::string& s, const std::string& drop = " ") {
	  std::string r = s.erase(s.find_last_not_of(drop) + 1);
	  return r.erase(0, r.find_first_not_of(drop));
     }

public:
     RegionFile(const std::string &fileName) {
	  char c;
	  std::string line;
	  std::string region;
	  std::string name;
	  std::ifstream ifs(fileName.c_str());

	  std::map<std::string, int> factions;

	  // Read header line
	  getline(ifs, line);

	  std::istringstream is(line);
	  do {
	       is >> c;
	  } while (c != ',' && !is.eof());

	  int count = 0;
	  while (!is.eof()) {
	       getline(is, name, ',');
	       mFactionToIndex[trim(name)] = count++;
	  }

	  while (!ifs.eof()) {
	       getline(ifs, line);
	       std::istringstream is(line);
	       getline(is, region, ',');
	       region = trim(region);
	       
	       std::vector<double> *vec = new std::vector<double>;
	       while(!is.eof()) {
		    double frac;
		    is >> frac;
		    vec->push_back(frac);
		    do {
			 is >> c;
		    } while (c != ',' && !is.eof());
	       }
	       mRegionToFactionFraction[region] = vec;
	  }
     }

     ~RegionFile() {
	  std::map<std::string, std::vector<double>*>::const_iterator it;
	  for (it = mRegionToFactionFraction.begin(); it != mRegionToFactionFraction.end(); it++) {
	       delete it->second;
	  }
     }

     double fractionForRegionAndFaction(const std::string& regionName, const std::string factionName) {
	  double ret = 0;
	  std::map<std::string, std::vector<double>*>::iterator it = mRegionToFactionFraction.find(regionName);
	  if (it != mRegionToFactionFraction.end()) {
	       std::map<std::string, int>::iterator it2 = mFactionToIndex.find(factionName);
	       if (it2 != mFactionToIndex.end()) {
		    ret = (*it->second)[it2->second];
	       }
	  }
	  return ret;
     }

     friend std::ostream &operator << (std::ostream& o, const RegionFile &rf) {
	  for (std::map<std::string, int>::const_iterator it = rf.mFactionToIndex.begin();
	       it != rf.mFactionToIndex.end(); ++it) {
	       o << it->first << " - " << it->second << std::endl;
	  }
	  std::map<std::string, std::vector<double>*>::const_iterator it;
	  for (it = rf.mRegionToFactionFraction.begin(); it != rf.mRegionToFactionFraction.end(); it++) {
	       o << "'" << it->first << "'";
	       for (std::vector<double>::iterator vit = it->second->begin(); vit != it->second->end(); vit++) {
		    o << " " << *vit;
	       }
	       o << std::endl;
	  }
	  return o;
     }
};

#endif   // STRATMAS_REGIONFILE_H
