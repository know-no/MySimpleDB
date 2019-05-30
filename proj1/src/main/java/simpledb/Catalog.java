package simpledb;

import javafx.scene.control.Tab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * The Catalog keeps track of all available tables in the database and their
 * associated schemas.
 * For now, this is a stub catalog that must be populated with tables by a
 * user program before it can be used -- eventually, this should be converted
 * to a catalog that reads a catalog table from disk.
 */

public class Catalog {

    /**
     * hashmap 储存表
     * Each table is represented by a single DbFile.所以存储的是 Dbfile -- name , 或者是id
     * 我认为这里是一对一的关系，即一个表对应一个Dbfile
     * HeapFile实现了 Dbfile接口
     */
//    private HashMap<String, DbFile> name2table;
//    private HashMap<Integer , DbFile> id2table;
    /**
     * 新建一个Table类，保存主键，DBfile，tablename等
     */
    private HashMap<Integer, Table> id2table;
    private HashMap<String, Table> name2table;

    /**
     * Constructor.
     * Creates a new, empty catalog.
     */
    public Catalog() {
        // some code goes here
        id2table = new HashMap<Integer, Table>();
        name2table = new HashMap<String, Table>();
    }

    /**
     * Add a new table to the catalog.
     * This table's contents are stored in the specified DbFile.
     * @param file the contents of the table to add;  file.getId() is the identfier of
     *    this file/tupledesc param for the calls getTupleDesc and getFile
     * @param name the name of the table -- may be an empty strDatabase Design and Implementationing.  May not be null.  If a name
     * @param pkeyField the name of the primary key field
     * conflict exists, use the last table to be added as the table for a given name.
     */
    public void addTable(DbFile file, String name, String pkeyField) {
        // some code goes here
        //一个dbfile 返回 an ID uniquely identifying this HeapFile.
        Table t = new Table(file, name, pkeyField);
        Integer id = file.getId();
        id2table.put(id, t);
        name2table.put(name, t);
    }

    public void addTable(DbFile file, String name) {
        addTable(file, name, "");
    }

    /**
     * Add a new table to the catalog.
     * This table has tuples formatted using the specified TupleDesc and its
     * contents are stored in the specified DbFile.
     * @param file the contents of the table to add;  file.getId() is the identfier of
     *    this file/tupledesc param for the calls getTupleDesc and getFile
     */
    public void addTable(DbFile file) {
        addTable(file, (UUID.randomUUID()).toString());
    }

    /**
     * gettable方法
     */
    private Table  getTable(String name) throws NoSuchElementException{
        Table t = name2table.get(name);
        if( null == t){
            throw new NoSuchElementException("the table doesn't exist");
        }
        return t;
    }

    private Table getTable(int id) throws  NoSuchElementException{
        Table t = id2table.get(id);
        if( null == t){
            throw new NoSuchElementException("the table doesn't exist");
        }
        return t;
    }
    /**
     * Return the id of the table with a specified name,
     * @throws NoSuchElementException if the table doesn't exist
     */
    public int getTableId(String name) throws NoSuchElementException {
        // some code goes here
        Table t = getTable(name);

        int id = -1;
        for(Map.Entry<Integer, Table> entry : id2table.entrySet()){
            if ( t == entry.getValue()){

                id = entry.getKey().intValue();
                break;
            }
        }
        return id;
    }

    /**
     * Returns the tuple descriptor (schema) of the specified table
     * @param tableid The id of the table, as specified by the DbFile.getId()
     *     function passed to addTable
     * @throws NoSuchElementException if the table doesn't exist
     */
    public TupleDesc getTupleDesc(int tableid) throws NoSuchElementException {
        // some code goes here
        Table t = id2table.get(tableid);
        if( null == t){
            throw new NoSuchElementException("the table doesn't exist");
        }

        return t.get_file().getTupleDesc();
    }

    /**
     * Returns the DbFile that can be used to read the contents of the
     * specified table.
     * @param tableid The id of the table, as specified by the DbFile.getId()
     *     function passed to addTable
     */
    public DbFile getDbFile(int tableid) throws NoSuchElementException {
        // some code goes here
        Table t = getTable(tableid);

        return t.get_file();
    }
    public DbFile getDbFile(Table table) throws NoSuchElementException{
        return table.get_file();
    }

    public String getPrimaryKey(int tableid) {
        // some code goes here
        Table t = getTable(tableid);

        return t.get_pkey();
    }

    public Iterator<Integer> tableIdIterator() {
        // some code goes here

        return id2table.keySet().iterator();
    }

    public String getTableName(int id) {
        // some code goes here

        Table t = getTable(id);

        return t.get_name();
    }
    
    /** Delete all tables from the catalog */
    public void clear() {
        // some code goes here
        // 这里的方法是仅仅清空这个类的tables呢 还是不仅如此，再删除本地的DbFile呢
        name2table.clear();
        id2table.clear();
    }
    
    /**
     * Reads the schema from a file and creates the appropriate tables in the database.
     * @param catalogFile
     */
    public void loadSchema(String catalogFile) {
        String line = "";
        String baseFolder=new File(catalogFile).getParent();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(catalogFile)));
            
            while ((line = br.readLine()) != null) {
                //assume line is of the format name (field type, field type, ...)
                String name = line.substring(0, line.indexOf("(")).trim();
                //System.out.println("TABLE NAME: " + name);
                String fields = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                String[] els = fields.split(",");
                ArrayList<String> names = new ArrayList<String>();
                ArrayList<Type> types = new ArrayList<Type>();
                String primaryKey = "";
                for (String e : els) {
                    String[] els2 = e.trim().split(" ");
                    names.add(els2[0].trim());
                    if (els2[1].trim().toLowerCase().equals("int"))
                        types.add(Type.INT_TYPE);
                    else if (els2[1].trim().toLowerCase().equals("string"))
                        types.add(Type.STRING_TYPE);
                    else {
                        System.out.println("Unknown type " + els2[1]);
                        System.exit(0);
                    }
                    if (els2.length == 3) {
                        if (els2[2].trim().equals("pk"))
                            primaryKey = els2[0].trim();
                        else {
                            System.out.println("Unknown annotation " + els2[2]);
                            System.exit(0);
                        }
                    }
                }
                Type[] typeAr = types.toArray(new Type[0]);
                String[] namesAr = names.toArray(new String[0]);
                TupleDesc t = new TupleDesc(typeAr, namesAr);
                HeapFile tabHf = new HeapFile(new File(baseFolder+"/"+name + ".dat"), t);
                addTable(tabHf,name,primaryKey);
                System.out.println("Added table : " + name + " with schema " + t);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IndexOutOfBoundsException e) {
            System.out.println ("Invalid catalog entry : " + line);
            System.exit(0);
        }
    }
}

