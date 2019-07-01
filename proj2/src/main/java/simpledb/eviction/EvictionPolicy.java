package simpledb.eviction;

/**
 * author : lx
 * date : 19-5-1 下午9:02
 */
public abstract class EvictionPolicy <a,b>{


    public EvictionPolicy(int numPages){

    }

    public abstract b getPage(a key);

    public abstract void putPage(a key, b val);
//

}
