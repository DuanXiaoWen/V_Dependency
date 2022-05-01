package toolWindow.entity;

import java.util.LinkedList;
import java.util.List;


public class Edge {
    String _id;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Node getNodeA() {
        return nodeA;
    }

    public void setNodeA(Node nodeA) {
        this.nodeA = nodeA;
    }

    public Node getNodeB() {
        return nodeB;
    }

    public void setNodeB(Node method2) {
        this.nodeB = nodeB;
    }

    public List<Access> getAccessList() {
        return accessList;
    }


    Node nodeA;
    Node nodeB;
    List<Access> accessList;

    public  void addSA(Access access){
        this.accessList.add(access);
    }

    public Edge() {
        this.accessList = new LinkedList<>();
    }
}
