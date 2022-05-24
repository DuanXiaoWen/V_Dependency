package toolWindow.LocalToolWindow.entity;

import com.intellij.ide.util.PropertiesComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


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

    public void setNodeB(Node nodeB) {
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

    public Edge(Node nodeA,Node nodeB,List<Access> accessList,String _id){
        this.nodeA=nodeA;
        this.nodeB=nodeB;
        this.accessList=accessList;
        this._id=_id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(_id, edge._id) && nodeA.equals(edge.nodeA) && nodeB.equals(edge.nodeB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, nodeA, nodeB);
    }
}
