// System
#include <algorithm>
#include <fstream>
#include <iostream>
#include <list>

// Own
#include "DataObject.h"
#include "debugheader.h"
#include "Error.h"
#include "IOHandler.h"
#include "LogStream.h"
#include "MemEntityResolver.h"
#include "ParserErrorReporter.h"
#include "PropertyHandler.h"
#include "PVRegion.h"
#include "StratmasMessage.h"
#include "StrX.h"
#include "Subscription.h"
#include "Update.h"
#include "XMLHandler.h"
#include "XMLHelper.h"

// Xerces
#include <xercesc/dom/DOMElement.hpp>
#include <xercesc/dom/DOMImplementation.hpp>
#include <xercesc/framework/MemBufInputSource.hpp>
#include <xercesc/parsers/XercesDOMParser.hpp>
#include <xercesc/util/OutOfMemoryException.hpp>

// Temporary
#include "stopwatch.h"

using namespace std;

// Static Definitions
XMLTranscoder* TranscoderWrapper::sTranscoder(0);
unsigned int TranscoderWrapper::sMaxCharSize(0);


/**
 * \brief Gets data from the currently held subscriptions and puts it
 * into the provided UpdateMessage.
 *
 * \param um The UpdateMessage into which to put the subscriptions.
 */
void XMLHandler::getSubscriptions(UpdateMessage &um) const
{
     map<int, Subscription*>::const_iterator it;
     for (it = mSubscriptions.begin(); it != mSubscriptions.end(); it++) {
	  um.addSubscription(it->second);
     }
}

/**
 * \brief Gets data from the currently held subscriptions that has to
 * do with the grid, e.g. Layer and Region subscriptions and puts it
 * into the provided UpdateMessage.
 *
 * \param um The UpdateMessage into which to put the subscriptions.
 */
void XMLHandler::getGridBasedSubscriptions(UpdateMessage &um) const
{
     map<int, Subscription*>::const_iterator it;
     for (it = mSubscriptions.begin(); it != mSubscriptions.end(); it++) {
	  if(dynamic_cast<RegionSubscription*>(it->second) ||
	     dynamic_cast<LayerSubscription*>(it->second)) {
	       um.addSubscription(it->second);
	  }
     }
}

/**
 * \brief Initializes the parser and error reporter to be used.
 *
 * \param buf A reference to the BufferObject.
 * \param ns The namespace part of the schemaLocation attribute.
 * \param schemaLocation Points out the schema to use for validation.
 * \param id The id of the Session this XMLHandler belongs to.
 */
XMLHandler::XMLHandler(Buffer &buf, string ns, string schemaLocation, int64_t id)
     : mId(id), mSimulation(0), mPVInitValueSet(0), mBuf(buf), mRegisteredForUpdates(false)
{
     try {
	  XMLPlatformUtils::Initialize();
	  if (!TranscoderWrapper::getTranscoder()) {
	       TranscoderWrapper::setEncoding("ISO-8859-1", 1);
	  }
     }
     catch(const XMLException &toCatch) {
	  Error e;
	  e << "Error during Xerces-c Initialization. Exception message:" << StrX(toCatch.getMessage());
	  throw e;
     }

     mParser = new XercesDOMParser;
     mParser->setValidationScheme(XercesDOMParser::Val_Always);
     mParser->setDoNamespaces(true);
     mParser->setDoSchema(true);
     mParser->setValidationSchemaFullChecking(true);
//     mParser->setIncludeIgnorableWhitespace(true);
     mParser->setIncludeIgnorableWhitespace(false);
     mParser->cacheGrammarFromParse(true);
     mParser->useCachedGrammarInParse(true);
     
     mpEntityResolver = 
	  new MemEntityResolver(mParser->getXMLEntityResolver());
     mParser->setXMLEntityResolver(mpEntityResolver);

     if (schemaLocation != "") {
  	  mParser->setExternalSchemaLocation(XStr(XMLHelper::encodeURLSpecialCharacters(ns) +
  						  " " + XMLHelper::encodeURLSpecialCharacters(schemaLocation)).str());
     }
     
     mErrorReporter = new ParserErrorReporter();
     mParser->setErrorHandler(mErrorReporter);
}

/**
 * \brief Destructor
 */
XMLHandler::~XMLHandler()
{
     if (mParser          ) { delete mParser; }
     if (mErrorReporter   ) { delete mErrorReporter; }
     if (mpEntityResolver ) { delete mpEntityResolver; mpEntityResolver = 0;}
     if (mPVInitValueSet) { delete mPVInitValueSet; }
     XMLPlatformUtils::Terminate();

     eraseSubscriptions();

     for (vector<Update*>::iterator it = mUpdates.begin(); it != mUpdates.end(); it++) {
	  delete *it;
     }

}

/**
 * \brief Parses and extracts data from the provided xml document.
 *
 * \param xml The xml message to handle.
 * \return The type of the message handled.
 */
int XMLHandler::handle(const string &xml)
{
     int res = eUnknown;
     mLastType = "Unknown";
     const char *xmlChar = xml.c_str();
     MemBufInputSource* memBuf = new MemBufInputSource((XMLByte*)xmlChar, xml.size(), "ClientMessage", false);
     memBuf->setCopyBufToStream(false);

     //  Parse the XML data, catching any XML exceptions that might propogate
     //  out of it.
     Error anError;  // Use this error of something happens that the parser can't handle.
     bool errOcc = false;
     mErrorReporter->resetErrors();
     try {
	  mParser->setValidationScheme(PropertyHandler::validateXML() ?
				       XercesDOMParser::Val_Always :
				       XercesDOMParser::Val_Never);
	  mParser->parse(*memBuf);
     }
     catch (const OutOfMemoryException&) {
	  errOcc = true;
	  anError << "OutOfMemoryException";
     }
     catch (const XMLException& e) {
	  errOcc = true;
	  anError << "An error occurred during parsing\n   Message: " << StrX(e.getMessage());
     }
     catch (const DOMException& e) {
	  errOcc = true;
	  const unsigned int maxChars = 2047;
	  XMLCh errText[maxChars + 1];

	  anError << "\nDOM Error during parsing. DOMException code is:  " << e.code;
	  
	  if (DOMImplementation::loadDOMExceptionMsg(e.code, errText, maxChars)) {
	       anError << "Message is: " << StrX(errText);
	  }
     }
     catch (...) {
	  errOcc = true;
	  anError << "An unknown error occurred during parsing";
     }

     if (mErrorReporter->errorsOccurred() || errOcc) {
	  mParser->resetDocumentPool();   // Destroy parsed document
	  delete memBuf;
	  debug("Dumping discarded message!");
	  IOHandler::dumpToFile(xml, "DISCARDED_MESSAGE.tmp");
	  if (errOcc) {
	       throw anError;
	  }
	  else {
	       throw mErrorReporter->errors();
	  }
     }
     else {
	  DOMDocument *doc = mParser->getDocument();
	  if (doc) {
	       DOMElement *root = doc->getDocumentElement();
	       StrX type(root->getAttribute(XStr("xsi:type").str()));
	       mLastType = type.str();
	       XMLHelper::removeNamespace(mLastType);
	       if (type == "sp:ConnectMessage") {
		    debug("ConnectMessage received.");
		    handleConnectMessage(*root);
		    res = eConnect;
	       }
	       else if (type == "sp:DisconnectMessage") {
		    debug("DisconnectMessage received.");
		    res = eDisconnect;
	       }
	       else if (type == "sp:InitializationMessage") {
		    debug("InitializationMessage received.");
//		    IOHandler::dumpToFile(xml, "init.xml");
		    StopWatch s;
		    s.start();
		    
		    mSimulation = DataObjectFactory::createDataObject(Reference::get(Reference::root(), "identifiables"),
								      XMLHelper::getFirstChildByTag(*root, "simulation"));

		    if (mPVInitValueSet) {
			 delete mPVInitValueSet;
		    }
		    mPVInitValueSet = new PVInitValueSet(root);
// 		    ofstream ofs("scenario.tmp.xml", ios_base::trunc);
// 		    mSimulation->toXML(ofs);
// 		    ofs.close();

		    s.stop();
		    debug("DataObject creation took " << s.secs() << " secs" );

		    res = eInitialization;
	       }
	       else if (type == "sp:ServerCapabilitiesMessage") {
		    debug("ServerCapabilitiesMessage received.");
		    res = eServerCapabilities;
	       }
	       else if (type == "sp:GetGridMessage") {
		    debug("GetGridMessage received.");
		    res = eGetGrid;
	       }
	       else if (type == "sp:RegisterForUpdatesMessage") {
		    debug("RegisterForUpdatesMessage received.");
		    handleRegisterForUpdatesMessage(*root);
		    res = eRegisterForUpdates;
	       }
	       else if (type == "sp:SubscriptionMessage") {
		    debug("SubscriptionMessage received.");
		    handleSubscriptionMessage(*root);
		    res = eSubscription;
	       }
	       else if (type == "sp:StepMessage") {
		    debug("StepMessage received.");
		    handleStepMessage(*root);
		    res = eStep;
	       }
	       else if (type == "sp:UpdateServerMessage") {
		    debug("UpdateServerMessage received.");
		    handleServerUpdateMessage(*root);
		    res = eUpdateServer;
	       }
	       else if (type == "sp:ResetMessage") {
		    debug("ResetMessage received.");
		    res = eReset;
	       }
	       else if (type == "sp:ProgressQueryMessage") {
		    debug("ProgressQueryMessage received.");
		    res = eProgressQuery;
	       }
	       else if (type == "sp:SetPropertyMessage") {
		    debug("SetPropertyMessage received.");
		    handleSetPropertyMessage(*root);
		    res = eSetProperty;
	       }
	       else if (type == "sp:LoadQueryMessage") {
		    res = eLoadQuery;
	       }
	       else {
		    Error e(Error::eWarning);
		    e << "Unknown message type --" << type << "-- ignoring...";
		    throw e;
	       }
	  }
	  else {
	       mParser->resetDocumentPool();   // Destroy parsed document
	       delete memBuf;
	       anError << "Couldn't get document from the XML parser";
	       throw anError;
	  }
     }

     mParser->resetDocumentPool();   // Destroy parsed document
     delete memBuf;
     return res;
}

/**
 * \brief Parses and extracts data from a ConnectMessage.
 *
 * \param n The DOMElement containing the message to be handled.
 */
void XMLHandler::handleConnectMessage(DOMElement &n)
{
     mSessionBigEndian = XMLHelper::getBool(n, "bigEndian");
}

/**
 * \brief Parses and extracts data from a RegisterForUpdatesMessage.
 *
 * \param n The DOMElement containing the message to be handled.
 */
void XMLHandler::handleRegisterForUpdatesMessage(DOMElement &n)
{
     mRegisteredForUpdates = XMLHelper::getBool(n, "register");     
}

/**
 * \brief Parses and extracts data from a SubscriptionMessage.
 *
 * \param n The DOMElement containing the message to be handled.
 */
void XMLHandler::handleSubscriptionMessage(DOMElement &n)
{
     vector<DOMElement*> v;
     XMLHelper::getChildElementsByTag(n, "subscription", v);
     for (vector<DOMElement*>::iterator it = v.begin(); it != v.end(); it++) {
	  createSubscription(**it);
     }
}

/**
 * \brief Parses and extracts data from a StepMessage.
 *
 * \param n The DOMElement containing the message to be handled.
 */
void XMLHandler::handleStepMessage(DOMElement &n)
{
     mNumberOfTimesteps = XMLHelper::getInt(n, "numberOfTimesteps");
//     debug("====== Number of timesteps " << mNumberOfTimesteps);
     // Shouldn't be optional in the future...
     if (XMLHelper::getFirstChildByTag(n, "detached")) {
	  mDetachedStep = XMLHelper::getBool(n, "detached");
     }
     else {
	  mDetachedStep = false;
     }
}

/**
 * \brief Parses and extracts data from a ServerUpdateMessage.
 *
 * \param n The DOMElement containing the message to be handled.
 */
void XMLHandler::handleServerUpdateMessage(DOMElement &n)
{
     vector<Error> errors;
     vector<DOMElement*> v;
     XMLHelper::getChildElementsByTag(n, "update", v);
     for (vector<DOMElement*>::iterator it = v.begin(); it != v.end(); it++) {
	  try {
	       mUpdates.push_back(new Update(**it, mId));
	  } catch (Error& e) {
	       errors.push_back(e);
	  }
     }
     if (!errors.empty()) {
	  throw errors;
     }
}

/**
 * \brief Parses and extracts data from a SetPropertyMessage.
 *
 * \param n The DOMElement containing the message to be handled.
 */
void XMLHandler::handleSetPropertyMessage(DOMElement &n)
{
     string property;
     string value;
     XMLHelper::getString(n, "property", property);
     XMLHelper::getString(n, "value", value);
     PropertyHandler::setProperty(property, value);
}

/**
 * \brief Helper for adding a new Subscription. Performs some error
 * handling.
 *
 * \param sub A pointer to the Subscription to add.
 */
void XMLHandler::addSubscription(Subscription *sub)
{
     if (!sub) {
	  Error e;
	  e << "Null Subscription in XMLHandler::addSubscription()";
	  throw e;
     }

     map<int, Subscription*>::iterator it = mSubscriptions.find(sub->id());
     if (it != mSubscriptions.end()) {
	  slog << "Subscription " << it->first << " has same id as another Subscription. "
	       << "The other subscription will be deleted..." << logEnd;
	  delete it->second;
	  mSubscriptions.erase(it->first);
     }
     mSubscriptions[sub->id()] = sub;
}

/**
 * \brief Creates a new subscription from the provided DOMElement.
 *
 * \param n The DOMElement containing data on the Subscription to be
 * created.
 */
void XMLHandler::createSubscription(DOMElement &n)
{
     StrX type(n.getAttribute(XStr("xsi:type").str()));
     if (type == "sp:RootSubscription") {
//	  addSubscription(new GeneralSubscription(&n, mBuf));
	  debug("=========== RootSubscriptions no longer supported! ===========");
     }
     else if (type == "sp:GeneralSubscription") {
//	  addSubscription(new GeneralSubscription(&n, mBuf));
	  debug("=========== GeneralSubscriptions no longer supported! ===========");
     }
     else if (type == "sp:StratmasObjectSubscription") {
	  addSubscription(new StratmasObjectSubscription(&n, mBuf, mId));
     }
     else if (type == "sp:LayerSubscription") {
	  addSubscription(new LayerSubscription(&n, mBuf, mSessionBigEndian));
     }
     else if (type == "sp:RegionSubscription") {
	  addSubscription(new RegionSubscription(&n, mBuf));
     }
     else if (type == "sp:Unsubscription") {
	  int toRemove = XMLHelper::getIntAttribute(n, "id");
	  map<int, Subscription*>::iterator it = mSubscriptions.find(toRemove);
	  debug("%%%%%%%%% Got remove subscription message");
	  if (it != mSubscriptions.end()) {
	       debug("Removing Subscription: '" << it->first << "'");
	       delete it->second;
	       mSubscriptions.erase(it->first);
	  }
	  else {
	       slog << "Tried to unsubscribe to non-existing Subscription (id: "
		    << toRemove << ")" << logEnd;
	  }
     }
     else {
	  Error e(Error::eWarning);
	  e << "Stratmas does not support Subscription of type: " << type.str();
	  throw e;
     }
}

/**
 * \brief Deletes all subscriptions held by this XMLHandler.
 */
void XMLHandler::eraseSubscriptions() {
     for (map<int, Subscription*>::iterator it = mSubscriptions.begin(); it != mSubscriptions.end(); it++) {
	  delete it->second;
     }
     mSubscriptions.clear();
}
