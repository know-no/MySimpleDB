package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {
    private File file;
    private TupleDesc tupleDesc;

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.file = f;
        this.tupleDesc = td;

    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here

        return file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here

        return file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.tupleDesc;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        Page page = null;
        int pgno = pid.pageNumber();
        int startB = pgno * BufferPool.PAGE_SIZE;

        byte[] data = new byte[BufferPool.PAGE_SIZE];
        try(RandomAccessFile rmf = new RandomAccessFile(getFile(), "r")){
            rmf.seek(startB);
            rmf.read(data,0,data.length);
//            System.out.println(java.util.Arrays.toString(data));
            page = new HeapPage((HeapPageId) pid,data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return page;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for proj1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        // page 是固定大小
        int numPages =(int) file.length() / BufferPool.PAGE_SIZE;
        return numPages;
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for proj1
    }

    // see DbFile.java for javadocs
    public Page deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for proj1
    }

    // see DbFile.java for javadocs, 根据
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new HeapFileDbFileIterator(tid);
    }

    private class HeapFileDbFileIterator implements DbFileIterator {
        boolean flag = false;
        Page page = null;
        TransactionId transactionId;
        int pgNo = 0;
        Iterator<Tuple> tupleIteratorInHeapPage = null;


        public HeapFileDbFileIterator(TransactionId transactionId) {
            this.transactionId =transactionId;
        }

        private Iterator<Tuple> getTupleIteratorInHeapPage (PageId pid) throws DbException, TransactionAbortedException{
            return Database.getBufferPool().getPage(transactionId, pid, Permissions.READ_ONLY).iterator();
        }

        @Override
        public void open() throws DbException, TransactionAbortedException {
            flag = true;
            pgNo = 0;
            HeapPageId pid = new HeapPageId(getId(), pgNo);
            tupleIteratorInHeapPage = getTupleIteratorInHeapPage(pid);
        }

        /**
         * @return
         * @throws DbException
         * @throws TransactionAbortedException
         */
        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {

           if(tupleIteratorInHeapPage == null) return false;
           if(tupleIteratorInHeapPage.hasNext()) return true;
           ++ pgNo;// 当前页加 1 , 当前file有多少页?

            while(pgNo < numPages()){
                tupleIteratorInHeapPage = getTupleIteratorInHeapPage(new HeapPageId(getId(), pgNo));
                if(tupleIteratorInHeapPage != null && tupleIteratorInHeapPage.hasNext()) return  true;
                ++ pgNo;

            }

            return false;
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if(flag) return tupleIteratorInHeapPage.next();
            throw new NoSuchElementException();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            open();
        }

        @Override
        public void close() {
            // open和close 是指打开（保存状态） 还是重新打开，如果是重新打开那么和rewind有什么区别?
            // 进过测试发现 是前者，具有状态的
            flag = false;
//            pgNo = 0;
//            tupleIteratorInHeapPage = null;
        }
    }

}

