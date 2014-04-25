#!/bin/python

import array
import struct
import sys

defaultbyteorder="little"

def getInt(f, byteorder=defaultbyteorder, signed=True):
    return int.from_bytes(f.read(4), byteorder=byteorder, signed=signed)

def getInts(f, no, byteorder=defaultbyteorder):
    ar = array.array('i', f.read(no*4))
    if byteorder != sys.byteorder:
        ar.byteswap()
    return ar.tolist()

def getDoubles(f, no, byteorder=defaultbyteorder):
    ar = array.array('d', f.read(no*8))
    if byteorder != sys.byteorder:
        ar.byteswap()
    return ar.tolist()

def getNull(f, rlen):
    return None

def getPoint(f, rlen):
    return tuple(getDoubles(f, 2))

def getMultiPoint(f, rlen):
    box = getDoubles(f, 4)
    numPoints = getInt(f)
    points = []
    for i in range(0,numPoints):
        p = getPoint(f, 2*8)
        points.append(p)
    return (box, numPoints, points)

def getPolyline(f, rlen):
    box = getDoubles(f, 4)
    numParts = getInt(f)
    numPoints = getInt(f)
    parts = getInts(f, numParts)
    points = []
    for i in range(0,numPoints):
        p = getPoint(f,  2*8)
        points.append(p)
    return (box, numParts, numPoints, parts, points)

def getPolygon(f, rlen):
    box = getDoubles(f, 4)
    numParts = getInt(f)
    numPoints = getInt(f)
    parts = getInts(f, numParts)
    points = []
    for i in range(0,numPoints):
        p = getPoint(f,  2*8)
        points.append(p)
    return (box, numParts, numPoints, parts, points)

def getPointZ(f, rlen):
    return tuple(getDoubles(f, 4))

def getPolygonZ(f, rlen):
    box = getDoubles(f, 4)
    numParts = getInt(f)
    numPoints = getInt(f)
    parts = getInts(f, numParts)
    points = []
    for i in range(0,numPoints):
        p = getPoint(f,  2*8)
        points.append(p)
    zrange = getDoubles(f, 2)
    zarray = getDoubles(f, numPoints)
    mrange = getDoubles(f, 2)
    marray = getDoubles(f, numPoints)
    return (box, numParts, numPoints, parts, points, zrange, zarray, mrange, marray)

#def getPolylineZ(f, rlen):
#def getMultiPointZ(f, rlen):
#def getPointM(f, rlen):
#def getPolylineM(f, rlen):
#def getPolygonM(f, rlen):
#def getMultiPointM(f, rlen):
#def getMultiPatch(f, rlen):

rtypes = {
        0 : getNull,
        1 : getPoint,
        3 : getPolyline,
        5 : getPolygon,
        8 : getMultiPoint,
        11: getPointZ,
        #13: getPolylineZ,
        15: getPolygonZ,
        #18: getMultiPointZ,
        #21: getPointM,
        #23: getPolylineM,
        #25: getPolygonM,
        #28: getMultiPointM,
        #31: getMultiPatch
}

def getType(f, rtype, rlen):
    return rtypes[rtype](f, rlen)

def printShpfile(dafile):
    with open(dafile, "rb") as f:
        fcode = getInt(f, byteorder="big")
        funused = getInts(f, 5, byteorder="big")
        flen = getInt(f, byteorder="big")
        fver = getInt(f, byteorder="little")
        fshptype = getInt(f, byteorder="little")
        print("fcode =", hex(fcode))
        print("funused =", funused)
        print("flen =", flen)
        print("fver =", fver)
        print("fshptype =", fshptype)

        fmbr = getDoubles(f, 4)
        fZr = getDoubles(f, 2)
        fMr = getDoubles(f, 2);
        print("fmbr =", fmbr)
        print("fZr =", fZr)
        print("fMr =", fMr)

        hlen = 100
        flen = flen * 2 # 16-bit words to 8-bit words
        flen = flen - hlen

        while flen > 0:
            print() # separator

            rno = getInt(f, byteorder="big")
            rlen = getInt(f, byteorder="big")
            print("rno =", rno)
            print("rlen =", rlen)

            rtype = getInt(f, byteorder="little")
            print("rtype =", rtype)
            if rtype in rtypes:
                rdata = getType(f, rtype, rlen)
                print("rdata =", rdata)
            else:
                print("unimplemented type ==> skipping")
                f.read(rlen*2)
            flen = flen - 2*(2*2 + rlen)

printShpfile(sys.argv[1])
