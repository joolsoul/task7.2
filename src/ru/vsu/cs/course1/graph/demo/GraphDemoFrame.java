package ru.vsu.cs.course1.graph.demo;

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
import ru.vsu.cs.course1.graph.Graph;
import ru.vsu.cs.course1.graph.GraphAlgorithms;
import ru.vsu.cs.util.GraphUtils;
import ru.vsu.cs.util.JTableUtils;
import ru.vsu.cs.util.SwingUtils;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.text.ParseException;
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

    private JFileChooser fileChooserTxtOpen;
    private JFileChooser fileChooserDotOpen;
    private JFileChooser fileChooserTxtSave;
    private JFileChooser fileChooserDotSave;
    private JFileChooser fileChooserImgSave;

    private Graph graph = null;

    private SvgPanel panelGraphPainter;

    private boolean[][] adjacencyMatrix = null;
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
        this.setTitle("Графы");
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
                Class clz = Class.forName("ru.vsu.cs.course1.graph.AdjMatrixGraph");
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
            if (GraphAlgorithms.isEulerGraph(adjacencyMatrix))
            {
                textAreaSystemOut.setText("Your graph is Euler: YES\n" + "You can find Euler cycle");
            } else
            {
                textAreaSystemOut.setText("Your graph is Euler: NO\n" + "You can't find Euler cycle");
            }
        });

        findEulerCycleButton.addActionListener(e ->
        {
            try
            {
                if (GraphAlgorithms.isEulerGraph(adjacencyMatrix))
                {
                    Stack<Integer> EulerCycle = GraphAlgorithms.findEulerCycle(JTableUtils.readIntMatrixFromJTable(tableArray), 0);
                    printCycle(EulerCycle);
                } else
                {
                    textAreaSystemOut.setText("Graph is not Euler\n" + "There is no Euler cycle");
                }
            } catch (ParseException parseException)
            {
                parseException.printStackTrace();
            }
        });

        clearButton.addActionListener(e ->
        {
            textAreaSystemOut.setText(null);
        });
    }


    private void printCycle(Stack<Integer> eulerCycle)
    {
        textAreaSystemOut.setText("Your Euler cycle: ");
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
        panelMain.setInheritsPopupMenu(true);
        final JSplitPane splitPane1 = new JSplitPane();
        panelMain.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel1);
        panelGraphPainterContainer = new JPanel();
        panelGraphPainterContainer.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panelGraphPainterContainer, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setLeftComponent(panel2);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tableArray = new JTable();
        scrollPane1.setViewportView(tableArray);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        createAGraphButton = new JButton();
        createAGraphButton.setText("Create a graph");
        panel3.add(createAGraphButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clearButton = new JButton();
        clearButton.setText("Clear");
        panel3.add(clearButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        findEulerCycleButton = new JButton();
        findEulerCycleButton.setText("Find Euler cycle");
        panel3.add(findEulerCycleButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkEulerGraphButton = new JButton();
        checkEulerGraphButton.setText("Check graph");
        panel3.add(checkEulerGraphButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel2.add(scrollPane2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textAreaSystemOut = new JTextArea();
        scrollPane2.setViewportView(textAreaSystemOut);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return panelMain;
    }

}
