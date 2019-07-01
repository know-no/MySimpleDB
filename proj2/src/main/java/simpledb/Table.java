package simpledb;

/**
 * author : lx
 * date : 19-4-21 下午11:54
 */
public class Table {

    public static void main(String[] args) {

    }

    /**
     * 不设置set方法，因为表一旦新建就不能修改（目前设置)
     */
    private DbFile file;
    private String name;
    private String pkeyField;

    public Table(DbFile file, String name, String pkeyField){
        this.file=file;
        this.name=name;
        this.pkeyField=pkeyField;
    }

    public DbFile get_file(){
        return this.file;
    }

    public String get_name(){
        return this.name;
    }
    public String get_pkey(){
        return this.pkeyField;
    }


}
