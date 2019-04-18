package simpledb;

import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    public ArrayList<TDItem> getItems() {
        return items;
    }

    /**
     * 保存行中所有的数据的描述
     */
    private ArrayList<TDItem> items = new ArrayList<TDItem>();


    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {



        private static final long serialVersionUID = 1L;

        public Type getFieldType() {
            return fieldType;
        }

        public void setFieldType(Type fieldType) {
            this.fieldType = fieldType;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        /**
         * The type of the field
         * */
        Type fieldType;
        
        /**
         * The name of the field
         * */
        String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        // some code goes here
        // 直接返回容器的迭代器
        return items.iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        // some code goes here
        for(int i = 0 ; i < typeAr.length; i ++){
            TDItem td = new TDItem(typeAr[i], fieldAr[i]);
            items.add(td);
        }
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        // some code goes here
        // 保存行中所有的数据的描述
        for(int i = 0 ; i < typeAr.length; i ++){
            TDItem td = new TDItem(typeAr[i], "");
            items.add(td);
        }
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
        // 返回的是容器的大小
        return items.size();
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
        // 根据i获取fieldname
        String fieldName = null;
        try{
            TDItem td = items.get(i);
            fieldName = td.fieldName;
        }catch (NoSuchElementException e){
            throw new NoSuchElementException(i+ "  is not a valid field reference");
        }

        return fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // some code goes here
        // 根据i获取fieldtype
        Type fieldType = null;
        try{
            TDItem td = items.get(i);
            fieldType = td.fieldType;
        }catch (NoSuchElementException e){
            throw new NoSuchElementException(i+ "  is not a valid field reference");
        }

        return fieldType;
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // some code goes here
        // 在items中查找数据的描述
        int index = -1;
            for(TDItem td : items){
                if(td.fieldName.equals(name)){
                    index = items.indexOf(td);
                }
            }
        if(index == -1){
            throw new NoSuchElementException("no field with a matching name is found");
        }
        return index;
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // some code goes here
        // size是返回的字节数
        int size = 0 ;
        for( TDItem td : items){
            size += td.fieldType.getLen();
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // some code goes here
        int index = 0 ;
        Type[] types = new Type[td1.numFields() + td2.numFields()];
        String[] names = new String[types.length];

        for(TDItem td: td1.getItems()){
            types[index] = td.fieldType;
            names[index] = td.fieldName;
            index ++;
        }

        for(TDItem td: td2.getItems()){
            types[index] = td.fieldType;
            names[index] = td.fieldName;
            index ++;
        }

        return new TupleDesc(types, names);
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they are the same size and if the n-th
     * type in this TupleDesc is equal to the n-th type in td.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(Object o) {
        // some code goes here
        // 比较每个元素
        boolean res = true;
        if(null == o ||  !(o instanceof TupleDesc)){
            res = false;
            return res;
        }
        TupleDesc other = (TupleDesc) o;
        if( other.numFields() != this.numFields()){
            res =false;
            return res;
        }
        for(int i = 0 ; i < this.numFields(); i ++){
            if(!this.getFieldType(i).equals(other.getFieldType(i))){
                res = false;
                break;
            }

        }

        return res;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
        // some code goes here
        String descrip = "";
        for (int i =0; i< this.numFields();i++){
            descrip+=this.getFieldType(i).toString()+"("+this.getFieldName(i).toString()+")"+",";
        }


        descrip=descrip.substring(0, descrip.length() - 1);
        return descrip;

    }
}
