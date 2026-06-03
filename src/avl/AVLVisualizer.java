package avl;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class AVLVisualizer extends JFrame {

    // ─── State ─────────────────────────────────────────────────────────────────
    private AVLTree tree = new AVLTree();
    private TreePanel treePanel;
    private DefaultListModel<String> logModel = new DefaultListModel<>();

    // Animation
    private Timer animTimer;
    private List<Integer> animPath = new ArrayList<>();
    private int animIndex = 0;
    private boolean autoBalance = false;

    // Insertion queue for step-by-step
    private Queue<Integer> insertQueue = new LinkedList<>();
    private boolean stepMode = false;

    // Sequence asignada (Estudiante #9)
    private static final int[] SEQUENCE = {48, 28, 75, 18, 38, 68, 88, 36, 37, 35, 34, 39};

    // ─── UI Components ─────────────────────────────────────────────────────────
    private JTextField inputField;
    private JSlider speedSlider;
    private JLabel statusLabel;
    private JToggleButton toggleAutoBtn;
    private JButton stepBtn;
    private JList<String> logList;

    // ─── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG_DARK    = new Color(18,  18,  28);
    private static final Color BG_PANEL   = new Color(28,  32,  50);
    private static final Color ACCENT     = new Color(80, 140, 240);
    private static final Color ACCENT2    = new Color(50, 200, 120);
    private static final Color TEXT_MAIN  = new Color(220, 225, 255);
    private static final Color TEXT_DIM   = new Color(130, 140, 170);

    public AVLVisualizer() {
        setTitle("Árbol AVL Interactivo — MORENO MOLINA JUAN SEBASTIAN (Est. #9)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300, 780);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(6, 6));

        buildUI();
        setVisible(true);
        addLog("▶ Aplicación iniciada. Secuencia asignada: " + Arrays.toString(SEQUENCE));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BUILD UI
    // ═══════════════════════════════════════════════════════════════════════════

    private void buildUI() {
        // TOP: title bar
        add(buildTopBar(), BorderLayout.NORTH);

        // CENTER: tree canvas
        treePanel = new TreePanel(tree);
        treePanel.setBorder(BorderFactory.createLineBorder(new Color(50, 60, 90), 2));
        add(treePanel, BorderLayout.CENTER);

        // RIGHT: controls + log
        add(buildRightPanel(), BorderLayout.EAST);

        // BOTTOM: status
        statusLabel = new JLabel(" Listo.", SwingConstants.LEFT);
        statusLabel.setForeground(TEXT_DIM);
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
        statusLabel.setBackground(BG_PANEL);
        statusLabel.setOpaque(true);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(new Color(22, 25, 40));
        bar.setBorder(new EmptyBorder(4, 8, 4, 8));

        JLabel title = new JLabel("🌳  AVL Tree — Estudiante #9");
        title.setForeground(TEXT_MAIN);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bar.add(title);

        return bar;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setPreferredSize(new Dimension(290, 0));
        panel.setBorder(new EmptyBorder(10, 8, 10, 8));

        // ── Input ──
        panel.add(sectionLabel("Operaciones"));
        panel.add(Box.createVerticalStrut(4));

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        inputRow.setBackground(BG_PANEL);
        inputField = styledField(8);
        inputRow.add(inputField);
        inputRow.add(actionBtn("Insertar", ACCENT, e -> doInsert()));
        inputRow.add(actionBtn("Eliminar", new Color(200, 80, 80), e -> doDelete()));
        panel.add(inputRow);
        panel.add(Box.createVerticalStrut(4));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        searchRow.setBackground(BG_PANEL);
        searchRow.add(actionBtn("🔍 Buscar (animado)", new Color(160, 100, 220), e -> doSearch()));
        panel.add(searchRow);

        panel.add(Box.createVerticalStrut(10));
        panel.add(separator());

        // ── Balanceo ──
        panel.add(sectionLabel("Modo de Balanceo"));
        panel.add(Box.createVerticalStrut(4));

        toggleAutoBtn = new JToggleButton("▶ Automático (toggle)");
        styleToggle(toggleAutoBtn);
        toggleAutoBtn.addActionListener(e -> {
            autoBalance = toggleAutoBtn.isSelected();
            toggleAutoBtn.setText(autoBalance ? "⏸ Automático ACTIVO" : "▶ Automático (toggle)");
            stepBtn.setEnabled(!autoBalance);
            addLog(autoBalance ? "⚙ Modo automático activado" : "⚙ Modo automático desactivado");
        });
        panel.add(toggleAutoBtn);
        panel.add(Box.createVerticalStrut(4));

        stepBtn = actionBtn("⏭ Insertar paso a paso", new Color(50, 170, 170), e -> doNextStep());
        stepBtn.setAlignmentX(LEFT_ALIGNMENT);
        stepBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(stepBtn);

        panel.add(Box.createVerticalStrut(10));
        panel.add(separator());

        // ── Velocidad ──
        panel.add(sectionLabel("Velocidad de Animación"));
        panel.add(Box.createVerticalStrut(2));

        speedSlider = new JSlider(100, 2000, 700);
        speedSlider.setInverted(true);
        speedSlider.setBackground(BG_PANEL);
        speedSlider.setForeground(TEXT_DIM);
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setAlignmentX(LEFT_ALIGNMENT);
        speedSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(speedSlider);

        panel.add(Box.createVerticalStrut(10));
        panel.add(separator());

        // ── Acciones rápidas ──
        panel.add(sectionLabel("Acciones Rápidas"));
        panel.add(Box.createVerticalStrut(4));

        JPanel btnRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        btnRow1.setBackground(BG_PANEL);
        btnRow1.add(actionBtn("📋 Cargar secuencia", new Color(90, 140, 90), e -> loadSequence()));
        btnRow1.add(actionBtn("🎲 Aleatorio", new Color(120, 100, 60), e -> generateRandom()));
        panel.add(btnRow1);

        JPanel btnRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        btnRow2.setBackground(BG_PANEL);
        btnRow2.add(actionBtn("🔄 Reiniciar árbol", new Color(160, 60, 60), e -> resetTree()));
        panel.add(btnRow2);

        panel.add(Box.createVerticalStrut(10));
        panel.add(separator());

        // ── Exportar ──
        panel.add(sectionLabel("Exportar"));
        panel.add(Box.createVerticalStrut(4));

        JPanel expRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        expRow.setBackground(BG_PANEL);
        expRow.add(actionBtn("🖼 PNG", new Color(60, 120, 160), e -> exportPNG()));
        expRow.add(actionBtn("📄 PDF", new Color(160, 90, 50), e -> exportPDF()));
        expRow.add(actionBtn("🖨 Imprimir", new Color(80, 80, 120), e -> printTree()));
        panel.add(expRow);

        panel.add(Box.createVerticalStrut(10));
        panel.add(separator());

        // ── Log ──
        panel.add(sectionLabel("Panel de Log"));
        panel.add(Box.createVerticalStrut(4));

        logList = new JList<>(logModel);
        logList.setBackground(new Color(14, 16, 25));
        logList.setForeground(new Color(180, 220, 180));
        logList.setFont(new Font("Consolas", Font.PLAIN, 11));
        logList.setSelectionBackground(new Color(40, 60, 100));

        JScrollPane logScroll = new JScrollPane(logList);
        logScroll.setPreferredSize(new Dimension(270, 160));
        logScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(50, 60, 90)));
        logScroll.getVerticalScrollBar().setBackground(BG_PANEL);
        panel.add(logScroll);

        JButton clearLogBtn = actionBtn("Limpiar log", new Color(80, 80, 90), e -> logModel.clear());
        clearLogBtn.setAlignmentX(LEFT_ALIGNMENT);
        clearLogBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(Box.createVerticalStrut(4));
        panel.add(clearLogBtn);

        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    private void doInsert() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) { status("⚠ Ingrese un valor."); return; }
        try {
            int val = Integer.parseInt(text);
            tree.clearLog();
            tree.insert(val);
            flushTreeLog();
            status("✔ Insertado: " + val + " | Nodos: " + tree.countNodes() + " | Altura: " + tree.getHeight());
            treePanel.clearHighlight();
            treePanel.repaint();
            inputField.setText("");
        } catch (NumberFormatException ex) {
            status("⚠ Valor inválido: " + text);
        }
    }

    private void doDelete() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) { status("⚠ Ingrese un valor."); return; }
        try {
            int val = Integer.parseInt(text);
            tree.clearLog();
            tree.delete(val);
            flushTreeLog();
            status("✔ Eliminado: " + val + " | Nodos: " + tree.countNodes() + " | Altura: " + tree.getHeight());
            treePanel.clearHighlight();
            treePanel.repaint();
            inputField.setText("");
        } catch (NumberFormatException ex) {
            status("⚠ Valor inválido.");
        }
    }

    private void doSearch() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) { status("⚠ Ingrese un valor para buscar."); return; }
        try {
            int val = Integer.parseInt(text);
            tree.clearLog();
            List<Integer> path = tree.search(val);
            flushTreeLog();

            if (path.isEmpty()) { status("✗ No encontrado: " + val); return; }

            animPath = path;
            animIndex = 0;
            status("🔍 Buscando " + val + " ...");

            if (animTimer != null) animTimer.stop();
            animTimer = new Timer(speedSlider.getValue(), null);
            final int target = val;
            animTimer.addActionListener(e -> {
                treePanel.setHighlightPath(animPath, target, animIndex);
                if (animIndex < animPath.size() - 1) {
                    animIndex++;
                } else {
                    ((Timer) e.getSource()).stop();
                    boolean found = animPath.get(animPath.size() - 1) == target;
                    status(found ? "✔ Encontrado: " + target + " (pasos: " + animPath.size() + ")"
                                 : "✗ No encontrado: " + target);
                }
            });
            animTimer.start();

        } catch (NumberFormatException ex) {
            status("⚠ Valor inválido.");
        }
    }

    private void loadSequence() {
        if (!autoBalance) {
            // Paso a paso: encolar
            insertQueue.clear();
            for (int v : SEQUENCE) insertQueue.add(v);
            addLog("📋 Secuencia cargada (" + SEQUENCE.length + " valores). Presione 'Paso a paso'.");
            status("📋 Secuencia lista. Use 'Insertar paso a paso'.");
            stepMode = true;
        } else {
            // Auto: insertar todo animado
            resetTree();
            Timer t = new Timer(speedSlider.getValue(), null);
            int[] idx = {0};
            t.addActionListener(e -> {
                if (idx[0] < SEQUENCE.length) {
                    tree.clearLog();
                    tree.insert(SEQUENCE[idx[0]]);
                    flushTreeLog();
                    treePanel.repaint();
                    status("⚙ Insertando: " + SEQUENCE[idx[0]] + " (" + (idx[0]+1) + "/" + SEQUENCE.length + ")");
                    idx[0]++;
                } else {
                    ((Timer) e.getSource()).stop();
                    status("✔ Secuencia cargada completa.");
                }
            });
            t.start();
        }
    }

    private void doNextStep() {
        if (!stepMode || insertQueue.isEmpty()) {
            addLog("⚠ No hay valores en cola. Cargue la secuencia primero.");
            status("⚠ Cola vacía. Cargue la secuencia.");
            return;
        }
        int val = insertQueue.poll();
        tree.clearLog();
        tree.insert(val);
        flushTreeLog();
        treePanel.repaint();
        status("⏭ Insertado paso: " + val + " | Restantes: " + insertQueue.size());
        if (insertQueue.isEmpty()) {
            stepMode = false;
            status("✔ Secuencia completa.");
        }
    }

    private void generateRandom() {
        Random rnd = new Random();
        int n = 5 + rnd.nextInt(16); // 5-20 nodos
        resetTree();
        Set<Integer> used = new HashSet<>();
        for (int i = 0; i < n; i++) {
            int v;
            do { v = 1 + rnd.nextInt(99); } while (used.contains(v));
            used.add(v);
            tree.insert(v);
        }
        treePanel.repaint();
        addLog("🎲 Árbol aleatorio generado (" + n + " nodos).");
        status("🎲 Árbol aleatorio — " + n + " nodos, altura: " + tree.getHeight());
    }

    private void resetTree() {
        tree = new AVLTree();
        treePanel = rebuildTreePanel();
        treePanel.clearHighlight();
        insertQueue.clear();
        stepMode = false;
        logModel.clear();
        addLog("🔄 Árbol reiniciado.");
        status("🔄 Árbol reiniciado.");
        revalidate();
        repaint();
    }

    private TreePanel rebuildTreePanel() {
        TreePanel tp = new TreePanel(tree);
        tp.setBorder(BorderFactory.createLineBorder(new Color(50, 60, 90), 2));
        getContentPane().remove(1); // remove old tree panel (CENTER)
        // Rebuild center
        tp.setBackground(new Color(18, 18, 28));
        getContentPane().add(tp, BorderLayout.CENTER, 1);
        return tp;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EXPORT
    // ═══════════════════════════════════════════════════════════════════════════

    private BufferedImage renderTree() {
        int w = treePanel.getWidth();
        int h = treePanel.getHeight();
        if (w <= 0 || h <= 0) { w = 900; h = 600; }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        treePanel.paint(g2);
        g2.dispose();
        return img;
    }

    private void exportPNG() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("arbol_avl_" + timestamp() + ".png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            BufferedImage img = renderTree();
            File out = fc.getSelectedFile();
            if (!out.getName().endsWith(".png")) out = new File(out.getAbsolutePath() + ".png");
            ImageIO.write(img, "png", out);
            addLog("🖼 PNG exportado: " + out.getAbsolutePath());
            status("✔ PNG guardado en: " + out.getName());
            JOptionPane.showMessageDialog(this, "PNG guardado:\n" + out.getAbsolutePath(), "Exportado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPDF() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("arbol_avl_" + timestamp() + ".pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File out = fc.getSelectedFile();
        if (!out.getName().endsWith(".pdf")) out = new File(out.getAbsolutePath() + ".pdf");

        try {
            // Use iText-free approach: write PostScript-like PDF manually (pure Java)
            BufferedImage img = renderTree();

            // Save image temp
            File tmpImg = File.createTempFile("avl_temp", ".png");
            ImageIO.write(img, "png", tmpImg);

            // Build minimal PDF with embedded JPEG image
            writePDF(out, img, tmpImg);
            tmpImg.delete();

            addLog("📄 PDF exportado: " + out.getAbsolutePath());
            status("✔ PDF guardado: " + out.getName());
            JOptionPane.showMessageDialog(this, "PDF guardado:\n" + out.getAbsolutePath(), "Exportado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writePDF(File out, BufferedImage img, File imgFile) throws Exception {
        // Convert image to JPEG bytes
        ByteArrayOutputStream jpegBaos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpeg", jpegBaos);
        byte[] jpegBytes = jpegBaos.toByteArray();

        // PDF page size: A4 = 595 x 842 pts
        float pageW = 595f, pageH = 842f;
        float margin = 40f;
        float imgW = pageW - 2 * margin;
        float imgH = imgW * img.getHeight() / img.getWidth();
        if (imgH > pageH - 2 * margin - 80) imgH = pageH - 2 * margin - 80;

        // Stats text
        String stats = buildStatsText();

        // Build PDF
        StringBuilder pdf = new StringBuilder();
        ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
        List<Long> offsets = new ArrayList<>();

        // Temp: we'll write to rawOut then compute xref
        DataOutputStream dos = new DataOutputStream(rawOut);

        String header = "%PDF-1.4\n";
        dos.writeBytes(header);

        // Object 1: catalog
        offsets.add((long) rawOut.size());
        dos.writeBytes("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        // Object 2: pages
        offsets.add((long) rawOut.size());
        dos.writeBytes("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        // Object 3: page
        offsets.add((long) rawOut.size());
        dos.writeBytes("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842]"
                + " /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> /XObject << /Im1 6 0 R >> >> >>\nendobj\n");

        // Object 4: page content stream
        String imgX = String.format("%.1f", margin);
        String imgY = String.format("%.1f", pageH - margin - imgH - 60);
        String imgWs = String.format("%.1f", imgW);
        String imgHs = String.format("%.1f", imgH);

        StringBuilder stream = new StringBuilder();
        stream.append("BT\n/F1 14 Tf\n").append(margin).append(" ").append(pageH - margin - 20).append(" Td\n");
        stream.append("(Arbol AVL - MORENO MOLINA JUAN SEBASTIAN) Tj\n");
        stream.append("/F1 9 Tf\n0 -16 Td\n(Estudiante #9 | ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())).append(") Tj\n");

        // Stats
        String[] lines = stats.split("\n");
        stream.append("0 -20 Td\n");
        for (String l : lines) {
            String safe = l.replaceAll("[()\\\\]", "");
            stream.append("(").append(safe).append(") Tj 0 -13 Td\n");
        }
        stream.append("ET\n");
        stream.append("q\n").append(imgWs).append(" 0 0 ").append(imgHs).append(" ")
              .append(imgX).append(" ").append(imgY).append(" cm\n/Im1 Do\nQ\n");

        byte[] streamBytes = stream.toString().getBytes("ISO-8859-1");
        offsets.add((long) rawOut.size());
        dos.writeBytes("4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
        dos.write(streamBytes);
        dos.writeBytes("\nendstream\nendobj\n");

        // Object 5: font
        offsets.add((long) rawOut.size());
        dos.writeBytes("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        // Object 6: image XObject
        offsets.add((long) rawOut.size());
        dos.writeBytes("6 0 obj\n<< /Type /XObject /Subtype /Image /Width " + img.getWidth()
                + " /Height " + img.getHeight() + " /ColorSpace /DeviceRGB /BitsPerComponent 8"
                + " /Filter /DCTDecode /Length " + jpegBytes.length + " >>\nstream\n");
        dos.write(jpegBytes);
        dos.writeBytes("\nendstream\nendobj\n");

        // xref
        long xrefOffset = rawOut.size();
        dos.writeBytes("xref\n0 7\n");
        dos.writeBytes("0000000000 65535 f \n");
        for (long offset : offsets) {
            dos.writeBytes(String.format("%010d 00000 n \n", offset));
        }
        dos.writeBytes("trailer\n<< /Size 7 /Root 1 0 R >>\n");
        dos.writeBytes("startxref\n" + xrefOffset + "\n%%EOF\n");
        dos.flush();

        dos.flush();
        try (FileOutputStream fos = new FileOutputStream(out)) {
            rawOut.writeTo(fos);
        }
    }

    private void printTree() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            double sx = pageFormat.getImageableWidth() / treePanel.getWidth();
            double sy = pageFormat.getImageableHeight() / treePanel.getHeight();
            double scale = Math.min(sx, sy);
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2.scale(scale, scale);
            treePanel.paint(g2);
            return java.awt.print.Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try {
                job.print();
                addLog("🖨 Impresión enviada.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al imprimir: " + ex.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private String buildStatsText() {
        return "Estadisticas del Arbol:\n"
             + "  Nodos totales : " + tree.countNodes() + "\n"
             + "  Altura        : " + tree.getHeight() + "\n"
             + "  Raiz          : " + (tree.root != null ? tree.root.value : "vacio") + "\n"
             + "  FE raiz       : " + (tree.root != null ? tree.root.getBalanceFactor() : 0) + "\n"
             + "  Generado      : " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
    }

    private void flushTreeLog() {
        for (String line : tree.getLog()) {
            addLog(line);
        }
    }

    private void addLog(String msg) {
        String ts = new SimpleDateFormat("HH:mm:ss").format(new Date());
        logModel.addElement("[" + ts + "] " + msg);
        logList.ensureIndexIsVisible(logModel.getSize() - 1);
    }

    private void status(String msg) {
        statusLabel.setText("  " + msg);
    }

    private String timestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI BUILDERS
    // ═══════════════════════════════════════════════════════════════════════════

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(ACCENT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 60, 90));
        sep.setBackground(BG_PANEL);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return sep;
    }

    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(new Color(30, 35, 55));
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(TEXT_MAIN);
        f.setFont(new Font("Consolas", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        f.addActionListener(e -> doInsert());
        return f;
    }

    private JButton actionBtn(String text, Color bg, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private void styleToggle(JToggleButton btn) {
        btn.setBackground(new Color(60, 100, 60));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }
}
