package ru.vsu.kudinov_i_m.demo;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;
import ru.vsu.kudinov_i_m.Graph;
import ru.vsu.kudinov_i_m.GraphAlgorithms;
import ru.vsu.kudinov_i_m.utils.GraphUtils;
import ru.vsu.kudinov_i_m.utils.JTableUtils;
import ru.vsu.kudinov_i_m.utils.SwingUtils;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.text.ParseException;
import java.util.Locale;
import java.util.Stack;

public class GraphDemoFrame extends JFrame
{
    private JPanel panelMain;
    private JTextArea textAreaSystemOut;
    private JPanel panelGraphPainterContainer;
    private JButton createAGraphButton;
    private JTable tableArray;
    private JButton clearButton;
    private JButton findEulerCycleButton;
    private JButton checkEulerGraphButton;
    private JSpinner vertexOfTheCycleSpinner;

    private JFileChooser fileChooserTxtOpen;
    private JFileChooser fileChooserDotOpen;
    private JFileChooser fileChooserTxtSave;
    private JFileChooser fileChooserDotSave;
    private JFileChooser fileChooserImgSave;

    private Graph graph = null;

    private SvgPanel panelGraphPainter;

    private boolean[][] adjacencyMatrix = null;
    private int vertexToStartCycle;
    private final int[][] INITIAL_ADJACENCY_MATRIX = {{0, 0, 0, 0, 0, 1, 1, 0, 0, 0}, {0, 0, 0, 0, 1, 0, 0, 1, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 1, 1, 0}, {0, 0, 0, 0, 0, 1, 0, 1, 1, 1},
            {0, 1, 0, 0, 0, 1, 1, 0, 1, 0}, {1, 0, 0, 1, 1, 0, 1, 0, 0, 0}, {1, 0, 0, 0, 1, 1, 0, 1, 0, 0}, {0, 1, 1, 1, 0, 0, 1, 0, 0, 0}, {0, 0, 1, 1, 1, 0, 0, 0, 0, 1}, {0, 0, 0, 1, 0, 0, 0, 0, 1, 0}};


    private static class SvgPanel extends JPanel
    {
        private GraphicsNode svgGraphicsNode = null;

        public void paint(String svg) throws IOException
        {
            String xmlParser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory df = new SAXSVGDocumentFactory(xmlParser);
            SVGDocument doc = df.createSVGDocument(null, new StringReader(svg));
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext ctx = new BridgeContext(userAgent, loader);
            ctx.setDynamicState(BridgeContext.DYNAMIC);
            GVTBuilder builder = new GVTBuilder();
            svgGraphicsNode = builder.build(ctx, doc);

            repaint();
        }

        @Override
        public void paintComponent(Graphics gr)
        {
            super.paintComponent(gr);

            if (svgGraphicsNode == null)
            {
                return;
            }

            double scaleX = this.getWidth() / svgGraphicsNode.getPrimitiveBounds().getWidth();
            double scaleY = this.getHeight() / svgGraphicsNode.getPrimitiveBounds().getHeight();
            double scale = Math.min(scaleX, scaleY);
            AffineTransform transform = new AffineTransform(scale, 0, 0, scale, 0, 0);
            svgGraphicsNode.setTransform(transform);
            Graphics2D g2d = (Graphics2D) gr;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            svgGraphicsNode.paint(g2d);
        }
    }

    public GraphDemoFrame()
    {
        this.setTitle("Graphs");
        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();

        JTableUtils.initJTableForArray(tableArray, 35, true, true, false, true);
        tableArray.setRowHeight(30);
        JTableUtils.writeArrayToJTable(tableArray, INITIAL_ADJACENCY_MATRIX);

        textAreaSystemOut.setLineWrap(true);
        textAreaSystemOut.setWrapStyleWord(true);

        fileChooserTxtOpen = new JFileChooser();
        fileChooserDotOpen = new JFileChooser();
        fileChooserTxtSave = new JFileChooser();
        fileChooserDotSave = new JFileChooser();
        fileChooserImgSave = new JFileChooser();
        fileChooserTxtOpen.setCurrentDirectory(new File("./files/input"));
        fileChooserDotOpen.setCurrentDirectory(new File("./files/input"));
        fileChooserTxtSave.setCurrentDirectory(new File("./files/input"));
        fileChooserDotSave.setCurrentDirectory(new File("./files/input"));
        fileChooserImgSave.setCurrentDirectory(new File("./files/output"));
        FileFilter txtFilter = new FileNameExtensionFilter("Text files (*.txt)", "txt");
        FileFilter dotFilter = new FileNameExtensionFilter("DOT files (*.dot)", "dot");
        FileFilter svgFilter = new FileNameExtensionFilter("SVG images (*.svg)", "svg");

        fileChooserTxtOpen.addChoosableFileFilter(txtFilter);
        fileChooserDotOpen.addChoosableFileFilter(dotFilter);
        fileChooserTxtSave.addChoosableFileFilter(txtFilter);
        fileChooserDotSave.addChoosableFileFilter(dotFilter);
        fileChooserImgSave.addChoosableFileFilter(svgFilter);

        fileChooserTxtSave.setAcceptAllFileFilterUsed(false);
        fileChooserTxtSave.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooserTxtSave.setApproveButtonText("Save");
        fileChooserDotSave.setAcceptAllFileFilterUsed(false);
        fileChooserDotSave.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooserDotSave.setApproveButtonText("Save");
        fileChooserImgSave.setAcceptAllFileFilterUsed(false);
        fileChooserImgSave.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooserImgSave.setApproveButtonText("Save");

        panelGraphPainterContainer.setLayout(new BorderLayout());
        panelGraphPainter = new SvgPanel();
        panelGraphPainterContainer.add(new JScrollPane(panelGraphPainter));


        createAGraphButton.addActionListener(e ->
        {
            try
            {
                adjacencyMatrix = JTableUtils.readIntMatrixFromJTable(tableArray);
                Class clz = Class.forName("ru.vsu.kudinov_i_m.AdjacencyMatrixGraph");
                Graph graph = GraphUtils.fromStr(readFromMatrixToStr(adjacencyMatrix), clz);
                GraphDemoFrame.this.graph = graph;
                panelGraphPainter.paint(dotToSvg(GraphUtils.toDot(graph)));

            } catch (Exception exc)
            {
                SwingUtils.showErrorMessageBox(exc);
            }
        });

        checkEulerGraphButton.addActionListener(e ->
        {
            if (graph == null)
            {
                textAreaSystemOut.setText("Graph not found, please make it");
            } else
            {
                if (GraphAlgorithms.isEulerGraph(adjacencyMatrix))
                {
                    textAreaSystemOut.setText("Your graph is Euler: YES\n" + "You can find Euler cycle");
                } else
                {
                    textAreaSystemOut.setText("Your graph is Euler: NO\n" + "You can't find Euler cycle");
                }
            }
        });

        findEulerCycleButton.addActionListener(e ->
        {
            try
            {
                if (graph == null)
                {
                    textAreaSystemOut.setText("Graph not found, please make it");
                } else
                {
                    if (GraphAlgorithms.isEulerGraph(adjacencyMatrix))
                    {
                        vertexToStartCycle = (int) vertexOfTheCycleSpinner.getValue();
                        if (vertexToStartCycle < 0 || vertexToStartCycle > adjacencyMatrix.length)
                        {
                            textAreaSystemOut.setText("Incorrect vertex");
                        } else
                        {
                            Stack<Integer> EulerCycle = GraphAlgorithms.findEulerCycle(JTableUtils.readIntMatrixFromJTable(tableArray), vertexToStartCycle);
                            printCycle(EulerCycle);
                        }
                    } else
                    {
                        textAreaSystemOut.setText("Graph is not Euler\n" + "There is no Euler cycle");
                    }
                }
            } catch (ParseException parseException)
            {
                parseException.printStackTrace();
            }
        });

        clearButton.addActionListener(e ->
        {
            int[][] clearArray = new int[1][1];
            JTableUtils.writeArrayToJTable(tableArray, clearArray);
            createAGraphButton.doClick();
            textAreaSystemOut.setText(null);
        });
    }

    private void printCycle(Stack<Integer> eulerCycle)
    {
        textAreaSystemOut.setText("Your Euler cycle:\n");
        textAreaSystemOut.append(String.valueOf(eulerCycle.pop()));
        while (!eulerCycle.isEmpty())
        {
            textAreaSystemOut.append(" => ");
            textAreaSystemOut.append(String.valueOf(eulerCycle.pop()));
        }
    }

    private static String readFromMatrixToStr(boolean[][] matrix)
    {
        StringBuilder strGraph = new StringBuilder(" ");
        strGraph.append(matrix.length).append("\n");

        int edgeCount = 0;

        for (int i = 0; i < matrix.length; i++)
        {
            for (int j = 0; j < matrix[i].length; j++)
            {
                if (matrix[i][j]) edgeCount++;
            }
        }

        strGraph.append(" ").append(edgeCount).append("\n");

        for (int i = 0; i < matrix.length; i++)
        {
            for (int j = 0; j < matrix[i].length; j++)
            {
                if (matrix[i][j]) strGraph.append(" ").append(i).append(" ").append(j).append("\n");
            }
        }
        System.out.println(strGraph.toString());
        return strGraph.toString();
    }

    private static String dotToSvg(String dotSrc) throws IOException
    {
        MutableGraph g = new Parser().read(dotSrc);
        return Graphviz.fromGraph(g).render(Format.SVG).toString();
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        panelMain = new JPanel();
        panelMain.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), 10, 10));
        panelMain.setBackground(new Color(-16756114));
        panelMain.setInheritsPopupMenu(true);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setBackground(new Color(-16756114));
        panelMain.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-16756114));
        splitPane1.setRightComponent(panel1);
        panelGraphPainterContainer = new JPanel();
        panelGraphPainterContainer.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelGraphPainterContainer.setBackground(new Color(-4474189));
        panelGraphPainterContainer.setForeground(new Color(-4474189));
        panel1.add(panelGraphPainterContainer, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setBackground(new Color(-16756114));
        splitPane1.setLeftComponent(panel2);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setAutoscrolls(true);
        scrollPane1.setBackground(new Color(-4474189));
        Font scrollPane1Font = this.$$$getFont$$$("Arial Black", -1, -1, scrollPane1.getFont());
        if (scrollPane1Font != null) scrollPane1.setFont(scrollPane1Font);
        scrollPane1.setForeground(new Color(-4474189));
        scrollPane1.setToolTipText("");
        panel2.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tableArray = new JTable();
        tableArray.setBackground(new Color(-2960950));
        tableArray.setCellSelectionEnabled(false);
        Font tableArrayFont = this.$$$getFont$$$("Cambria", Font.BOLD, -1, tableArray.getFont());
        if (tableArrayFont != null) tableArray.setFont(tableArrayFont);
        tableArray.setGridColor(new Color(-16756114));
        scrollPane1.setViewportView(tableArray);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setBackground(new Color(-16756114));
        panel2.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        createAGraphButton = new JButton();
        createAGraphButton.setBackground(new Color(-2960950));
        createAGraphButton.setText("Create a graph");
        panel3.add(createAGraphButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clearButton = new JButton();
        clearButton.setBackground(new Color(-2960950));
        clearButton.setText("Clear");
        panel3.add(clearButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        findEulerCycleButton = new JButton();
        findEulerCycleButton.setBackground(new Color(-2960950));
        findEulerCycleButton.setText("Find Euler cycle");
        panel3.add(findEulerCycleButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkEulerGraphButton = new JButton();
        checkEulerGraphButton.setBackground(new Color(-2960950));
        checkEulerGraphButton.setText("Check graph");
        panel3.add(checkEulerGraphButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel4.setBackground(new Color(-16756114));
        panel3.add(panel4, new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setAutoscrolls(true);
        label1.setBackground(new Color(-267));
        label1.setForeground(new Color(-1));
        label1.setHorizontalAlignment(10);
        label1.setHorizontalTextPosition(11);
        label1.setText("Select the starting");
        panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        vertexOfTheCycleSpinner = new JSpinner();
        vertexOfTheCycleSpinner.setBackground(new Color(-4474189));
        vertexOfTheCycleSpinner.setForeground(new Color(-2960950));
        panel4.add(vertexOfTheCycleSpinner, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setForeground(new Color(-1));
        label2.setText("vertex of the cycle:");
        panel4.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel2.add(scrollPane2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textAreaSystemOut = new JTextArea();
        textAreaSystemOut.setBackground(new Color(-2960950));
        textAreaSystemOut.setCaretColor(new Color(-2960950));
        scrollPane2.setViewportView(textAreaSystemOut);
        label1.setLabelFor(vertexOfTheCycleSpinner);
        label2.setLabelFor(vertexOfTheCycleSpinner);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont)
    {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null)
        {
            resultName = currentFont.getName();
        } else
        {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1'))
            {
                resultName = fontName;
            } else
            {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return panelMain;
    }

}
