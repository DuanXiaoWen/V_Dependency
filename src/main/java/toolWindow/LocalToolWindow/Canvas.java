package toolWindow.LocalToolWindow;

import toolWindow.LocalToolWindow.entity.Edge;
import toolWindow.LocalToolWindow.entity.Node;
import util.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import toolWindow.LocalToolWindow.entity.Label;


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
    private Graph graph;

    private Node hoveredNode = null;
    private LocalToolWindow localToolWindow;

    public Canvas(LocalToolWindow localToolWindow) {
        this.localToolWindow=localToolWindow;
    }
    public Point2D.Float getCameraOrigin() {
        return cameraOrigin;
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        // set up the drawing panel
        Graphics2D graphics2D = (Graphics2D) graphics;
        //消除线条锯齿
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // fill the background for entire canvas
        graphics2D.setColor(Colors.BACKGROUND_COLOR.color);
        graphics2D.fillRect(0, 0, this.getWidth(), this.getHeight());


        // draw un-highlighted edgesMap
        for (Edge edge : visibleEdges) {
            if (!isNodeHighlighted(edge.getNodeA()) && !isNodeHighlighted(edge.getNodeB())) {
                drawEdge(graphics2D, edge, Colors.UN_HIGHLIGHTED_COLOR.color);
            }
        }

        // draw upstream/downstream edgesMap
        Set<Node> highlightedNodes= visibleNodes.stream().filter(this::isNodeHighlighted).collect(Collectors.toSet());
        Set<Edge> upStreamEdges = highlightedNodes.stream().flatMap(e -> e.getInEdges().stream()).collect(Collectors.toSet());
        Set<Edge> downStreamEdges = highlightedNodes.stream().flatMap(e -> e.getOutEdges().stream()).collect(Collectors.toSet());
        upStreamEdges.forEach(e->drawEdge(graphics2D,e,Colors.UPSTREAM_COLOR.color));
        downStreamEdges.forEach (e-> drawEdge(graphics2D, e, Colors.UPSTREAM_COLOR.color));
        Set<Node> upStreamNodes = upStreamEdges.stream().map(Edge::getNodeA).collect(Collectors.toSet());
        Set<Node> downStreamNodes = upStreamEdges.stream().map(Edge::getNodeA).collect(Collectors.toSet());
        Set<Node> unHighlightedNodes = visibleNodes.stream().filter(it -> !isNodeHighlighted(it) && !upStreamNodes.contains(it) && !downStreamNodes.contains(it)).collect(Collectors.toSet());

        unHighlightedNodes.forEach (e->
            drawNodeLabels(graphics2D, e, Colors.NEUTRAL_COLOR.color, false)
        );

        // draw un-highlighted nodesMap (upstream/downstream nodesMap are excluded)
        this.nodeShapesMap.clear();
        for(Node node:unHighlightedNodes){
            if(!upStreamNodes.contains(node) && !downStreamNodes.contains(node)){
                drawNode(graphics2D, node, Colors.UN_HIGHLIGHTED_COLOR.color) ;
            }
        }


        // draw upstream/downstream label and nodesMap
        upStreamNodes.forEach(e->drawNodeLabels(graphics2D, e, Colors.UPSTREAM_COLOR.color, false));
        downStreamNodes.forEach(e->drawNodeLabels(graphics2D, e, Colors.UPSTREAM_COLOR.color, false));
        upStreamNodes.forEach(e->drawNode(graphics2D, e, Colors.UPSTREAM_COLOR.color));
        downStreamNodes.forEach(e->drawNode(graphics2D, e, Colors.UPSTREAM_COLOR.color));


        // draw highlighted node and label
        for(Node node:visibleNodes){
            if(isNodeHighlighted(node)){
                drawNode(graphics2D, node, Colors.HIGHLIGHTED_COLOR.color);
                drawNodeLabels(graphics2D, node, Colors.HIGHLIGHTED_COLOR.color, true);
            }
        }


    }

    public void reset(Graph graph) {
        this.graph = graph;
        this.visibleNodes.clear();
        this.visibleNodes.addAll(graph.getNodes());
        this.visibleEdges.clear();
        this.visibleEdges.addAll(graph.getEdges());
        this.nodeShapesMap.clear();
        this.hoveredNode = null;
        this.cameraOrigin.setLocation(defaultCameraOrigin);
        this.zoomRatio.setLocation(this.defaultZoomRatio, this.defaultZoomRatio);
    }

    private void drawNodeLabels(Graphics2D graphics2D, Node node, Color labelColor, Boolean isNodeHovered) {
        // create labels
        List<Label> labels = createNodeLabels(node, labelColor, isNodeHovered);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        float halfLabelHeight = 0.5f * (fontMetrics.getAscent() + fontMetrics.getDescent());
        Point2D.Float nodeCenter = toCameraView(node.getPoint());
        Point2D.Float boundingBoxLowerLeft = new Point2D.Float(
                nodeCenter.x + 4 * nodeRadius,
                nodeCenter.y + halfLabelHeight
        );
        Color backgroundColor = localToolWindow.isQueried(node.getMethodName()) ? Colors.HIGHLIGHTED_BACKGROUND_COLOR.color : Colors.BACKGROUND_COLOR.color;
        Color borderColor = isNodeHovered ? Colors.UN_HIGHLIGHTED_COLOR.color : Colors.BACKGROUND_COLOR.color;

        drawLabels(graphics2D, boundingBoxLowerLeft, labels, backgroundColor, borderColor);
    }

    private  List<Label> createNodeLabels(Node node, Color signatureColor, Boolean isNodeHovered){
        // draw labels in top-down order
        List<Label> labels=new ArrayList<>();

        // package name
        if (isNodeHovered) {
            labels.add(new Label(node.getPackageClassName(),Colors.UN_HIGHLIGHTED_COLOR.color));
        }
        // function signature
        String signature=isNodeHovered? node.getSignature() : node.getMethodName();
        labels.add(new Label(signature,signatureColor));
        return labels;
    }



    private void drawEdge(Graphics2D graphics2D, Edge edge, Color color) {
        Point2D.Float sourceNodeCenter = toCameraView(edge.getNodeA().getPoint());
        Point2D.Float targetNodeCenter = toCameraView(edge.getNodeB().getPoint());
        drawLine(graphics2D, sourceNodeCenter, targetNodeCenter, color);
//        drawLineArrow(graphics2D, sourceNodeCenter, targetNodeCenter, color)
    }
    private void drawLabels(
            Graphics2D graphics2D,
            Point2D.Float boundingBoxLowerLeft,
            List<Label> labels,
            Color backgroundColor,
            Color borderColor
    ) {
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        int singleLabelHeight = fontMetrics.getAscent() + fontMetrics.getDescent();
        int boundingBoxWidth = 0;
        for(Label label:labels){
            boundingBoxWidth=Math.max((int) fontMetrics.getStringBounds(label.getText(), graphics2D).getWidth(),boundingBoxWidth);
        }

        int boundingBoxHeight = labels.size() * singleLabelHeight;
        Point2D.Float boundingBoxUpperLeft = new Point2D.Float(
                boundingBoxLowerLeft.x,
                boundingBoxLowerLeft.y - 2 * 2 - boundingBoxHeight
        );
        Point2D.Float boundingBoxUpperRight = new Point2D.Float(
                boundingBoxUpperLeft.x + 2 * 2 + boundingBoxWidth,
                boundingBoxUpperLeft.y
        );
        Point2D.Float boundingBoxLowerRight = new Point2D.Float(
                boundingBoxUpperRight.x,
                boundingBoxLowerLeft.y
        );
        // fill background to overall bounding box
        graphics2D.setColor(backgroundColor);
        graphics2D.fillRect(
                (int)(boundingBoxUpperLeft.x + 1),
                (int) (boundingBoxUpperLeft.y + 1),
                2 * 2 + boundingBoxWidth,
                2 * 2 + boundingBoxHeight
        );
        // draw border if the node is hovered
        drawLine(graphics2D, boundingBoxLowerLeft, boundingBoxUpperLeft, borderColor);
        drawLine(graphics2D, boundingBoxUpperLeft, boundingBoxUpperRight, borderColor);
        drawLine(graphics2D, boundingBoxUpperRight, boundingBoxLowerRight, borderColor);
        drawLine(graphics2D, boundingBoxLowerRight, boundingBoxLowerLeft, borderColor);
        // draw text

        for(int i = 0; i<labels.size(); i++){
            Label curLabel=labels.get(labels.size()-1-i);
            Point2D.Float labelLowerLeft = new Point2D.Float(
                    boundingBoxLowerLeft.x + 2,
                    boundingBoxLowerLeft.y - 2 - fontMetrics.getDescent() - i * singleLabelHeight);
            drawText(graphics2D, labelLowerLeft, curLabel.getText(), curLabel.getColor());

        }

    }

    private void drawNode(Graphics2D graphics2D, Node node, Color outlineColor) {
        Point2D.Float nodeCenter = toCameraView(node.getPoint());
        Color backgroundColor = getNodeBackgroundColor(node);
        Shape nodeShape = drawCircle(graphics2D, nodeCenter, backgroundColor, outlineColor);
        this.nodeShapesMap.put(nodeShape,node);
    }



    private Color getNodeBackgroundColor( Node node){
        return Colors.BACKGROUND_COLOR.color;
    }

    private Point2D.Float toCameraView(Point2D.Float point){
        Dimension canvasSize = this.localToolWindow.getCanvasSize();
        return new Point2D.Float(
                this.zoomRatio.x * point.x * canvasSize.width - this.cameraOrigin.x,
                this.zoomRatio.y * point.y * canvasSize.height - this.cameraOrigin.y
        );
    }

    private Shape drawCircle(
            Graphics2D graphics2D,
            Point2D.Float circleCenter ,
            Color backgroundColor,
            Color outlineColor){
        // create node shape
        Point2D.Float upperLeft = new Point2D.Float(
                circleCenter.x - this.nodeRadius,
                circleCenter.y - this.nodeRadius
        );
        float diameter = 2 * this.nodeRadius;
        Ellipse2D.Float shape = new Ellipse2D.Float(
                upperLeft.x,
                upperLeft.y,
                diameter,
                diameter
        );
        // fill node with color
        graphics2D.setColor(backgroundColor);
        graphics2D.fill(shape);
        // draw the outline
        graphics2D.setColor(outlineColor);
        Shape strokedShape = this.solidLineStroke.createStrokedShape(shape);
        graphics2D.draw(strokedShape);
        return shape;
    }

    private void drawLine(
            Graphics2D graphics2D,
            Point2D.Float nodeA,
            Point2D.Float nodeB,
            Color lineColor) {
        Line2D.Float shape = new Line2D.Float(nodeA, nodeB);
        Shape strokedShape = this.solidLineStroke.createStrokedShape(shape);
        graphics2D.setColor(lineColor);
        graphics2D.draw(strokedShape);
    }
    private boolean isNodeHighlighted(Node node){
        return Objects.equals(node,hoveredNode)|| this.localToolWindow.isFocusedElement(node.getPsiElement());
    }

    public Node  getNodeUnderPoint(Point2D point) {
        for(Shape shape:nodeShapesMap.keySet()){
            if(shape.contains(point.getX(),point.getY())){
                return nodeShapesMap.get(shape);
            }
        }
        return null;
    }
    private void drawText (Graphics2D graphics2D, Point2D.Float textLowerLeft, String text, Color textColor) {
        graphics2D.setColor(textColor);
        graphics2D.drawString(text, textLowerLeft.x, textLowerLeft.y);
    }

    public void clearClickedNodes() {
        this.localToolWindow.clearFocusedMethods();
        repaint();
    }
    public void toggleClickedNode(Node node) {
        this.localToolWindow.toggleFocusedMethod(node.getPsiElement());
        repaint();
    }

    public Canvas setHoveredNode(Node node){
        if (this.hoveredNode != node) {
            this.hoveredNode = node;
            repaint();
        }
        return this;
    }

    public void zoomAtPoint(Point2D.Float point, Float xZoomFactor, Float yZoomFactor)  {
        this.cameraOrigin.setLocation(
                xZoomFactor * this.cameraOrigin.x + (xZoomFactor - 1) * point.x,
                yZoomFactor * this.cameraOrigin.y + (yZoomFactor - 1) * point.y
        );
        this.zoomRatio.x *= xZoomFactor;
        this.zoomRatio.y *= yZoomFactor;
        repaint();
    }

}