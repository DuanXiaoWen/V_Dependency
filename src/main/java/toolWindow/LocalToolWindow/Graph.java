package toolWindow.LocalToolWindow;

import com.intellij.openapi.project.Project;
import toolWindow.LocalToolWindow.entity.Edge;
import toolWindow.LocalToolWindow.entity.Node;
import util.PsiUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {


    private  Map<String ,Node> idNodeMap = new HashMap<>();
    private  Set<Edge> edges = new HashSet<>();
//

    public Graph(){
    }

    public Graph(Map<String ,Node> idNodeMap,Set<Edge> edges){
        this.idNodeMap=idNodeMap;
        this.edges=edges;
    }

    private void setNodeId(Node node) {
        String nodeId = getNodeHash(node.getClassNameAndMethodName());
        node.set_id(nodeId);
    }

//


    public void addEdge(Project project,Edge edge){
        Node nodeA=edge.getNodeA();
        Node nodeB=edge.getNodeB();
        setNodeId(nodeA);
        setNodeId(nodeB);
        String edgeId = getEdgeHash(nodeA.get_id(), nodeB.get_id());
        edge.set_id(edgeId);
        PsiUtils.matchNodeAndPsiClass(project, nodeA);
        if(idNodeMap.containsKey(nodeA.get_id())){
            nodeA=idNodeMap.get(nodeA.get_id());
            nodeA.addOutEdge(edge);
            edge.setNodeA(nodeA);
        }else {
            nodeA.addOutEdge(edge);
            idNodeMap.put(nodeA.get_id(),nodeA);
        }
        PsiUtils.matchNodeAndPsiClass(project,nodeB);

        if(idNodeMap.containsKey(nodeB.get_id())){
            nodeB=idNodeMap.get(nodeB.get_id());
            nodeB.addInEdge(edge);
            edge.setNodeB(nodeB);
        }else {

            nodeB.addInEdge(edge);
            idNodeMap.put(nodeB.get_id(),nodeB);
        }
        edges.add(edge);
    }



    public Map<String, Node> getIdNodeMap() {
        return idNodeMap;
    }

    private String getNodeHash(String classField){
        return String.valueOf(classField.hashCode());
    }

    private String getEdgeHash(String nodeA_id, String nodeB_id){
        return nodeA_id+"-"+nodeB_id;
    }


    public Set<Node> getNodes() {
        return new HashSet<>(idNodeMap.values());
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public Set<Graph> getSubGraphs(){
        Set<Node> visitedNodes=new HashSet<>();
        return  this.getNodes().stream().map(e->traverseBfs(e,visitedNodes))
                .filter(e->!e.isEmpty())
                .map( e->{
                    Map<String,Node> subIdNodeMap=new HashMap<>();
                    idNodeMap.forEach((k,v)->{
                        if(e.contains(v)){
                            subIdNodeMap.put(k,v);
                        }
                    });
                    Set<Edge> subEdges=edges.stream().filter(it->e.contains(it.getNodeA()) ||
                            e.contains(it.getNodeB())).collect(Collectors.toSet());
                    return new Graph(subIdNodeMap,subEdges);
                }).collect(Collectors.toSet());
    }


    private Set<Node> traverseBfs(Node root,Set<Node> visitedNodes){
        if (visitedNodes.contains(root)) {
            return Collections.emptySet();
        }
        Set<Node> path=new HashSet<>();
        Set<Node> quene=new HashSet<>();
        quene.add(root);
        while (!quene.isEmpty()) {
            visitedNodes.addAll(quene);
            path.addAll(quene);
            List<Node> newQuene= quene.stream().flatMap(e->
                            e.getNeighbors().stream()
                    ).
                    filter(e->
                            !visitedNodes.contains(e)
                    ).
                    collect(Collectors.toList());
            quene.clear();
            quene.addAll(newQuene);

        }
        return path;
    }
}
