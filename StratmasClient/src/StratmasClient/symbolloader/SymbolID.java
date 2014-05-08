/*
 * A different approach to representing a SymbolID: how about keeping it as a bunch of enumerations, since most fields are easily
 * enumerated? This drops the explicit string representation, instead converting to/from string when necessary.
 */

package ApproxsimClient.symbolloader;

import java.io.*;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.event.*;

class SymbolID {

    // Base class for a field.
    static abstract class Field {
        private int pos = 0;

        // This is overridden by subclasses that need references to other fields in same context.
        public void configure(SymbolID parent) {}

        public String getName() {
            return "?";
        }

        public short getWidth() {
            return 0;
        }

        public int getPos() {
            return pos;
        }

        public int setPos(int p) {
            pos = p;
            return p + getWidth();
        }

        public String cut(String s) {
            return s.substring(pos, pos + getWidth());
        }

        abstract public String encode();

        abstract public boolean decode(String in);

        abstract public void reset();
    }

    static class EnumerationTable {
        protected String name;
        protected String[] symbol, meaning;
        protected short defValue;
        protected short width;

        // Initialize from pairs of "SYMBOL:MEANING", separated by semicolon.
        protected void initFromString(String def) {
            String[] part = def.split(";");
            Arrays.sort(part);
            symbol = new String[part.length];
            meaning = new String[part.length];
            width = -1;
            for (int i = 0; i < part.length; i++) {
                int ci = part[i].indexOf(':');
                if (ci > 0) {
                    symbol[i] = part[i].substring(0, ci);
                    if (part[i].charAt(ci - 1) == '*') {
                        defValue = (short) i;
                        symbol[i] = symbol[i].substring(0, ci - 1);
                    }
                    width = (short) Math.max(width, symbol[i].length());
                    meaning[i] = part[i].substring(ci + 1);
                }
            }
        }

        protected void initFromLineNumberReader(LineNumberReader r) {
            StringBuffer def = new StringBuffer(5 * 1024);
            def.append("--:NONE");        // Ugly hack, but handy for country codes (sole user).
            try {
                for (String line; (line = r.readLine()) != null;) {
                    if (line.length() == 0) continue;
                    if (line.charAt(0) == '#') continue;
                    int cl, spl;
                    for (cl = 0; Character.isUpperCase(line.charAt(cl)); cl++);
                    for (spl = cl + 1; Character.isWhitespace(line.charAt(spl)); spl++);
                    if (cl > 0 && spl < line.length()) {
                        if (def.length() > 0) def.append(";");
                        def.append(line.substring(0, cl));
                        def.append(":");
                        def.append(line.substring(spl));
                    }
                }
            } catch (IOException ioe) {
                System.out.println("EnumarationTable: I/O error");
            }
            initFromString(def.toString());
        }

        protected void initFromStream(InputStream stream) {
            LineNumberReader r = null;
            new Vector(50);
            r = new LineNumberReader(new InputStreamReader(stream));
            initFromLineNumberReader(r);
        }

        // Initialize from file contents. Typically used for FIPS 10-4 country code table.
        // File format is simply lines of "SYMBOL MEANING", with an arbitrary but non-zero
        // number of whitespace characters in between. This builds a huge semicolon-separated
        // string, so is not exactly optimal in terms of, well, memory or time.
        protected void initFromFile(String fName) {
            LineNumberReader r = null;
            new Vector(50);
            try {
                r = new LineNumberReader(new FileReader(fName));
                initFromLineNumberReader(r);
            } catch (FileNotFoundException fnf) {
                System.out.println("EnumerationTable couldn't open " + fName
                        + " -- won't work");
            }
        }

        public EnumerationTable(String def) {
            defValue = -1;
            if (def.startsWith("file://"))
                initFromFile(def.substring(7));
            else initFromString(def);
            name = "";
        }

        public EnumerationTable(InputStream stream) {
            defValue = -1;
            initFromStream(stream);
            name = "";
        }

        public EnumerationTable(String aName, String def) {
            this(def);
            name = aName;
        }

        public EnumerationTable(String aName, InputStream def) {
            this(def);
            name = aName;
        }

        public String getName() {
            return name;
        }

        public short getWidth() {
            return width;
        }

        public int length() {
            return symbol.length;
        }

        public short getDefault() {
            return defValue;
        }

        public void setDefault(short v) {
            defValue = v;
        }

        public String encode(short value) {
            return symbol[value];
        }

        public short decode(String sym) {
            return (short) Arrays.binarySearch(symbol, sym);
        }

        public String getMeaning(short value) {
            return meaning[value];
        }

        public String[] getValues() {
            String[] v = new String[meaning.length];
            for (int i = 0; i < meaning.length; i++) {
                v[i] = symbol[i] + " " + meaning[i];
            }
            return v;
        }
    }

    static class EnumeratedField extends Field {
        private EnumerationTable table;
        private short value;

        public EnumeratedField(EnumerationTable aTable) {
            table = aTable;
            reset();
        }

        public String getName() {
            return table.name;
        }

        public short getWidth() {
            return table.getWidth();
        }

        public String encode() {
            return table.symbol[value];
        }

        public boolean decode(String in) {
            if (in != null) {
                short i = table.decode(in);
                if (i >= 0) {
                    value = i;
                    return true;
                }
            }
            return false;
        }

        public short getValue() {
            return value;
        }

        public void reset() {
            value = table.getDefault();
        }
    }

    static class CodingSchemeField extends EnumeratedField {
        private static final EnumerationTable theTable = new EnumerationTable(
                "CODING SCHEME", "S*:WARFIGHTING;G:TACTICAL GRAPHICS;"
                        + "I:INTELLIGENCE;M:MAPPING;O:MOOTW");

        public CodingSchemeField() {
            super(theTable);
        }
    }

    static class AffiliationField extends EnumeratedField {
        private static final EnumerationTable theTable = new EnumerationTable(
                "AFFILIATION", "P:PENDING;U*:UNKNOWN;A:ASSUMED FRIEND;"
                        + "F:FRIEND;N:NEUTRAL;S:SUSPECT;H:HOSTILE;"
                        + "J:JOKER;K:FAKER;O:NONE SPECIFIED");

        static public String[] getValues() {
            return theTable.getValues();
        }

        public AffiliationField() {
            super(theTable);
        }

        // Map every affiliation on another, for frame loading purposes (CGM problems).
        static String getIconAlias(final String affil) {
            final String icon = "AFHJKONPSU", alias = "FFHHHONPHU";        // FIXME: Violates standard.
            int i;
            if ((i = icon.indexOf(affil.charAt(0))) >= 0)
                return alias.substring(i, i + 1);
            return null;
        }
    }

    static class BattleDimensionField extends EnumeratedField {
        private static final EnumerationTable theTable = new EnumerationTable(
                "BATTLE DIMENSION", "P:SPACE;A:AIR;G*:GROUND;S:SEA SURFACE;"
                        + "U:SEA SUBSURFACE;F:SOF;X:OTHER;Z:UNKNOWN");

        public BattleDimensionField() {
            super(theTable);
        }
    }

    static class StatusField extends EnumeratedField {
        private static final EnumerationTable theTable = new EnumerationTable(
                "STATUS", "A:ANTICIPATED/PLANNED;P*:PRESENT");

        static public String[] getValues() {
            return theTable.getValues();
        }

        public StatusField() {
            super(theTable);
        }
    }

    static class FunctionIDField extends Field {
        private String address;
        private Field codingScheme, battleDimension;

        static final String[] equipPrefix = { "1.X.2.2", "1.X.3.2", "1.X.5.2" },
                instPrefix = { "1.X.3.3" };

        public FunctionIDField() {
            codingScheme = null;
            battleDimension = null;
            reset();
        }

        public void configure(SymbolID parent) {
            codingScheme = parent.getField(SymbolID.F_CODINGSCHEME);
            battleDimension = parent.getField(SymbolID.F_BATTLEDIMENSION);
        }

        public String getName() {
            return "FUNCTION ID";
        }

        public short getWidth() {
            return 6;
        }

        public String encode() {
            FunctionIDTree.FunctionIDNode fin = SymbolLoader.ftree
                    .findByAddress(address);
            if (fin != null) {
                return fin.getSymbolID().substring(2);
            }
            return "------";
        }

        public boolean decode(String sym) {
            StringBuffer fs = new StringBuffer(8);
            fs.append(codingScheme.encode());
            fs.append(battleDimension.encode());
            fs.append(sym);
            FunctionIDTree.FunctionIDNode fin = SymbolLoader.ftree
                    .findBySymbolID(fs.toString());
            if (fin != null) {
                address = fin.getAddress();
                return true;
            }
            return false;
        }

        public void reset() {
            address = "1.X.3.1";
        }

        public boolean isUnit() {
            if (isEquipment() || isInstallation()) return false;
            return true;
        }

        public boolean isEquipment() {
            for (int i = 0; i < equipPrefix.length; i++)
                if (address.startsWith(equipPrefix[i])) return true;
            return false;
        }

        public boolean isInstallation() {
            for (int i = 0; i < instPrefix.length; i++)
                if (address.startsWith(instPrefix[i])) return true;
            return false;
        }

        public String getAddress() {
            return address;
        }
    }

    // Symbol modifier field can be enumerated as well, but since it's fairly large
    // at >120 states, this more dedicated field type handles it a bit more cleverly.
    static class SymbolModifierField extends Field {
        public static final int TYPE_UNIT_NONE = 0, TYPE_UNIT_HQ = 1,
                TYPE_UNIT_TFHQ = 2, TYPE_UNIT_FDHQ = 3, TYPE_UNIT_FDTFHQ = 4,
                TYPE_UNIT_TF = 5, TYPE_UNIT_FD = 6, TYPE_UNIT_FDTF = 7,
                TYPE_INSTALLATION = 8, TYPE_EQUIPMENT = 9, TYPE_TOWED = 10;
        public static final int UNIT_NONE = 0, UNIT_TEAM = 1, UNIT_SQUAD = 2,
                UNIT_SECTION = 3, UNIT_PLATOON = 4, UNIT_COMPANY = 5,
                UNIT_BATTALION = 6, UNIT_REGIMENT = 7, UNIT_BRIGADE = 8,
                UNIT_DIVISION = 9, UNIT_CORPS = 10, UNIT_ARMY = 11,
                UNIT_ARMYGROUP = 12, UNIT_REGION = 13;
        public static final int INST_NONE = 0, INST_FD = 1;
        public static final int EQUIP_NONE = 0, EQUIP_WHEELED = 1,
                EQUIP_CROSSCOUNTRY = 2, EQUIP_TRACKED = 3,
                EQUIP_WHEELEDTRACKED = 4, EQUIP_TOWED = 5, EQUIP_RAIL = 6,
                EQUIP_OVERTHESNOW = 7, EQUIP_SLED = 8, EQUIP_PACKANIMALS = 9,
                EQUIP_BARGE = 10, EQUIP_AMPHIBIOUS = 11;
        public static final int TOWED_SHORT = 0, TOWED_LONG = 1;

        protected static final String TYPES = "-ABCDEFGHMN",                // 1:1 with type.
                UNITS = "-ABCDEFGHIJKLM",        // 1:1 with UNIT-codes.
                INSTS = "-B",                        // 1:1 with INST-codes.
                EQUIPS = "-OPQRSTUVWXY",        // 1:1 with EQUIP-codes.
                TOWEDS = "SL";                        // 1:1 with TOWED-codes.

        protected static final String[] CODES = { UNITS, INSTS, EQUIPS, TOWEDS };

        private FunctionIDField func;
        private short type, code;

        public SymbolModifierField() {
            func = null;
            reset();
        }

        public void configure(SymbolID parent) {
            func = (FunctionIDField) parent.getField(F_FUNCTIONID);
        }

        public String getName() {
            return "SYMBOL MODIFIER";
        }

        public short getWidth() {
            return 2;
        }

        public boolean isHeadquarters() {
            return type >= TYPE_UNIT_HQ && type <= TYPE_UNIT_FDTFHQ;
        }

        public boolean isFeintDummy() {
            return type == TYPE_UNIT_FDHQ || type == TYPE_UNIT_FDTFHQ
                    || type == TYPE_UNIT_FD || type == TYPE_UNIT_FDTF;
        }

        public boolean isTaskForce() {
            return type == TYPE_UNIT_TFHQ || type == TYPE_UNIT_FDTFHQ
                    || type == TYPE_UNIT_TF || type == TYPE_UNIT_FDTF;
        }

        protected boolean typeIsValid(int t) {
            if (t == TYPE_UNIT_NONE) return true;
            if (t > TYPE_UNIT_NONE && t < TYPE_INSTALLATION)
                return func.isUnit();
            else if (t == TYPE_INSTALLATION)
                return true;// func.isInstallation();
            else if (t == TYPE_EQUIPMENT) return func.isEquipment();
            return false;
        }

        public int getMobility() {
            if (type == TYPE_EQUIPMENT) return code;
            return -1;
        }

        public String encode() {
            StringBuffer sb = new StringBuffer(2);
            sb.append(TYPES.charAt(type));
            int t = type < TYPE_INSTALLATION ? 0
                    : (type - TYPE_INSTALLATION) + 1;
            sb.append(CODES[t].charAt(code));
            return sb.toString();
        }

        public boolean decode(String sym) {
            int x;
            if ((x = TYPES.indexOf(sym.charAt(0))) >= 0) {
                if (typeIsValid(x)) {
                    type = (short) x;
                    int t = type < TYPE_INSTALLATION ? 0
                            : (type - TYPE_INSTALLATION) + 1;
                    if ((x = CODES[t].indexOf(sym.charAt(1))) >= 0) {
                        code = (short) x;
                        return true;
                    }
                }
            }
            return false;
        }

        public void reset() {
            type = TYPE_UNIT_NONE;
            code = UNIT_NONE;
        }

        // Useful for hierarchy create. Tries to be reasonably smart.
        public void setEchelon(int e) {
            if (type < TYPE_UNIT_NONE || type > TYPE_UNIT_FDTF)
                type = TYPE_UNIT_NONE;
            if (e < UNIT_NONE || e > UNIT_REGION) e = UNIT_NONE;
            code = (short) e;
        }

        // Return index compatible with e.g. array in ModifierPane, or -1.
        public int getEchelon() {
            if (type >= TYPE_UNIT_NONE && type <= TYPE_UNIT_FDTF) return code;
            return -1;
        }
    }

    static class CountryCodeField extends EnumeratedField {
        private static final EnumerationTable theTable =
        // new EnumerationTable("COUNTRY CODE", "file://fips10-4.txt");
        new EnumerationTable("COUNTRY CODE",
                CountryCodeField.class.getResourceAsStream("fips10-4.txt"));

        static public String[] getValues() {
            return theTable.getValues();
        }

        static public int decodeToIndex(String s) {
            return (int) theTable.decode(s);
        }

        public CountryCodeField() {
            super(theTable);
            theTable.setDefault((short) 0);        // Default country to "--".
            reset();
        }
    }

    static class OrderOfBattleField extends EnumeratedField {
        private static final EnumerationTable theTable = new EnumerationTable(
                "ORDER OF BATTLE",
                "A:AIR OB;E:ELECTRONIC OB;C:CIVILIAN OB;G*:GROUND OB;"
                        + "N:MARITIME OB;S:STRATEGIC FORCE RELATED");

        static public String[] getValues() {
            return theTable.getValues();
        }

        public OrderOfBattleField() {
            super(theTable);
        }
    }

    // --------------------------------------------------------------------------------------

    public static final int F_CODINGSCHEME = 0, F_AFFILIATION = 1,
            F_BATTLEDIMENSION = 2, F_STATUS = 3, F_FUNCTIONID = 4,
            F_SYMBOLMODIFIER = 5, F_COUNTRYCODE = 6, F_ORDEROFBATTLE = 7,
            F_NUMFIELDS = 8;
    private static final Class fieldClass[] = { CodingSchemeField.class,
            AffiliationField.class, BattleDimensionField.class,
            StatusField.class, FunctionIDField.class,
            SymbolModifierField.class, CountryCodeField.class,
            OrderOfBattleField.class };

    private Field[] field;
    private ChangeListener changeListener;

    public static Class getFieldClass(int id) {
        if (id >= 0 && id < fieldClass.length) return fieldClass[id];
        return null;
    }

    public SymbolID() {
        field = new Field[F_NUMFIELDS];
        for (int i = 0, p = 0; i < field.length; i++) {
            try {
                field[i] = (Field) fieldClass[i].newInstance();
            } catch (InstantiationException e) {
                System.out.println(e);
            } catch (IllegalAccessException e) {
                System.out.println(e);
            }
            field[i].configure(this);
            p = field[i].setPos(p);
        }
    }

    public SymbolID(ChangeListener cl) {
        this();
        changeListener = cl;
    }

    // Copy constructor.
    public SymbolID(SymbolID src) {
        this();
        set(src.toString());
        changeListener = src.changeListener;
    }

    // Set symbol ID from a string representation. Will validate every field, semi-seriously at least.
    public boolean set(String s) {
        // Make sure string has proper length, and is all upper case.
        StringBuffer sb = new StringBuffer(15);
        sb.append(s);
        if (sb.length() > 15)
            sb.setLength(15);
        else while (sb.length() < 15)
            sb.append('-');
        for (int i = 0; i < sb.length(); i++)
            sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
        s = sb.toString();
        // Now loop through, cutting out each field's substring, and decoding it.
        boolean ret = true;
        for (int i = 0; i < field.length; i++) {
            if (!field[i].decode(field[i].cut(s))) {
                field[i].reset();        // Decode failed, so reset the field.
                ret = false;
            }
        }
        if (changeListener != null)
            changeListener.stateChanged(new ChangeEvent(this));
        return ret;
    }

    public boolean set(int f, String s) {
        if (f >= 0 && f < field.length) {
            boolean ret = field[f].decode(s);
            if (ret && changeListener != null)
                changeListener.stateChanged(new ChangeEvent(this));
        }
        return false;
    }

    // Return actual Field object for a certain field identifier.
    public Field getField(int i) {
        if (i >= 0 && i < field.length) return field[i];
        return null;
    }

    // Return the text version of a field. Since we don't buffer the coded form, we encode on the fly.
    public String getFieldValue(int i) {
        if (i >= 0 && i < field.length) return field[i].encode();
        return null;
    }

    public boolean setField(int i, String s) {
        if (i >= 0 && i < field.length) return field[i].decode(s);
        return false;
    }

    public int getFieldValueAsIndex(int i) {
        if (i >= 0 && i < field.length) {
            if (field[i] instanceof EnumeratedField)
                return ((EnumeratedField) field[i]).getValue();
        }
        return -1;
    }

    public String getIconAddress() {
        return ((FunctionIDField) field[F_FUNCTIONID]).getAddress();
    }

    public String getAddress() {
        return getIconAddress();
    }

    public boolean isEquipment() {
        return ((FunctionIDField) field[F_FUNCTIONID]).isEquipment();
    }

    public boolean isHeadquarters() {
        return ((SymbolModifierField) field[F_SYMBOLMODIFIER]).isHeadquarters();
    }

    public boolean isFeintDummy() {
        return ((SymbolModifierField) field[F_SYMBOLMODIFIER]).isFeintDummy();
    }

    public boolean isTaskForce() {
        return ((SymbolModifierField) field[F_SYMBOLMODIFIER]).isTaskForce();
    }

    public int getMobility() {
        if (isEquipment()) {
            return ((SymbolModifierField) field[F_SYMBOLMODIFIER])
                    .getMobility();
        }
        return -1;
    }

    // Return string representation of full symbol ID.
    public String toString() {
        StringBuffer buf = new StringBuffer(15);
        for (int i = 0; i < field.length; i++)
            buf.append(field[i].encode());
        return buf.toString();
    }
}
