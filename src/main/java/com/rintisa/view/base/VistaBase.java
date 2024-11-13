
package com.rintisa.view.base;

import com.rintisa.controller.UsuarioController;
import com.rintisa.exception.DatabaseException;
import com.rintisa.model.Pantalla;
import com.rintisa.security.PermisosPantallaManager;
import com.rintisa.service.interfaces.IPermisosPantallaService;
import com.rintisa.util.IconManager;
import com.rintisa.util.ModernUIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

public abstract class VistaBase extends JPanel {
    protected final PermisosPantallaManager permisoManager;
    protected final String rolActual;
    protected final Pantalla pantalla;
    protected JToolBar toolbar;
    protected JPanel contentPanel;
    protected Map<String, JButton> botones;

    public VistaBase(PermisosPantallaManager permisoManager, String rolActual, Pantalla pantalla) {
        this.permisoManager = permisoManager;
        this.rolActual = rolActual;
        this.pantalla = pantalla;
        this.botones = new HashMap<>();
        
        inicializar();
    }

    private void inicializar() {
        if (!permisoManager.tieneAcceso(rolActual, pantalla)) {
            mostrarErrorAcceso();
            return;
        }

        setLayout(new BorderLayout());
        crearComponentes();
        configurarPermisos();
        configurarEventos();
    }

    protected void crearComponentes() {
        // Toolbar
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(toolbar, BorderLayout.NORTH);

        // Panel principal
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(contentPanel, BorderLayout.CENTER);

        // Botones estándar
        JButton btnNuevo = crearBoton("Nuevo", "new", e -> accionNuevo());
        JButton btnEditar = crearBoton("Editar", "edit", e -> accionEditar());
        JButton btnEliminar = crearBoton("Eliminar", "delete", e -> accionEliminar());

        botones.put("nuevo", btnNuevo);
        botones.put("editar", btnEditar);
        botones.put("eliminar", btnEliminar);

        toolbar.add(btnNuevo);
        toolbar.addSeparator();
        toolbar.add(btnEditar);
        toolbar.addSeparator();
        toolbar.add(btnEliminar);
    }

    protected JButton crearBoton(String texto, String icono, ActionListener accion) {
        JButton boton = ModernUIUtils.createPrimaryButton(texto, icono);
        boton.addActionListener(accion);
        return boton;
    }

    protected void configurarPermisos() {
        botones.get("editar").setEnabled(permisoManager.puedeEditar(rolActual, pantalla));
        botones.get("eliminar").setEnabled(permisoManager.puedeEliminar(rolActual, pantalla));
    }

    protected void mostrarErrorAcceso() {
        removeAll();
        JPanel errorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel mensajeLabel = new JLabel("No tiene permisos para acceder a esta pantalla");
        mensajeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mensajeLabel.setForeground(Color.RED);
        
        errorPanel.add(mensajeLabel, gbc);
        add(errorPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Métodos abstractos que las pantallas específicas deben implementar
    protected abstract void configurarEventos();
    protected abstract void accionNuevo();
    protected abstract void accionEditar();
    protected abstract void accionEliminar();
}