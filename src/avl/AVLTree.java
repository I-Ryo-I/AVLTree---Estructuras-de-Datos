package avl;

import java.util.ArrayList;
import java.util.List;

public class AVLTree {

    public AVLNode root;
    private List<String> log = new ArrayList<>();

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private int height(AVLNode n) {
        return (n == null) ? 0 : n.height;
    }

    private void updateHeight(AVLNode n) {
        if (n != null)
            n.height = 1 + Math.max(height(n.left), height(n.right));
    }

    private int bf(AVLNode n) {
        return (n == null) ? 0 : height(n.left) - height(n.right);
    }

    // ─── Rotations ─────────────────────────────────────────────────────────────

    private AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;
        x.right = y;
        y.left = T2;
        updateHeight(y);
        updateHeight(x);
        log("  ↻ Rotación DERECHA en nodo " + y.value);
        return x;
    }

    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;
        y.left = x;
        x.right = T2;
        updateHeight(x);
        updateHeight(y);
        log("  ↺ Rotación IZQUIERDA en nodo " + x.value);
        return y;
    }

    // ─── Balance ───────────────────────────────────────────────────────────────

    private AVLNode balance(AVLNode node) {
        updateHeight(node);
        int b = bf(node);

        // Left heavy
        if (b > 1) {
            if (bf(node.left) < 0) {
                log("  ⟳ Caso Izquierda-Derecha en " + node.value);
                node.left = rotateLeft(node.left);
            } else {
                log("  ⟳ Caso Izquierda-Izquierda en " + node.value);
            }
            return rotateRight(node);
        }
        // Right heavy
        if (b < -1) {
            if (bf(node.right) > 0) {
                log("  ⟳ Caso Derecha-Izquierda en " + node.value);
                node.right = rotateRight(node.right);
            } else {
                log("  ⟳ Caso Derecha-Derecha en " + node.value);
            }
            return rotateLeft(node);
        }
        return node;
    }

    // ─── Insert ────────────────────────────────────────────────────────────────

    public void insert(int value) {
        log("▶ Insertar: " + value);
        root = insertRec(root, value);
        log("✔ Inserción completa. Raíz: " + (root != null ? root.value : "null"));
    }

    private AVLNode insertRec(AVLNode node, int value) {
        if (node == null) {
            log("  + Nodo creado: " + value);
            return new AVLNode(value);
        }
        if (value < node.value) {
            log("  → Ir a la izquierda de " + node.value);
            node.left = insertRec(node.left, value);
        } else if (value > node.value) {
            log("  → Ir a la derecha de " + node.value);
            node.right = insertRec(node.right, value);
        } else {
            log("  ⚠ Valor duplicado: " + value + " (ignorado)");
            return node;
        }
        return balance(node);
    }

    // ─── Delete ────────────────────────────────────────────────────────────────

    public void delete(int value) {
        log("▶ Eliminar: " + value);
        root = deleteRec(root, value);
        log("✔ Eliminación completa.");
    }

    private AVLNode deleteRec(AVLNode node, int value) {
        if (node == null) {
            log("  ⚠ Valor " + value + " no encontrado");
            return null;
        }
        if (value < node.value) {
            log("  → Ir a la izquierda de " + node.value);
            node.left = deleteRec(node.left, value);
        } else if (value > node.value) {
            log("  → Ir a la derecha de " + node.value);
            node.right = deleteRec(node.right, value);
        } else {
            log("  ✂ Nodo " + value + " encontrado y eliminado");
            if (node.left == null || node.right == null) {
                return (node.left != null) ? node.left : node.right;
            }
            AVLNode succ = minNode(node.right);
            log("  ↔ Sucesor inorden: " + succ.value);
            node.value = succ.value;
            node.right = deleteRec(node.right, succ.value);
        }
        return balance(node);
    }

    private AVLNode minNode(AVLNode n) {
        while (n.left != null) n = n.left;
        return n;
    }

    // ─── Search ────────────────────────────────────────────────────────────────

    public List<Integer> search(int value) {
        List<Integer> path = new ArrayList<>();
        log("▶ Buscar: " + value);
        searchRec(root, value, path);
        return path;
    }

    private boolean searchRec(AVLNode node, int value, List<Integer> path) {
        if (node == null) {
            log("  ✗ No encontrado");
            return false;
        }
        path.add(node.value);
        log("  → Visitar: " + node.value + " (FE=" + bf(node) + ")");
        if (value == node.value) {
            log("  ✔ Encontrado: " + value);
            return true;
        }
        if (value < node.value) return searchRec(node.left, value, path);
        return searchRec(node.right, value, path);
    }

    // ─── Stats ─────────────────────────────────────────────────────────────────

    public int getHeight() {
        return height(root);
    }

    public int countNodes() {
        return countRec(root);
    }

    private int countRec(AVLNode n) {
        if (n == null) return 0;
        return 1 + countRec(n.left) + countRec(n.right);
    }

    // ─── Log ───────────────────────────────────────────────────────────────────

    private void log(String msg) {
        log.add(msg);
    }

    public List<String> getLog() {
        return new ArrayList<>(log);
    }

    public void clearLog() {
        log.clear();
    }
}
