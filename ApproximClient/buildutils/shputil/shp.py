#!/bin/python

import array
import struct
import sys
import io

defaultbyteorder="little"

def putInt(f, v, byteorder=defaultbyteorder):
    if byteorder == "little":
        fmt = "<i"
    elif byteorder == "big":
        fmt = ">i"
    else:
        raise Exception("bad endian")
    return f.write(struct.pack(fmt, v))

def putInts(f, vs, byteorder=defaultbyteorder):
    if byteorder == "little":
        fmt = "<i"
    elif byteorder == "big":
        fmt = ">i"
    else:
        raise Exception("bad endian")
    for v in vs:
        f.write(struct.pack(fmt, v))
        

def putDoubles(f, vs, byteorder=defaultbyteorder):
    if byteorder == "little":
        fmt = "<d"
    elif byteorder == "big":
        fmt = ">d"
    else:
        raise Exception("bad endian")
    for v in vs:
        f.write(struct.pack(fmt, v))

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

def getNull(frlen):
    return {}

def getPoint(f):
    return tuple(getDoubles(f, 2))

def getPolygon(f):
    data = {}
    data["box"] = getDoubles(f, 4)
    data["numParts"] = getInt(f)
    data["numPoints"] = getInt(f)
    data["parts"] = getInts(f, data["numParts"])
    data["points"] = list([getPoint(f) for i in range(0, data["numPoints"])])
    return data

def getPointZ(f):
    x,y = tuple(getDoubles(f, 4))
    return {"x": x, "y": y}

def getPolygonZ(f):
    data = {}
    data["box"] = getDoubles(f, 4)
    data["numParts"] = getInt(f)
    data["numPoints"] = getInt(f)
    data["parts"] = getInts(f, data["numParts"])
    data["points"] = list([getPoint(f) for i in range(0, data["numPoints"])])
    print(data)
    data["zrange"] = getPoint(f)
    data["zarray"] = getDoubles(f, data["numPoints"])
    data["mrange"] = getPoint(f)
    data["marray"] = getDoubles(f, data["numPoints"])
    return data

rtypes_gets = {
        0 : getNull,
        1 : getPoint,
        #3 : getPolyline,
        5 : getPolygon,
        #8 : getMultiPoint,
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


def getType(f, rtype):
    return rtypes_gets[rtype](f)




def putNull(f, rtype, rlen):
    return 0

def putPoint(f, rdata):
    putDoubles(f, list(rdata))
    return 2*8

def putPolygon(f, rdata):
    putDoubles(f, rdata["box"])
    putInt(f, rdata["numParts"])
    putInt(f, rdata["numPoints"])
    putInts(f, rdata["parts"])
    plen = sum([putPoint(f, p) for p in rdata["points"]])
    return 4*8 + 4 + 4 + 4*rdata["numParts"] + plen

rtypes_puts = {
        #0 : putNull,
        1 : putPoint,
        #3 : putPolyline,
        5 : putPolygon,
        #8 : putMultiPoint,
        #11: putPointZ,
        #13: putPolylineZ,
        #15: putPolygonZ,
        #18: putMultiPointZ,
        #21: putPointM,
        #23: putPolylineM,
        #25: putPolygonM,
        #28: putMultiPointM,
        #31: putMultiPatch
}

def putType(f, rtype, rdata):
    return rtypes_puts[rtype](f, rdata)





def getShpHeader(f):
    return {
        "code" : getInt(f, byteorder="big"),
        "unused" : getInts(f, 5, byteorder="big"),
        "len" : getInt(f, byteorder="big"),
        "ver" : getInt(f, byteorder="little"),
        "shptype" : getInt(f, byteorder="little"),
        "mbr" : getDoubles(f, 4),
        "zrange" : getPoint(f),
        "mrange" : getPoint(f)
    }

def getShpRecord(f):
    record = {}
    record["no"] = getInt(f, byteorder="big")
    record["len"] = getInt(f, byteorder="big")
    record["type"] = getInt(f, byteorder="little")
    if record["type"] not in rtypes_gets:
        print("unimplemented type ==> dying")
        sys.exit(1)
    else:
        record["data"] = getType(f, record["type"])
    return record

def printShpHeader(header):
    print("code =", header["code"])
    print("unused =", header["unused"])
    print("len =", header["len"])
    print("ver =", header["ver"])
    print("shptype =", header["shptype"])
    print("mbr =", header["mbr"])
    print("zrange =", header["zrange"])
    print("mrange =", header["mrange"])

def printShpRecord(record):
    print("no =", record["no"])
    print("len =", record["len"])
    print("type =", record["type"])
    print("data =", record["data"])

def filterShpFile(infile, outfile):
    with open(outfile, "wb") as o:
        with open(infile, "rb") as f:
            header = getShpHeader(f)
            printShpHeader(header)

            if header["shptype"] == 15: # PolygonZ
                header["shptype"] = 5 # Polygon (non-Z)

            putInt(o, header["code"], byteorder="big")
            putInts(o, header["unused"], byteorder="big")
            putInt(o, header["len"], byteorder="big")
            putInt(o, header["ver"], byteorder="little")
            putInt(o, header["shptype"], byteorder="little")
            putDoubles(o, header["mbr"])
            putPoint(o, header["zrange"])
            putPoint(o, header["mrange"])

            hlen = 50   # 16-bit word no
            nlen = hlen #

            flen = 2*(header["len"] - hlen) # 8-bit word no

            while flen > 0:
                print() # separator

                record = getShpRecord(f)
                printShpRecord(record)

                rno = record["no"]
                rlen = record["len"]
                rtype = record["type"]
                rdata = record["data"]

                if rtype == 15: # PolygonZ
                    print("converting PolygonZ to Polygon (non-Z)")
                    rtype = 5 # Polygon  (no Z)
                    nrdata = dict([(k,rdata[k]) for k in rdata if k in ["box",
                        "numParts", "numPoints", "parts", "points"]])

                    print("new rtype =", rtype)
                    print("new rdata =", rdata)

                putInt(o, rno, byteorder="big")
                putInt(o, rlen, byteorder="big")
                putInt(o, rtype, byteorder="little")

                nrlen = 4 + putType(o, rtype, rdata)
                print("new rlen =", nrlen)

                o.seek(-nrlen+4, 1)
                putInt(o, nrlen, byteorder="big")
                o.seek(-4+nrlen, 1)

                flen = flen - 2*4 - 2*rlen
                nlen = nlen + (2*4 + nrlen)/2

            nlen = int(nlen)
            print("nlen =", nlen)
            o.seek(24)
            putInt(o, nlen, byteorder="big")

def printShpFile(infile):
    with open(infile, "rb") as f:
        header = getShpHeader(f)
        printShpHeader(header)

        hlen = 50   # 16-bit word no
        nlen = hlen #

        flen = 2*(header["len"] - hlen) # 8-bit word no

        while flen > 0:
            print() # separator

            record = getShpRecord(f)
            printShpRecord(record)

            rlen = record["len"] # 16-bit word no
            flen = flen - 2*4 - 2*rlen # len: 2 int + content


if len(sys.argv) == 3:
    filterShpFile(sys.argv[1], sys.argv[2])
elif len(sys.argv) == 2:
    printShpFile(sys.argv[1])
else:
    print("Wrong no of args.")
