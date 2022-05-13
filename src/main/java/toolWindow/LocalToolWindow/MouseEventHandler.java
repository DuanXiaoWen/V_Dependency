package toolWindow.LocalToolWindow;

import toolWindow.LocalToolWindow.entity.Node;

import java.awt.event.*;
import java.awt.geom.Point2D;

public class MouseEventHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
    private Canvas canvas;
    private Point2D.Float lastMousePosition = new Point2D.Float();
    public  MouseEventHandler(Canvas canvas){
        this.canvas=canvas;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Node node = this.canvas.getNodeUnderPoint(e.getPoint());
        if (node == null) {
            this.canvas.clearClickedNodes();
        } else {
            this.canvas.toggleClickedNode(node);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.lastMousePosition.setLocation((float) e.getX(), (float) e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point2D.Float currentMousePosition = new  Point2D.Float((float)e.getX(), (float)e.getY());
        if (!currentMousePosition.equals(this.lastMousePosition)) {
            Point2D.Float currentCameraOrigin = this.canvas.getCameraOrigin();
            Point2D.Float newCameraOrigin = new Point2D.Float(
                    currentCameraOrigin.x - currentMousePosition.x + this.lastMousePosition.x,
                    currentCameraOrigin.y - currentMousePosition.y + this.lastMousePosition.y
            );
            this.canvas.getCameraOrigin().setLocation(newCameraOrigin);
            this.canvas.repaint();
            this.lastMousePosition.setLocation(currentMousePosition);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Node node = this.canvas.getNodeUnderPoint(e.getPoint());
        this.canvas.setHoveredNode(node);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int scrollRotation = e.getWheelRotation(); // 1 if scroll down, -1 otherwise
        Float zoomFactor = (float) Math.pow(1.25, -scrollRotation);
        Point2D.Float mousePosition = new Point2D.Float((float) e.getX(),(float) e.getY());
        this.canvas.zoomAtPoint(mousePosition, zoomFactor, zoomFactor);
    }
}
