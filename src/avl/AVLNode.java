package avl;

public class AVLNode {
    public int value;
    public int height;
    public AVLNode left, right;

    public AVLNode(int value) {
        this.value = value;
        this.height = 1;
        this.left = null;
        this.right = null;
    }

    public int getBalanceFactor() {
        int leftH = (left != null) ? left.height : 0;
        int rightH = (right != null) ? right.height : 0;
        return leftH - rightH;
    }
}
