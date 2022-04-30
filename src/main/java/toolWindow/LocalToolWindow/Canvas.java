package toolWindow.LocalToolWindow;


import com.intellij.openapi.wm.ToolWindow;
import toolWindow.entity.Edge;
import toolWindow.entity.Node;
import util.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Canvas extends JPanel {
    private final Point2D.Float defaultCameraOrigin = new Point2D.Float(0f, 0f);
    private Point2D.Float cameraOrigin = new Point2D.Float(defaultCameraOrigin.x, defaultCameraOrigin.y);
    private final float defaultZoomRatio = 1f;
    private Point2D.Float zoomRatio = new Point2D.Float(defaultZoomRatio, defaultZoomRatio);
    private float nodeRadius = 5f;
    private float regularLineWidth = 1f;
    private BasicStroke solidLineStroke = new BasicStroke(regularLineWidth);
    private Set<Node> visibleNodes = new HashSet<>();
    private Set<Edge> visibleEdges = new HashSet<>();
    private Map<Shape, Node> nodeShapesMap = new HashMap<>();
    private Graph graph = new Graph();
    private Node hoveredNode = null;
    private LocalToolWindow localToolWindow = null;

    public Canvas(LocalToolWindow localToolWindow) {
        this.localToolWindow=localToolWindow;
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        // set up the drawing panel
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // fill the background for entire canvas
        graphics2D.setColor(Colors.BACKGROUND_COLOR.color);
        graphics2D.fillRect(0, 0, this.getWidth(), this.getHeight());


        // draw un-highlighted edgesMap

        for(Edge edge:visibleEdges){
            if(!isNodeHighlighted(edge.getMethod1()))
        }
        this.visibleEdges.
                .filter {
            !isNodeHighlighted(it.sourceNode) && !isNodeHighlighted(it.targetNode)
        }
                .forEach {
            drawNonLoopEdge(graphics2D, it, Colors.UN_HIGHLIGHTED_COLOR.color)
        }

        // draw upstream/downstream edgesMap
        val highlightedNodes = this.visibleNodes.filter {
            isNodeHighlighted(it)
        }.toSet()
        val upstreamEdges = highlightedNodes.flatMap {
            it.inEdges.values
        }.toSet()
        val downstreamEdges = highlightedNodes.flatMap {
            it.outEdges.values
        }.toSet()
        upstreamEdges.forEach {
            drawNonLoopEdge(graphics2D, it, Colors.UPSTREAM_COLOR.color)
        }
        downstreamEdges.forEach {
            drawNonLoopEdge(graphics2D, it, Colors.DOWNSTREAM_COLOR.color)
        }

        // draw un-highlighted labels
        val upstreamNodes = upstreamEdges.map {
            it.sourceNode
        }.toSet()
        val downstreamNodes = downstreamEdges.map {
            it.targetNode
        }.toSet()
        val unHighlightedNodes = this.visibleNodes
                .filter {
            !isNodeHighlighted(it) && !upstreamNodes.contains(it) && !downstreamNodes.contains(it)
        }
                .toSet()
        unHighlightedNodes.forEach {
            drawNodeLabels(graphics2D, it, Colors.NEUTRAL_COLOR.color, false)
        }

        // draw un-highlighted nodesMap (upstream/downstream nodesMap are excluded)
        this.nodeShapesMap.clear()
        unHighlightedNodes
                .filter {
            !upstreamNodes.contains(it) && !downstreamNodes.contains(it)
        }
                .forEach {
            drawNode(graphics2D, it, Colors.UN_HIGHLIGHTED_COLOR.color)
        }

        // draw upstream/downstream label and nodesMap
        upstreamNodes.forEach {
            drawNodeLabels(graphics2D, it, Colors.UPSTREAM_COLOR.color, false)
        }
        downstreamNodes.forEach {
            drawNodeLabels(graphics2D, it, Colors.DOWNSTREAM_COLOR.color, false)
        }
        upstreamNodes.forEach {
            drawNode(graphics2D, it, Colors.UPSTREAM_COLOR.color)
        }
        downstreamNodes.forEach {
            drawNode(graphics2D, it, Colors.DOWNSTREAM_COLOR.color)
        }

        // draw highlighted node and label
        this.visibleNodes
                .filter {
            isNodeHighlighted(it)
        }
                .forEach {
            drawNode(graphics2D, it, Colors.HIGHLIGHTED_COLOR.color)
            drawNodeLabels(graphics2D, it, Colors.HIGHLIGHTED_COLOR.color, true)
        }

        // draw legend
        if (this.seeMoreToolWindow.isLegendNeeded()) {
            val legend = if (this.seeMoreToolWindow.isNodeColorByAccess()) {
                listOf(PsiModifier.PUBLIC, PsiModifier.PROTECTED, PsiModifier.PACKAGE_LOCAL, PsiModifier.PRIVATE)
                        .map {
                    this.methodAccessLabelMap.getValue(it) to this.methodAccessColorMap.getValue(it)
                }
            } else {
                emptyList()
            }
            drawLegend(graphics2D, legend)
        }

    }


    private boolean isNodeHighlighted(Node node){
        return this.hoveredNode .equals(node) || this.localToolWindow.isFocusedMethod(node.method);
    }

}