package toolWindow.entity;

import java.util.Objects;

public  class Node {
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getClassNameAndMethodName() {
        return classNameAndMethodName;
    }

    public void setClassNameAndMethodName(String classNameAndMethodName) {
        this.classNameAndMethodName = classNameAndMethodName;
    }

    String _id;
    String classNameAndMethodName;

    public Node(String classField) {
        this.classNameAndMethodName = classField;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return _id.equals(node._id) && classNameAndMethodName.equals(node.classNameAndMethodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, classNameAndMethodName);
    }
}
