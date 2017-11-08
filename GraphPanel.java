import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.List;

public class GraphPanel extends JComponent {
    public static int count;
    private static final int WIDE = 640;
    private static final int HIGH = 480;
    private static final int RADIUS = 35;
    private static final Random rnd = new Random();
    private ControlPanel control = new ControlPanel();
    private int radius = RADIUS;
    private Kind kind = Kind.Circular;
    private List<Node> nodes = new ArrayList<>();
    private List<Node> selected = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private Point mousePt = new Point(WIDE / 2, HIGH / 2);
    private Rectangle mouseRect = new Rectangle();
    private boolean selecting = false;
    private boolean connecting = false;
    private ArrayList<String> caracteres = new ArrayList<>();
    private Node aux;
    private Point aux2;

    public static void main(String[] args) throws Exception {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                JFrame f = new JFrame("GraphPanel");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                GraphPanel gp = new GraphPanel();
                f.add(gp.control, BorderLayout.NORTH);
                f.add(new JScrollPane(gp), BorderLayout.CENTER);
                f.getRootPane().setDefaultButton(gp.control.defaultButton);
                f.pack();
                f.setLocationByPlatform(true);
                f.setVisible(true);
            }
        });
    }

    public GraphPanel() {
        this.setOpaque(true);
        this.addMouseListener(new MouseHandler());
        this.addMouseMotionListener(new MouseMotionHandler());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDE, HIGH);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(new Color(0x00f0f0f0));
        g.fillRect(0, 0, getWidth(), getHeight());
        for (Edge e : edges) {
            e.draw(g);
        }
        for (Node n : nodes) {
            n.draw(g);
        }
        if (selecting) {
            g.setColor(Color.darkGray);
            g.drawRect(mouseRect.x, mouseRect.y,
                    mouseRect.width, mouseRect.height);
        }
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            selecting = false;
            mouseRect.setBounds(0, 0, 0, 0);
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
            e.getComponent().repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mousePt = e.getPoint();
            if (e.isShiftDown()) {
                Node.selectToggle(nodes, mousePt);
            } else if (e.isPopupTrigger()) {
                Node.selectOne(nodes, mousePt);
                showPopup(e);
            } else if (Node.selectOne(nodes, mousePt)) {
                selecting = false;
            } else {
                Node.selectNone(nodes);
                selecting = true;
            }
            if (connecting) {
                Node.getSelected(nodes, selected);
                if (selected.size() > 0) {
                    selected.get(0);
                    connecting = false;
                    String s1 = JOptionPane.showInputDialog("Caracteres para transición:");
                    if (s1 != null && !s1.equals("")) {
                        Edge newEdge = new Edge(aux, selected.get(0));
                        for (int j = 0; j < edges.size(); j++) {
                            Edge edge = edges.get(j);
                            if (edge.n1 == aux && edge.n2 == selected.get(0)) {
                                edge.setCaracteres(s1.trim().split(","));
                                return;
                            } else if (edge.n1 == aux) {
                                for (String string : s1.trim().split(",")) {
                                    for (String string2 : edge.getCaracteres()) {
                                        if (string.equals(string2)) {
                                            edge.setCaracteres(removeElements(edge.getCaracteres(), string));
                                        }
                                    }
                                }
                            }
                            if (edge.getCaracteres().length == 0) {
                                e.getComponent().repaint();
                                ListIterator<Edge> iter = edges.listIterator();
                                while (iter.hasNext()) {
                                    Edge eb = iter.next();
                                    if (eb == edge) {
                                        iter.remove();
                                        break;
                                    }
                                }
                            }
                        }
                        edges.add(newEdge);
                        edges.get(edges.size() - 1).setCaracteres(s1.trim().split(","));
                    }
                }
            }
            e.getComponent().repaint();

        }

        public String[] removeElements(String[] input, String deleteMe) {
            ArrayList result = new ArrayList<>();

            for (String item : input)
                if (!deleteMe.equals(item))
                    result.add(item);

            return (String[]) result.toArray(new String[result.size()]);
        }

        private void showPopup(MouseEvent e) {
            control.popup.show(e.getComponent(), e.getX(), e.getY());
        }

    }


    private class MouseMotionHandler extends MouseMotionAdapter {

        Point delta = new Point();

        @Override
        public void mouseDragged(MouseEvent e) {
            if (selecting) {
                mouseRect.setBounds(
                        Math.min(mousePt.x, e.getX()),
                        Math.min(mousePt.y, e.getY()),
                        Math.abs(mousePt.x - e.getX()),
                        Math.abs(mousePt.y - e.getY()));
                Node.selectRect(nodes, mouseRect);
            } else {
                delta.setLocation(
                        e.getX() - mousePt.x,
                        e.getY() - mousePt.y);
                Node.updatePosition(nodes, delta);
                mousePt = e.getPoint();
            }
            e.getComponent().repaint();
        }
    }

    private class ControlPanel extends JToolBar {
        private Action newNode = new NewNodeAction("Nuevo estado");
        private Action clearAll = new ClearAction("Limpiar panel");
        private Action color = new ColorAction("Color");
        private Action connect = new ConnectAction("Conectar");
        private Action delete = new DeleteAction("Eliminar");
        private Action random = new RandomAction("Generar 3 estados");
        private Action estadoFinal = new EstadoFinalAction("Estado Final");
        private Action estadoInicial = new EstadoInicial("Estado Inicial");
        private Action showTableAction = new ShowTableAction("Mostrar tabla");
        private Action Ingresar = new Ingresar("Ingresar cadenas");
        private Action minimizar = new minimizarAction("Minimizar AFD");
        private JButton defaultButton = new JButton(newNode);
        private ColorIcon hueIcon = new ColorIcon(Color.blue);
        private JPopupMenu popup = new JPopupMenu();

        ControlPanel() {
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            this.setBackground(Color.lightGray);
            this.add(defaultButton);
            this.add(new JButton(clearAll));
            this.add(new JButton(color));
            this.add(new JLabel(hueIcon));
            this.add(new JButton(random));
            this.add(new JButton(showTableAction));
            this.add(new JButton(Ingresar));
            this.add(new JButton(minimizar));
            popup.add(new JMenuItem(connect));
            popup.add(new JMenuItem(delete));
            popup.add(new JMenuItem(estadoFinal));
            popup.add(new JMenuItem(estadoInicial));


        }
    }

    private class ClearAction extends AbstractAction {

        public ClearAction(String name) {

            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            nodes.clear();
            edges.clear();
            repaint();
            count = 0;
        }
    }

    private class minimizarAction extends AbstractAction {
        public minimizarAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                DFA dfa = MinimizeAFD.getDFA(getJTable());
                for (int i = 0; i < nodes.size(); i++) {
                    Node node = nodes.get(i);
                    if (node.estadoFinal == true) {
                        dfa.IsFinal[i + 1] = true;
                    } else {
                        dfa.IsFinal[i + 1] = false;
                    }
                    if (node.estadoInicial == true) {
                        dfa.StartState = i + 1;
                    }
                }
                if(dfa.StartState==0){
                    throw  new Exception();
                }
                for (int i = 1; i <dfa.IsFinal.length; i++) {
                    if(dfa.IsFinal[i]==true){
                        break;
                    }else if(i==dfa.IsFinal.length -1){
                        throw  new Exception();
                    }
                }
                HashMap<Pair, Boolean> pairs = MinimizeAFD.setupMarkedPairs(dfa);
                MinimizeAFD.processPairs(dfa, pairs);
                int[] e_class = MinimizeAFD.createEqClasses(dfa, pairs);
                JOptionPane.showMessageDialog(null, MinimizeAFD.outputResults(dfa, pairs, e_class));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,"Datos incorrectos","ERROR",JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    private class EstadoInicial extends AbstractAction {
        public EstadoInicial(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            Node.getSelected(nodes, selected);
            if (selected.size() > 0) {
                for (int i = 0; i < nodes.size(); ++i) {
                    Node no = nodes.get(i);
                    if (no.estadoInicial) {
                        no.setEstadoInicial(false);
                    }

                }
                Node n1 = selected.get(0);
                n1.setEstadoInicial(!n1.estadoInicial);
                repaint();
            }
        }
    }

    private class EstadoFinalAction extends AbstractAction {
        public EstadoFinalAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            Node.getSelected(nodes, selected);
            for (int i = 0; i < selected.size(); ++i) {
                Node n1 = selected.get(i);
                n1.setEstadoFinal(!n1.estadoFinal);
            }
            repaint();

        }
    }

    private class ColorAction extends AbstractAction {

        public ColorAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            Color color = control.hueIcon.getColor();
            color = JColorChooser.showDialog(
                    GraphPanel.this, "Choose a color", color);
            if (color != null) {
                Node.updateColor(nodes, color);
                control.hueIcon.setColor(color);
                control.repaint();
                repaint();
            }
        }
    }

    private class ConnectAction extends AbstractAction {

        public ConnectAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            Node.getSelected(nodes, selected);

            if (selected.size() > 0) {
                Node n1 = selected.get(0);
                connecting = true;
                aux = n1;
                repaint();
            }
        }
    }

    private class DeleteAction extends AbstractAction {

        public DeleteAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            ListIterator<Node> iter = nodes.listIterator();
            while (iter.hasNext()) {
                Node n = iter.next();
                if (n.isSelected()) {
                    deleteEdges(n);
                    iter.remove();
                }
            }
            repaint();
        }

        private void deleteEdges(Node n) {
            ListIterator<Edge> iter = edges.listIterator();
            while (iter.hasNext()) {
                Edge e = iter.next();
                if (e.n1 == n || e.n2 == n) {
                    iter.remove();
                }
            }
        }
    }

    private class NewNodeAction extends AbstractAction {

        public NewNodeAction(String name) {
            super(name);


        }

        public void actionPerformed(ActionEvent e) {
            Node.selectNone(nodes);
            Point p = mousePt.getLocation();
            Color color = new Color(rnd.nextInt());
            Node n = new Node(p, radius, color, kind);
            n.setSelected(true);
            nodes.add(n);
            repaint();
        }
    }

    private class RandomAction extends AbstractAction {

        public RandomAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < 3; i++) {
                Point p = new Point(rnd.nextInt(getWidth()), rnd.nextInt(getHeight()));
                nodes.add(new Node(p, radius, new Color(rnd.nextInt()), kind));
            }
            repaint();
        }
    }

    private class Ingresar extends AbstractAction {

        public Ingresar(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            caracteres.clear();
            for (Edge edge : edges) {
                for (String s1 : edge.getCaracteres()) {
                    if (!caracteres.contains(s1)) {
                        caracteres.add(s1);
                    }
                }
            }
            caracteres.sort(String::compareTo);
            String table[][] = new String[nodes.size()][caracteres.size()];
            for (int i = 0; i < nodes.size(); i++) {

                for (int j = 0; j < edges.size(); j++) {
                    String[] cA = edges.get(j).getCaracteres();
                    Node n1 = edges.get(j).n1;
                    Node n2 = edges.get(j).n2;
                    if (n1 == nodes.get(i)) {
                        for (int o = 0; o < cA.length; o++) {
                            int dj = caracteres.indexOf(cA[o]);
                            table[i][dj] = "" + n2.countnodo;
                        }
                    }
                }
            }

            String inicial = "";
            for (Node node : nodes) {
                if (node.estadoInicial) {
                    inicial = "" + node.countnodo;
                }
            }
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<String> arrayList1 = new ArrayList<>();

            for (Node node : nodes) {
                if (node.estadoFinal) {
                    arrayList.add("" + node.countnodo);
                }
                arrayList1.add("" + node.countnodo);
            }
            String x[] = new String[arrayList.size()];
            for (int y = 0; y < arrayList.size(); y++) {
                x[y] = arrayList.get(y);
            }
            arrayList1.sort(String::compareTo);
            AFD unAFD = new AFD(table, caracteres, inicial, nodes.size(), x, arrayList1);
            //VARIABLES
            File file = null; //
            String total = "";
            //Panel para agarrar el archivo que se va a scannear
            JButton open = new JButton();
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setDialogTitle("Selecionar archivo de entradara");
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (jFileChooser.showOpenDialog(open) == JFileChooser.APPROVE_OPTION) {
                file = jFileChooser.getSelectedFile();
            }
            try {
                BufferedReader bufferedReader;
                bufferedReader = new BufferedReader(new FileReader(file.toString()));
                while (true) {
                    String Linea = bufferedReader.readLine();
                    if (Linea == null) {
                        break;
                    }
                    total += "La palabra " + Linea + " es : " + (unAFD.checkWord(Linea) ? "valida" : "no valida") + "\n";
                }
                JTextArea textArea = new JTextArea(total);
                JScrollPane scrollPane = new JScrollPane(textArea);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                scrollPane.setPreferredSize(new Dimension(500, 500));
                JOptionPane.showMessageDialog(null, scrollPane, "Resultados", JOptionPane.NO_OPTION);
            } catch (Exception ex) {
                //Hubo un error leyendo el archivo
                JOptionPane.showMessageDialog(null, "Datos incorrectos", "ERROR", JOptionPane.ERROR_MESSAGE);
            }


        }
    }

    private JTable getJTable() {
        caracteres.clear();
        for (Edge edge : edges) {
            for (String s1 : edge.getCaracteres()) {
                if (!caracteres.contains(s1)) {
                    caracteres.add(s1); //se leen todos los caracters que existan
                }
            }
        }
        caracteres.sort(String::compareTo); //ordeno los caracteres
        String table[][] = new String[nodes.size()][caracteres.size()]; //se genera la tabla de tamaño
        for (int i = 0; i < nodes.size(); i++) {

            for (int j = 0; j < edges.size(); j++) {
                String[] cA = edges.get(j).getCaracteres();
                Node n1 = edges.get(j).n1;
                Node n2 = edges.get(j).n2;
                if (n1 == nodes.get(i)) {
                    for (int o = 0; o < cA.length; o++) {
                        int dj = caracteres.indexOf(cA[o]);
                        table[i][dj] = "" + n2.countnodo;
                    }
                }
            }
        }


        Object[][] rows = new Object[nodes.size()][caracteres.size() + 1];
        Object[] cols = new Object[caracteres.size() + 1];


        cols[0] = "q";
        for (int o = 0; o < caracteres.size(); o++) {
            cols[o + 1] = caracteres.get(o);
        }
        for (int i = 0; i < nodes.size(); i++) {
            rows[i][0] = nodes.get(i).countnodo;

        }
        for (int i = 0; i < nodes.size(); i++) {
            for (int o = 0; o < caracteres.size(); o++) {
                rows[i][o + 1] = table[i][o];
            }
        }


        JTable tablex = new JTable(rows, cols);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        for(int x=0;x<tablex.getColumnCount();x++){
            tablex.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
        }

        return tablex;
    }

    private class ShowTableAction extends AbstractAction {
        public ShowTableAction(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null, new JScrollPane(getJTable()));
        }
    }

    /**
     * The kind of node in a graph.
     */
    private enum Kind {
        Circular
    }

    /**
     * An Edge is a pair of Nodes.
     */
    private static class Edge {

        private Node n1;
        private Node n2;
        private String[] caracteres;

        public Edge(Node n1, Node n2) {
            this.n1 = n1;
            this.n2 = n2;
        }

        public String[] getCaracteres() {
            return caracteres;
        }

        public void setCaracteres(String[] caracteres) {
            this.caracteres = caracteres;
        }

        public void draw(Graphics g) {

            g.setColor(Color.darkGray);
            if (n1 == n2) {
                Point p1 = n1.b.getLocation();
                g.drawArc(p1.x - 15, p1.y + 10, RADIUS, RADIUS, 45, 270);
            } else {
                Point p1 = n1.getLocation();
                Point p2 = n2.getLocation();
                g.drawLine(p1.x, p1.y, p2.x, p2.y);

            }

        }
    }

    /**
     * A Node represents a node in a graph.
     */
    private static class Node {
        public int countnodo;
        public boolean estadoFinal;
        public boolean estadoInicial;
        private Point p;
        private int r;
        private Color color;
        private Kind kind;
        private boolean selected = false;
        private Rectangle b = new Rectangle();

        public void setEstadoInicial(boolean b) {


            this.estadoInicial = b;
        }

        public void setEstadoFinal(boolean b) {
            this.estadoFinal = b;
        }

        /**
         * Construct a new node.
         */
        public Node(Point p, int r, Color color, Kind kind) {
            this.p = p;
            this.r = r;
            this.color = color;
            this.kind = kind;
            setBoundary(b);
            countnodo = count;
            count++;
            estadoFinal = false;
        }

        /**
         * Calculate this node's rectangular boundary.
         */
        private void setBoundary(Rectangle b) {
            b.setBounds(p.x - r, p.y - r, 2 * r, 2 * r);
        }

        /**
         * Draw this node.
         */
        public void draw(Graphics g) {
            g.setColor(this.color);
            g.drawString("q" + countnodo, b.x, b.y);
            if (estadoFinal) {
                g.drawString("F", b.x + b.height, b.y + b.height);
            }
            if (estadoInicial) {
                g.drawString("I", b.x, b.y + b.height);
            }
            g.fillOval(b.x, b.y, b.width, b.height);
            if (selected) {
                g.setColor(Color.darkGray);
                g.drawRect(b.x, b.y, b.width, b.height);
            }
        }

        /**
         * Return this node's location.
         */
        public Point getLocation() {
            return p;
        }

        /**
         * Return true if this node contains p.
         */
        public boolean contains(Point p) {
            return b.contains(p);
        }

        /**
         * Return true if this node is selected.
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * Mark this node as selected.
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        /**
         * Collected all the selected nodes in list.
         */
        public static void getSelected(List<Node> list, List<Node> selected) {
            selected.clear();
            for (Node n : list) {
                if (n.isSelected()) {
                    selected.add(n);
                }
            }
        }

        /**
         * Select no nodes.
         */
        public static void selectNone(List<Node> list) {
            for (Node n : list) {
                n.setSelected(false);
            }
        }

        /**
         * Select a single node; return true if not already selected.
         */
        public static boolean selectOne(List<Node> list, Point p) {
            for (Node n : list) {
                if (n.contains(p)) {
                    if (!n.isSelected()) {
                        Node.selectNone(list);
                        n.setSelected(true);
                    }
                    return true;
                }
            }
            return false;
        }

        /**
         * Select each node in r.
         */
        public static void selectRect(List<Node> list, Rectangle r) {
            for (Node n : list) {
                n.setSelected(r.contains(n.p));
            }
        }

        /**
         * Toggle selected state of each node containing p.
         */
        public static void selectToggle(List<Node> list, Point p) {
            for (Node n : list) {
                if (n.contains(p)) {
                    n.setSelected(!n.isSelected());
                }
            }
        }

        /**
         * Update each node's position by d (delta).
         */
        public static void updatePosition(List<Node> list, Point d) {
            for (Node n : list) {
                if (n.isSelected()) {
                    n.p.x += d.x;
                    n.p.y += d.y;
                    n.setBoundary(n.b);
                }
            }
        }

        /**
         * Update each node's color.
         */
        public static void updateColor(List<Node> list, Color color) {
            for (Node n : list) {
                if (n.isSelected()) {
                    n.color = color;
                }
            }
        }
    }

    private static class ColorIcon implements Icon {

        private static final int WIDE = 20;
        private static final int HIGH = 20;
        private Color color;

        public ColorIcon(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, WIDE, HIGH);
        }

        public int getIconWidth() {
            return WIDE;
        }

        public int getIconHeight() {
            return HIGH;
        }
    }
}