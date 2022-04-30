package toolWindow.entity;

import java.util.HashMap;
import java.util.Map;

public class Edge {
    String _id;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getMethod1() {
        return method1;
    }

    public void setMethod1(String method1) {
        this.method1 = method1;
    }

    public String getMethod2() {
        return method2;
    }

    public void setMethod2(String method2) {
        this.method2 = method2;
    }

    public Map<String, Access> getAccessList() {
        return accessList;
    }


    String method1;
    String method2;
    Map<String, Access> accessList;

    public  void addSA(String key,Access access){
        this.accessList.put(key, access);
    }

    public Edge() {
        this.method1 = "";
        this.method2 = "";
        this.accessList = new HashMap<>();
    }
}
