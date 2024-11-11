
package com.rintisa.util;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.AbstractBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModernUIUtils {
    private static final Logger logger = LoggerFactory.getLogger(ModernUIUtils.class);

    // Rutas base para iconos
    private static final String ICONS_PATH = "/icons/";
    
    // Tamaños predefinidos para iconos
    public static final int ICON_SMALL = 16;
    public static final int ICON_MEDIUM = 24;
    public static final int ICON_LARGE = 32;

    // Colores de la aplicación
    public static final Color PRIMARY_COLOR = new Color(24, 119, 242); // Azul moderno
    public static final Color SECONDARY_COLOR = new Color(228, 230, 235);
    public static final Color SUCCESS_COLOR = new Color(45, 164, 78);
    public static final Color ERROR_COLOR = new Color(220, 53, 69);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);
    public static final Color BACKGROUND_COLOR = new Color(246, 247, 249);
    
    
    
    
    
    // Método para cargar y redimensionar iconos
    public static ImageIcon loadIcon(String name, int size) {
        try {
            String path = "/icons/" + name + ".png";
            ImageIcon originalIcon = new ImageIcon(ModernUIUtils.class.getResource(path));
            if (originalIcon.getIconWidth() == -1) {
                logger.error("No se pudo cargar el icono: " + path);
                return null;
            }
            Image scaledImage = originalIcon.getImage().getScaledInstance(
                size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (Exception e) {
            logger.error("Error al cargar icono: " + name, e);
            return null;
        }
    }

    // Método para crear botones modernos con iconos
    public static JButton createStyledButton(String text, String iconName, Color backgroundColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(backgroundColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(backgroundColor.brighter());
                } else {
                    g2.setColor(backgroundColor);
                }
                
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 5, 5);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        // Cargar icono si se especifica
        if (iconName != null && !iconName.isEmpty()) {
            try {
                // Cargar icono desde recursos
                ImageIcon originalIcon = new ImageIcon(ModernUIUtils.class.getResource("/icons/" + iconName + ".png"));
                
                // Redimensionar icono a 16x16
                Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);
                
                button.setIcon(scaledIcon);
            } catch (Exception e) {
                logger.error("Error al cargar icono: " + iconName, e);
            }
        }

        // Estilo del botón
        button.setOpaque(false); // Importante para el fondo personalizado
        button.setContentAreaFilled(false); // Importante para el fondo personalizado
        button.setBorderPainted(false); // Quitar el borde pintado
        
        button.setBackground(backgroundColor);
        button.setForeground(backgroundColor.equals(PRIMARY_COLOR) ? Color.WHITE : Color.BLACK);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setIconTextGap(8);

        // Configuración del botón
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        
        // Tamaño y padding
        button.setPreferredSize(new Dimension(100, 30)); // Tamaño fijo
        button.setMinimumSize(new Dimension(100, 30));
        button.setMaximumSize(new Dimension(150, 30));
        
        // Margen interior
        button.setMargin(new Insets(5, 10, 5, 10));
        
        // Espacio entre icono y texto
        button.setIconTextGap(8);
        
        // Fuente y colores
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;      
    }

    // Método para crear botones primarios
    public static JButton createPrimaryButton(String text, String iconName) {
        JButton button = createStyledButton(text, iconName, PRIMARY_COLOR);
        // Ajustar el tamaño basado en el contenido
        button.setPreferredSize(new Dimension(
            button.getPreferredSize().width + 20, // Añadir padding horizontal
            30 // Altura fija
        ));
        return button;
    }

    public static JButton createSecondaryButton(String text, String iconName) {
        JButton button = createStyledButton(text, iconName, SECONDARY_COLOR);
        button.setForeground(Color.BLACK); // Texto negro para botones secundarios
        button.setPreferredSize(new Dimension(
            button.getPreferredSize().width + 20,
            30
        ));
        return button;
    }
    
    
    
    private static Color adjustBrightness(Color color, double factor) {
        int r = Math.min(255, (int)(color.getRed() * factor));
        int g = Math.min(255, (int)(color.getGreen() * factor));
        int b = Math.min(255, (int)(color.getBlue() * factor));
        return new Color(r, g, b);
    }
      
      
      
    

    // Método para crear un campo de búsqueda moderno
    public static JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Color.WHITE);
        
        // Crear campo de búsqueda
        JTextField searchField = createModernTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        
        // Agregar icono de búsqueda
        JLabel searchIcon = new JLabel(loadIcon("search", ICON_SMALL));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        
        // Agregar placeholder
        TextPrompt placeholder = new TextPrompt("Buscar...", searchField);
        placeholder.changeAlpha(0.5f);
        
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Borde redondeado
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR),
            BorderFactory.createEmptyBorder(5, 0, 5, 8)
        ));

        return searchPanel;
    }

    // Método para estilizar tablas
    public static void setupModernTable(JTable table) {
        // Configuración básica
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(40);
        table.setFillsViewportHeight(true);
        
        // Colores de selección
        table.setSelectionBackground(PRIMARY_COLOR.brighter());
        table.setSelectionForeground(Color.WHITE);

        // Estilo del header
        JTableHeader header = table.getTableHeader();
        header.setBackground(BACKGROUND_COLOR);
        header.setForeground(Color.BLACK);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, SECONDARY_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // Renderizador personalizado para las celdas
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, 
                        isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : BACKGROUND_COLOR);
                    setForeground(Color.BLACK);
                }
                
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                
                // Estado con iconos
                if (value != null && value.toString().contains("Activo")) {
                    setIcon(loadIcon("check", ICON_SMALL));
                } else if (value != null && value.toString().contains("Inactivo")) {
                    setIcon(loadIcon("cancel", ICON_SMALL));
                } else {
                    setIcon(null);
                }
                
                return c;
            }
        };
        
        table.setDefaultRenderer(Object.class, renderer);
    }

    // Clase auxiliar para placeholder en campos de texto
    private static class TextPrompt extends JLabel implements FocusListener {
        private final JTextField textField;
        private boolean showPromptOnce;
        private int alpha = 128;

        public TextPrompt(String text, JTextField textField) {
            this.textField = textField;
            setText(text);
            setFont(textField.getFont());
            setForeground(textField.getForeground());
            setHorizontalAlignment(JLabel.LEADING);
            textField.addFocusListener(this);
            textField.add(this);
        }

        public void changeAlpha(float alpha) {
            this.alpha = (int) (alpha * 255);
            repaint();
        }

        @Override
        public void focusGained(FocusEvent e) {
            setVisible(false);
        }

        @Override
        public void focusLost(FocusEvent e) {
            setVisible(textField.getText().length() == 0);
        }
    }

    // Método para crear campos de texto modernos
    public static JTextField createModernTextField() {
        JTextField textField = new JTextField();
        textField.setBackground(Color.WHITE);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return textField;
    }

    // Método para mostrar mensajes tipo toast
    public static void showToast(Component parent, String message, boolean isError) {
        JDialog toast = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent));
        toast.setUndecorated(true);
        
        // Panel del mensaje
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(isError ? new Color(255, 235, 238) : new Color(232, 245, 233));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isError ? ERROR_COLOR : SUCCESS_COLOR),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        // Icono
        JLabel iconLabel = new JLabel(loadIcon(isError ? "error" : "success", ICON_SMALL));
        panel.add(iconLabel, BorderLayout.WEST);

        // Mensaje
        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(isError ? ERROR_COLOR : SUCCESS_COLOR);
        panel.add(messageLabel, BorderLayout.CENTER);

        toast.add(panel);
        toast.pack();

        // Posicionar en la esquina inferior derecha
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        toast.setLocation(
            screenSize.width - toast.getWidth() - 20,
            screenSize.height - toast.getHeight() - 40
        );

        // Mostrar y ocultar con temporizador
        toast.setVisible(true);
        new Timer(3000, e -> {
            toast.dispose();
        }).start();
    }
    
    public static void addShadow(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(),
            component.getBorder()
        ));
    }
    
    private static class ShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Crear sombra suave
            int shadowSize = 3;
            for (int i = 0; i < shadowSize; i++) {
                g2.setColor(new Color(0, 0, 0, ((shadowSize - i) * 10)));
                g2.drawRoundRect(x + i, y + i, width - (i * 2), height - (i * 2), 10, 10);
            }
            
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 4, 4);
        }
    }
    
    
    
    
}