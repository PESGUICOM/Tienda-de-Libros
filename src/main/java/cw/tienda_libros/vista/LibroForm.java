package cw.tienda_libros.vista;

import cw.tienda_libros.modelo.Libro;
import cw.tienda_libros.servicio.LibroServicio;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Component
public class LibroForm extends JFrame {
    LibroServicio libroServicio;
    private JPanel panel;
    private JTable tablaLibros;
    private JTextField idTexto;
    private JTextField libroTexto;
    private JTextField autorTexto;
    private JTextField precioTexto;
    private JTextField existenciasTexto;
    private JButton agregarButton;
    private JButton modificarButton;
    private JButton eliminarButton;
    private DefaultTableModel tablaModeloLibros;

    @Autowired
    public LibroForm(LibroServicio libroServicio){
        this.libroServicio = libroServicio;
        iniciarForma();
        agregarButton.addActionListener(e -> agregarLibro());
        tablaLibros.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                cargarLibroSeleccionado();
            }
        });
        modificarButton.addActionListener(e -> modificarLibro());
        eliminarButton.addActionListener(e -> eliminarLibro());
    }

    private void iniciarForma(){
        setContentPane(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setSize(900, 700);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension tamanoPantalla = toolkit.getScreenSize();
        int x = (tamanoPantalla.width - getWidth() / 2);
        int y = (tamanoPantalla.height - getHeight() / 2);
        setLocation(x, y);
    }

    private void agregarLibro(){
        //Leer los valores del formulario
        if (libroTexto.getText().isEmpty()){
            mostrarMensaje("Proporciona el nombre del Libro");
            libroTexto.requestFocusInWindow(); //para cambiar el cursor
            return;
        }
        var nombreLibro = libroTexto.getText();
        var autor = autorTexto.getText();
        var precio = Double.parseDouble(precioTexto.getText());
        var existencia = Integer.parseInt(existenciasTexto.getText());
        //Crear el objeto libro
        var libro = new Libro(null, nombreLibro, autor, precio, existencia);
        //También se puede hacer así
        //libro.setNombreLibro(nombreLibro)
        //libro.detAutor(autor)
        //libro.setPrecio(precio)
        //libro.setExistencias(existencias)
        this.libroServicio.guardarLibro(libro);
        mostrarMensaje("Se agregó el Libro...");
        limpiarFormulario();
        listarLibros();
    }

    private void cargarLibroSeleccionado(){
        //Los índices de las columnas inician en 0
        var renglon = tablaLibros.getSelectedRow();
        if (renglon != -1){ //Regresa -1 si no se seleccionó ningún registro
            String idLibro = tablaLibros.getModel().getValueAt(renglon, 0).toString();
            idTexto.setText(idLibro);
            String nombreLibro = tablaLibros.getModel().getValueAt(renglon, 1).toString();
            libroTexto.setText(nombreLibro);
            String autor = tablaLibros.getModel().getValueAt(renglon, 2).toString();
            autorTexto.setText(autor);
            String precio = tablaLibros.getModel().getValueAt(renglon, 3).toString();
            precioTexto.setText(precio);
            String existencias = tablaLibros.getModel().getValueAt(renglon, 4).toString();
            existenciasTexto.setText(existencias);
        }
    }

    private void modificarLibro(){
        if (this.idTexto.getText().isEmpty()){
            mostrarMensaje("Debe seleccionar un registro...");
        } else {
            //Verificamos que nombre del libro no sea nulo
            if (libroTexto.getText().isEmpty()){
                mostrarMensaje("Proporciona el nombre del libro...");
                libroTexto.requestFocusInWindow();
                return;
            }
            //Llenamos el objeto libro a actualizar
            int idLibro = Integer.parseInt(idTexto.getText());
            var nombreLibro = libroTexto.getText();
            var autor = autorTexto.getText();
            var precio = Double.parseDouble(precioTexto.getText());
            var existencias = Integer.parseInt(existenciasTexto.getText());
            var libro = new Libro(idLibro, nombreLibro, autor, precio, existencias);
            libroServicio.guardarLibro(libro);
            mostrarMensaje("Se modificó el libro");
            limpiarFormulario();
            listarLibros();
        }
    }
    private void eliminarLibro(){
        var renglon = tablaLibros.getSelectedRow(); //sabemos que renglón seleccionó el user
        if (renglon != -1){
            String idLibro = tablaLibros.getModel().getValueAt(renglon, 0).toString();
            var libro = new Libro();
            libro.setIdLibro(Integer.parseInt(idLibro));
            libroServicio.eliminarLibro(libro);
            mostrarMensaje("Libro " + idLibro + " eliminado" + libro);
            limpiarFormulario();
            listarLibros();
        } else {
            mostrarMensaje("No se ha seleccionado ningún libro a eliminar...");
        }
    }

    private void limpiarFormulario(){
        libroTexto.setText("");
        autorTexto.setText("");
        precioTexto.setText("");
        existenciasTexto.setText("");
    }

    private void mostrarMensaje(String mensaje){
        JOptionPane.showMessageDialog(this, mensaje);
    }

    //Método para personalizar los componentes del formulario Swing
    private void createUIComponents(){
        //TODO: place custom component creation code here
        //Creamos el elemento idTexto oculto
        idTexto = new JTextField("");
        idTexto.setVisible(false);
        this.tablaModeloLibros = new DefaultTableModel(0, 5) {
            @Override //sobrescribiendo método para no dar click sobre el registro y modificarlo en la lista
            public boolean isCellEditable(int row, int column){return false;}
        };
        String[] cabeceros = {"Id", "Libro", "Autor", "Precio", "Existencias"};
        this.tablaModeloLibros.setColumnIdentifiers(cabeceros);
        //Instanciar el objeto JTable
        this.tablaLibros = new JTable(tablaModeloLibros);
        //Evitar que se seleccionen varios registros de la tabla
        tablaLibros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);//selecciona un solo registro
        listarLibros();
    }

    private void listarLibros(){
        //Limpiar la tabla
        tablaModeloLibros.setRowCount(0);
        //Obtener los libros
        var libros = libroServicio.listarLibros();
        libros.forEach((libro) -> {
            Object[] renglonLibro = {
                    libro.getIdLibro(),
                    libro.getNombreLibro(),
                    libro.getAutor(),
                    libro.getPrecio(),
                    libro.getExistencias()
            };
            this.tablaModeloLibros.addRow(renglonLibro);
        });
    }
}
