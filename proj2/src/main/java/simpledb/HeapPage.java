package simpledb;

import javax.swing.text.html.HTMLDocument;
import java.math.BigInteger;
import java.util.*;
import java.io.*;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

/**
 * Each instance of HeapPage stores data for one page of HeapFiles and 
 * implements the Page interface that is used by BufferPool.
 *
 * @see HeapFile
 * @see BufferPool
 *
 */
public class HeapPage implements Page {

    HeapPageId pid;
    TupleDesc td;
    byte header[];
    Tuple tuples[];
    int numSlots;
    byte[] oldData;



    /**
     * Create a HeapPage from a set of bytes of data read from disk.
     * The format of a HeapPage is a set of header bytes indicating
     * the slots of the page that are in use, some number of tuple slots.
     *  Specifically, the number of tuples is equal to: <p>
     *          floor((BufferPool.PAGE_SIZE*8) / (tuple size * 8 + 1))
     * <p> where tuple size is the size of tuples in this
     * database table, which can be determined via {@link Catalog#getTupleDesc}.
     * The number of 8-bit header words is equal to:
     * <p>
     *      ceiling(no. tuple slots / 8)
     * <p>
     * @see Database#getCatalog
     * @see Catalog#getTupleDesc
     * @see BufferPool#PAGE_SIZE
     */
    public HeapPage(HeapPageId id, byte[] data) throws IOException {
        this.pid = id;
        this.td = Database.getCatalog().getTupleDesc(id.getTableId());
        // 计算每个页面拥有的总slots数量

        this.numSlots = getNumTuples();

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

        // allocate and read the header slots of this page
        header = new byte[getHeaderSize() ];
        for (int i=0; i<header.length; i++)
            header[i] = dis.readByte();
//        System.out.println(java.util.Arrays.toString(header) +"      header :" + header.length);
        try{
            // allocate and read the actual records of this page
            tuples = new Tuple[numSlots];
//            System.out.println("numSlots" + numSlots);
            for (int i=0; i<tuples.length; i++)
                tuples[i] = readNextTuple(dis,i);
        }catch(NoSuchElementException e){
            e.printStackTrace();
        }
        dis.close();

        setBeforeImage();
    }

    /** Retrieve the number of tuples on this page.
        @return the number of tuples on this page
    */
    private int  getNumTuples() {
        // some code goes here
        // 我认为这里的numTuples就是文档里说的tupsPerPage
        return  (int) floor((BufferPool.PAGE_SIZE * 8) / (td.getSize() * 8 + 1));


    }

    /**
     * Computes the number of bytes in the header of a page in a HeapFile with each tuple occupying tupleSize bytes
     * @return the number of bytes in the header of a page in a HeapFile with each tuple occupying tupleSize bytes
     */
    private int getHeaderSize() {

        // some code goes here
        // 为了节约空间 ， 用了类似于bitmap排序的那个方法，用每一位表示一个slot是否被使用
        // !! 这里必须是8.0 如果是8 则 15 / 8 = 1, 15/8.0 = 1.7 ceil(1.7)=2
        return (int)ceil(this.numSlots/8.0);

    }

    /** Return a view of this page before it was modified
        -- used by recovery */
    public HeapPage getBeforeImage(){
        try {
            return new HeapPage(pid,oldData);
        } catch (IOException e) {
            e.printStackTrace();
            //should never happen -- we parsed it OK before!
            System.exit(1);
        }
        return null;
    }
    
    public void setBeforeImage() {
        oldData = getPageData().clone();
    }

    /**
     * @return the PageId associated with this page.
     */
    public HeapPageId getId() {
    // some code goes here

        return this.pid;
//    throw new UnsupportedOperationException("implement this");
    }

    /**
     * Suck up tuples from the source file.
     */
    private Tuple readNextTuple(DataInputStream dis, int slotId) throws NoSuchElementException {
        // if associated bit is not set, read forward to the next tuple, and
        // return null.
        if (!isSlotUsed(slotId)) {

            for (int i=0; i<td.getSize(); i++) {
                try {
                    dis.readByte();
                } catch (IOException e) {
                    throw new NoSuchElementException("error reading empty tuple");
                }
            }
            return null;
        }

        // read fields in the tuple
        Tuple t = new Tuple(td);
        RecordId rid = new RecordId(pid, slotId);
        t.setRecordId(rid);
        try {
            for (int j=0; j<td.numFields(); j++) {
                Field f = td.getFieldType(j).parse(dis);
                t.setField(j, f);
            }
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            throw new NoSuchElementException("parsing error!");
        }

        return t;
    }

    /**
     * Generates a byte array representing the contents of this page.
     * Used to serialize this page to disk.
     * <p>
     * The invariant here is that it should be possible to pass the byte
     * array generated by getPageData to the HeapPage constructor and
     * have it produce an identical HeapPage object.
     *
     * @see #HeapPage
     * @return A byte array correspond to the bytes of this page.
     */
    public byte[] getPageData() {
        int len = BufferPool.PAGE_SIZE;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
        DataOutputStream dos = new DataOutputStream(baos);

        // create the header of the page
        for (int i=0; i<header.length; i++) {
            try {
                dos.writeByte(header[i]);
            } catch (IOException e) {
                // this really shouldn't happen
                e.printStackTrace();
            }
        }

        // create the tuples
        for (int i=0; i<tuples.length; i++) {

            // empty slot
            if (!isSlotUsed(i)) {
                for (int j=0; j<td.getSize(); j++) {
                    try {
                        dos.writeByte(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                continue;
            }

            // non-empty slot
            for (int j=0; j<td.numFields(); j++) {
                Field f = tuples[i].getField(j);
                try {
                    f.serialize(dos);
                
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // padding
        int zerolen = BufferPool.PAGE_SIZE - (header.length + td.getSize() * tuples.length); //- numSlots * td.getSize();
        byte[] zeroes = new byte[zerolen];
        try {
            dos.write(zeroes, 0, zerolen);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    /**
     * Static method to generate a byte array corresponding to an empty
     * HeapPage.
     * Used to add new, empty pages to the file. Passing the results of
     * this method to the HeapPage constructor will create a HeapPage with
     * no valid tuples in it.
     *
     * @return The returned ByteArray.
     */
    public static byte[] createEmptyPageData() {
        int len = BufferPool.PAGE_SIZE;
        return new byte[len]; //all 0
    }

    /**
     * Delete the specified tuple from the page;  the tuple should be updated to reflect
     *   that it is no longer stored on any page.
     * @throws DbException if this tuple is not on this page, or tuple slot is
     *         already empty.
     * @param t The tuple to delete
     */
    public void deleteTuple(Tuple t) throws DbException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Adds the specified tuple to the page;  the tuple should be updated to reflect
     *  that it is now stored on this page.
     * @throws DbException if the page is full (no empty slots) or tupledesc
     *         is mismatch.
     * @param t The tuple to add.
     */
    public void insertTuple(Tuple t) throws DbException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Marks this page as dirty/not dirty and record that transaction
     * that did the dirtying
     */
    public void markDirty(boolean dirty, TransactionId tid) {
        // some code goes here
	// not necessary for lab1
    }

    /**
     * Returns the tid of the transaction that last dirtied this page, or null if the page is not dirty
     */
    public TransactionId isDirty() {
        // some code goes here
	// Not necessary for lab1
        return null;      
    }

    /**
     * Returns the number of empty slots on this page.
     */
    public int getNumEmptySlots() {
        // some code goes here
        int numEmptySlots = 0 ;
        for(int i = 0 ; i < this.header.length * 8 ;++ i ){
            if( ! isSlotUsed(i)){
               ++ numEmptySlots;
            }
        }

        return numEmptySlots;
    }

    /**
     * Returns true if associated slot on this page is filled.
     */
    public boolean isSlotUsed(int i) throws IllegalArgumentException {
        // some code goes here
        if(i < 0 || i > numSlots){
            throw new IllegalArgumentException("Wrong arguments");
        }
        boolean flag = false;

        int whichByte = i / 8 ;
        int whichBit = i % 8 ;
        if(whichByte >= this.header.length){
            throw new IllegalArgumentException("Wrong arguments" + whichByte + "  " + whichBit);
//            return true;
        }
        /**
         *使用移位判断这个位置是否被占用，若是1，移位之后整个数是负数，若是0，整个数是正数 !! (byte)进行强转 否则是int，变成正数了,
         * (byte)(header[whichByte] << ( 7- whichBit)) < 0;
         * 如下
         * int t = (header[whichByte] << ( 7- whichBit));
         *  System.out.println("int : " + (t < 0) + " byte: " + ((byte)(header[whichByte] << ( 7- whichBit))<0));
         *  因为byte 在打印的时候会自动升级为int
         *
         *  或者右移两位，看最后移位是否是1
         * ((header[whichByte] >> (  whichBit)) & 1) == 1;
         *
         * 或者转化为BigInteger 然后用testbit的方法
         */

        return ((header[whichByte] >> (  whichBit)) & 1) == 1;


    }

    /**
     * Abstraction to fill or clear a slot on this page.
     */
    private void markSlotUsed(int i, boolean value) {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * @return an iterator over all tuples on this page (calling remove on this iterator throws an UnsupportedOperationException)
     * (note that this iterator shouldn't return tuples in empty slots!)
     */
    public Iterator<Tuple> iterator() {
        // some code goes here
        // tuples数组中保存着所有tuple 包括null

        return new TupleIteratorInHeapPage();
    }

    class TupleIteratorInHeapPage implements Iterator<Tuple> {
        int index = 0;
        public boolean hasNext() {
            if(index < numSlots){

                for(int pos = index; pos < numSlots ; ++ pos){
                    if(isSlotUsed(pos)){
                        return true;
                    }
                }

            }
            return  false;
        }

        public Tuple next() {
            Tuple res = null;
            while( ! isSlotUsed(index)){
                ++ index;
            }
            if(index >= numSlots){
                throw new IllegalStateException();
            }
            return tuples[ index ++];
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


}

