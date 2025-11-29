package fisiclinica.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Estilos {
    
    // Colores definidos
    public static final Color AZUL_OSCURO = new Color(0, 102, 204); // Azul Base
    public static final Color CELESTE = new Color(135, 206, 250);   // Hover
    public static final Color TEXTO_NEGRO = Color.BLACK;
    public static final Color TEXTO_BLANCO = Color.WHITE;

    /**
     * Aplica el estilo solicitado: Azul por defecto, Celeste al pasar el mouse.
     * Mantiene el texto legible.
     */
    public static void aplicarEstiloBoton(JButton btn) {
        btn.setBackground(AZUL_OSCURO);
        btn.setForeground(TEXTO_BLANCO); // Texto blanco sobre azul se lee mejor
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(CELESTE);
                btn.setForeground(TEXTO_NEGRO); // Texto negro sobre celeste
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(AZUL_OSCURO);
                btn.setForeground(TEXTO_BLANCO);
            }
        });
    }
}
