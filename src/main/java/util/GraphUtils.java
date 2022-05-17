package util;

import com.google.common.collect.Maps;

import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import toolWindow.LocalToolWindow.Graph;
import toolWindow.LocalToolWindow.entity.BlueprintTuple;
import toolWindow.LocalToolWindow.entity.Edge;
import toolWindow.LocalToolWindow.entity.Node;

import java.awt.geom.Point2D;
import java.util.*;

import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;


public class GraphUtils {
    private static final float normalizedGridSize = 0.1f;

    public static void layout(Graph graph) {
        // get sub graphs from the graph, and render each part separately
        List<Map<String, Point2D.Float>> subGraphBlueprints = graph.getSubGraphs().stream()
                .map(GraphUtils::getLayoutFromGraphViz)
                .map(GraphUtils::normalizeBlueprintGridSize)
                .collect(Collectors.toList());


        // merge all sub graphs to a single graph, then adjust node coordinates so they fit in the view
        Map<String, Point2D.Float> mergedBlueprint = mergeNormalizedLayouts(subGraphBlueprints);
        applyRawLayoutBlueprintToGraph(mergedBlueprint, graph);
        applyLayoutBlueprintToGraph(mergedBlueprint, graph);
    }

    private static void applyRawLayoutBlueprintToGraph(Map<String, Point2D.Float> blueprint, Graph graph){
        blueprint.forEach((nodeId,point)->graph.getIdNodeMap().get(nodeId).getRawLayoutPoint().setLocation(point));
    }
    public static void applyLayoutBlueprintToGraph(Map<String, Point2D.Float> blueprint, Graph graph) {
        blueprint.forEach ( (nodeId, point) -> graph.getIdNodeMap().get(nodeId).getPoint().setLocation(point) );
    }

    private static Map<String, Point2D.Float> mergeNormalizedLayouts(List<Map<String, Point2D.Float>> blueprints){
        if (blueprints.isEmpty()) {
            return new HashMap<>();
        }


        List<BlueprintTuple> blueprintTuples = blueprints.stream()
                .map(blueprint -> {
                    List<Float> xPoints = blueprint.values().stream().map(e -> e.x).collect(Collectors.toList());
                    List<Float> yPoints = blueprint.values().stream().map(e -> e.y).collect(Collectors.toList());
                    float maxOfXPoints = Collections.max(xPoints);
                    float maxOfYPoints = Collections.max(yPoints);
                    float minOfXPoints = Collections.min(xPoints);
                    float minOfYPoints = Collections.min(yPoints);
                    Point2D.Float max = new Point2D.Float(maxOfXPoints, maxOfYPoints);
                    Point2D.Float min = new Point2D.Float(minOfXPoints, minOfYPoints);

                    float width = max.x - min.x + normalizedGridSize;
                    float height = max.y - min.y + normalizedGridSize;
                    return new BlueprintTuple(blueprint, height, width);

                })
                .collect(Collectors.toList());

        List<Float> sortedHeights = blueprintTuples.stream()
                .map(BlueprintTuple::getHeight)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        List<Map<String, Point2D.Float>> sortedBlueprints = blueprintTuples.stream()
                .sorted(Comparator.comparing((BlueprintTuple bp) -> -bp.getHeight())
                        .thenComparing((BlueprintTuple bp) -> -bp.getWidth()))
                .map(BlueprintTuple::getBlueprint)
                .collect(Collectors.toList());

        Point2D.Float baseline = new Point2D.Float(0.5f, 0.5f);
        // put the left-most point of the first sub-graph in the view center, by using its y value as central line
        float  yCentralLine = sortedBlueprints.get(0).values().stream()
                .min((o1, o2) -> (int) (o1.x-o2.x))
                .orElseGet(() -> new Point2D.Float(0f,0f))
                .y;

        float yOffset=0;
        
        for(int i = 0; i<sortedBlueprints.size(); i++){
            // calculate the y-offset of this sub-graph (by summing up all the height of previous sub-graphs)
            if(i>0)
                yOffset+=sortedHeights.get(i-1);

            // left align the graph by the left-most nodesMap, then centering the baseline
            Float minX =sortedBlueprints.get(i).values().stream()
                    .map(it->it.x)
                    .min(((o1, o2) -> (int) (o1-o2)))
                    .orElseGet(() -> 0f);

            //noinspection Unnecessary LocalVariable
            float finalYOffset = yOffset;
            sortedBlueprints.set(i,Maps.transformValues(sortedBlueprints.get(i), aFloat -> new Point2D.Float(
                    aFloat.x-minX+baseline.x,
                    aFloat.y+ finalYOffset -yCentralLine+baseline.y)));

        }

        Map<String, Point2D.Float> res=new HashMap<>();
        sortedBlueprints.forEach(res::putAll);
        return res;

    }
    private static Point2D.Float getGridSize(Map<String, Point2D.Float> blueprint){
        int precisionFactor = 1000;
        Set<Integer> xUniqueValues = blueprint.values().stream()
                .map(e -> Math.round(precisionFactor * e.x))
                .collect(Collectors.toSet());
        Set<Integer> yUniqueValues = blueprint.values().stream()
                .map(e -> Math.round(precisionFactor * e.y))
                .collect(Collectors.toSet());

        return new Point2D.Float(
                getAverageElementDifference(xUniqueValues) / precisionFactor,
                getAverageElementDifference(yUniqueValues) / precisionFactor
        );
    }
    private static float getAverageElementDifference(Set<Integer> elements){
        int max=Collections.max(elements);
        int min=Collections.min(elements);
        return  (elements.size() < 2 ) ? 0f : (float)(max - min) / (elements.size() - 1);
    }

    private static Map<String, Point2D.Float> getLayoutFromGraphViz(Graph graph){
        Map<String ,Point2D.Float> res=new HashMap<>();
        // if graph only has one node, just set its coordinate to (0.5, 0.5), no need to call GraphViz
        if (graph.getNodes().size() == 1) {
            for(Node node:graph.getNodes()){
                res.put(node.get_id(),new Point2D.Float(0.5f,0.5f));
            }
            return res;
        }
        // construct the GraphViz graph
        MutableGraph gvGraph = mutGraph("test")
                .graphAttrs()
                .add(RankDir.LEFT_TO_RIGHT);


        graph.getNodes().stream().sorted((o1, o2) -> o1.getMethodName().compareTo(o2.getMethodName()))
                .forEach(node->{
                    MutableNode gvNode = mutNode(node.get_id());
                    node.getOutEdges().stream()
                            .map(Edge::getNodeB)
                            .sorted((o1, o2) -> o1.getMethodName().compareTo(o2.getMethodName()))
                            .forEach(it-> gvNode.addLink(it.get_id()));
                    gvGraph.add(gvNode);

        });


        // parse the GraphViz layout as a mapping from "node name" to "x-y coordinate (percent of full graph size)"
        // GraphViz doc: https://graphviz.gitlab.io/_pages/doc/info/output.html#d:plain
        String layoutRawText = Graphviz.fromGraph(gvGraph).render(Format.PLAIN).toString();
        Arrays.stream(layoutRawText.split("\n"))
                .filter(e->e.startsWith("node"))
                .map(e->e.split(" "))
                .forEach(e->res.put(e[1],new Point2D.Float(Float.parseFloat(e[2]),Float.parseFloat(e[3]))));
        return res;

    }
    private static Map<String, Point2D.Float>  normalizeBlueprintGridSize(Map<String, Point2D.Float> blueprint){
        if (blueprint.size() < 2) {
            return blueprint;
        }
        Point2D.Float gridSize = getGridSize(blueprint);
        Point2D.Float desiredGridSize = new Point2D.Float(normalizedGridSize, normalizedGridSize);
        float xFactor = (gridSize.x == 0f) ? 1f : desiredGridSize.x / gridSize.x;
        float yFactor = (gridSize.y == 0f) ? 1f : desiredGridSize.y / gridSize.y;
        return Maps.transformValues(blueprint, aFloat -> new Point2D.Float(aFloat.x*xFactor,aFloat.y*yFactor));

    }

}
