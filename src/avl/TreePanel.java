package avl;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TreePanel extends JPanel {

    private AVLTree tree;
    private Set<Integer> highlightPath = new HashSet<>();
    private int searchTarget = -1;
    private int animStep = -1; // current animated node index in path

    public TreePanel(AVLTree tree) {
        this.tree = tree;
        setBackground(new Color(18, 18, 28));
    }

    public void setHighlightPath(List<Integer> path, int target, int step) {
        this.highlightPath = new HashSet<>(path.subList(0, Math.min(step + 1, path.size())));
        this.searchTarget = target;
        this.animStep = step;
        repaint();
    }

    public void clearHighlight() {
        highlightPath.clear();
        searchTarget = -1;
        animStep = -1;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (tree.root == null) {
            g2.setColor(new Color(120, 120, 150));
            g2.setFont(new Font("Consolas", Font.ITALIC, 16));
            g2.drawString("Árbol vacío — inserte un valor", getWidth() / 2 - 130, getHeight() / 2);
            return;
        }

        int treeH = tree.getHeight();
        int levelGap = Math.max(60, (getHeight() - 40) / (treeH + 1));
        int startX = getWidth() / 2;

        drawNode(g2, tree.root, startX, 40, getWidth() / 4, levelGap);
    }

    private void drawNode(Graphics2D g2, AVLNode node, int x, int y, int xOffset, int levelGap) {
        if (node == null) return;

        int childY = y + levelGap;

        if (node.left != null) {
            int childX = x - xOffset;
            g2.setColor(new Color(80, 100, 140));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x, y, childX, childY);
            drawNode(g2, node.left, childX, childY, Math.max(20, xOffset / 2), levelGap);
        }
        if (node.right != null) {
            int childX = x + xOffset;
            g2.setColor(new Color(80, 100, 140));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x, y, childX, childY);
            drawNode(g2, node.right, childX, childY, Math.max(20, xOffset / 2), levelGap);
        }

        int r = 22;
        boolean isHighlighted = highlightPath.contains(node.value);
        boolean isTarget = (node.value == searchTarget);

        Color fillColor;
        if (isTarget) {
            fillColor = new Color(50, 200, 100);
        } else if (isHighlighted) {
            fillColor = new Color(220, 150, 50);
        } else {
            fillColor = new Color(60, 80, 140);
        }

        // Shadow
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(x - r + 3, y - r + 3, r * 2, r * 2);

        // Circle
        g2.setColor(fillColor);
        g2.fillOval(x - r, y - r, r * 2, r * 2);

        // Border
        g2.setColor(isHighlighted ? new Color(255, 200, 80) : new Color(100, 140, 200));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x - r, y - r, r * 2, r * 2);

        // Value
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 13));
        String val = String.valueOf(node.value);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(val, x - fm.stringWidth(val) / 2, y + fm.getAscent() / 2 - 1);

        // Height & BF label
        g2.setColor(new Color(160, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 10));
        String meta = "h=" + node.height + " FE=" + node.getBalanceFactor();
        g2.drawString(meta, x - fm.stringWidth(meta) / 2 - 5, y + r + 12);
    }
}
