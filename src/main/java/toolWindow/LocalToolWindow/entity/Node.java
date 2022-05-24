package toolWindow.LocalToolWindow.entity;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import util.PsiUtils;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

public  class Node{



    public void addInEdge(Edge edge){
        inEdges.add(edge);
    }
    public  void  addOutEdge(Edge edge){
        outEdges.add(edge);
    }

    public Set<Edge> getInEdges() {
        return inEdges;
    }
    public Set<Edge> getOutEdges() {
        return outEdges;
    }
    public List<Node>  getNeighbors(){

        List<Node> res=new ArrayList<>();
        for(Edge edge:inEdges){
            res.add(edge.getNodeA());
        }
        for(Edge edge:outEdges){
            res.add(edge.getNodeB());
        }
        return res;
    }

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

    public Point2D.Float getPoint() {
        return point;
    }

    public void setPoint(Point2D.Float point) {
        this.point = point;
    }


    public PsiElement getPsiElement() {
        return psiElement;
    }

    public void setPsiElement(PsiElement psiElement) {
        this.psiElement = psiElement;
        if(psiElement instanceof PsiMethod){
            methodName=((PsiMethod) psiElement).getName();
        }else if(psiElement instanceof PsiClass){
            methodName=((PsiClass) psiElement).getName()+":constructor";
        }
        signature= PsiUtils.getSignature(psiElement);
    }


    public String getPackageClassName() {
        return packageClassName;
    }

    public void setPackageClassName(String packageClassName) {
        this.packageClassName = packageClassName;
    }



    public Node(String classField) {

        this.classNameAndMethodName = classField;
    }

    public Node(String _id, String classNameAndMethodName) {

        this._id = _id;
        this.classNameAndMethodName = classNameAndMethodName;
    }
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getMethodName() {
        return methodName;
    }
    public String getSignature() {
        return signature;
    }
    public Point2D.Float getRawLayoutPoint() {
        return rawLayoutPoint;
    }

    Point2D.Float point = new Point2D.Float();
    Point2D.Float rawLayoutPoint =new  Point2D.Float();
    String signature;
    String packageClassName;
    String _id;
    String classNameAndMethodName;
    PsiElement psiElement;
    String methodName;
    String note;
    Set<Edge> inEdges=new HashSet<>();
    Set<Edge> outEdges=new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return classNameAndMethodName.equals(node.classNameAndMethodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classNameAndMethodName);
    }
}
