package planificadorroundrobin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import util.Archivo;

/**
 *
 * @author Equipo 1
 */
public class PanelProceso extends javax.swing.JPanel {

  static int Quantum;
  final int PRIMERODELISTA = 0;
  int tiempo = 0;
  ArrayList<Proceso> listos = new ArrayList();
  ArrayList<Proceso> bloqueados = new ArrayList();
  ArrayList<Proceso> terminados = new ArrayList();
  ArrayList<Proceso> ejecucion = new ArrayList();

  public PanelProceso() {
    initComponents();
    this.addEventos();
  }

  public void agregarProcesos() {
    try {
      URL url = this.getClass().getResource("/archivo/Procesos.txt");
      File archivo = new File(url.toURI());
      ArrayList<String> lineas = Archivo.leerArchivo(archivo);
      int numeroProcesos = lineas.size();
      for (int i = 0; i < numeroProcesos; i++) {
        String linea = lineas.get(i);
        String[] datos = linea.split(",");
        listos.add(new Proceso(datos[0], datos[1].replace(" ","")));
      }
      while (!terminados.isEmpty()) {
        terminados.remove(PRIMERODELISTA);
      }
      mostrar();
      Thread.sleep(1000);
    } catch (URISyntaxException ex) {
      System.out.println("Error en el archivo: " + ex);
      System.exit(-1);
    } catch (InterruptedException ex) {
      System.out.println("Error de hilo: " + ex);
      System.exit(-1);
    }

    procesar();
  }

  public void mostrar() {
    areaListos.setText("");
    areaBloqueado.setText("");
    areaTerminado.setText("");
    areaEjecutando.setText("");

    for (int i = 0; i < listos.size(); i++) {
      areaListos.append(listos.get(i).procesoID + "\n");
    }
    for (int i = 0; i < bloqueados.size(); i++) {
      areaBloqueado.append(bloqueados.get(i).procesoID + "\n");
    }
    for (int i = 0; i < terminados.size(); i++) {
      areaTerminado.append(terminados.get(i).procesoID + "\n");
    }
    for (int i = 0; i < ejecucion.size(); i++) {
      areaEjecutando.append(ejecucion.get(i).procesoID + "\n");
    }
    if (ejecucion.isEmpty()) {
      campoProceso.setText("");
      campoRafaga.setText("");
    } else {
      campoProceso.setText(ejecucion.get(PRIMERODELISTA).procesoID);
      campoRafaga.setText(ejecucion.get(PRIMERODELISTA).rafaga + "");
    }
    campoQuantum.setText(Quantum + "");
    areaTiempo.setText(tiempo + "");
  }

  public void procesar() {
    tiempo = 0;

    try {
      //DESPACHAR
      while (!listos.isEmpty() || !bloqueados.isEmpty()) {
        Quantum = 5;

        if (!listos.isEmpty()) {
          ejecucion.add(listos.get(PRIMERODELISTA));
          listos.remove(PRIMERODELISTA);
        }

        //EJECUCIÓN
        for (int i = Quantum; i > 0; i--) {
          mostrar();
          Thread.sleep(1500);

          if (!procesarBloqueado()) {
            break;
          }

          Quantum--;
          tiempo++;
          if (!ejecucion.isEmpty()) {
            ejecucion.get(PRIMERODELISTA).rafaga--;
            if (ejecucion.get(PRIMERODELISTA).rafaga == 0) {
              break;
            }
          }
        }
        expulsarDeEjecucion();
        mostrar();
      }
    } catch (InterruptedException e) {
      System.out.println("Error: " + e);
      System.exit(-1);
    }
  }

  //Expulsa o saca de ejecución para finalizar o ingresar a listos
  public void expulsarDeEjecucion() {

    if (!ejecucion.isEmpty()) {
      System.out.println("Proceso: " + ejecucion.get(0).procesoID + " Rafaga: " + ejecucion.get(PRIMERODELISTA).rafaga);

//          EXPIRACIÓN DE TIEMPO
      if (ejecucion.get(PRIMERODELISTA).rafaga != 0) {
        listos.add(ejecucion.get(PRIMERODELISTA));
        ejecucion.remove(PRIMERODELISTA);
      } else {
//            FINALIZACIÓN
        terminados.add(ejecucion.get(PRIMERODELISTA));
        ejecucion.remove(PRIMERODELISTA);
      }
    }
  }

  //Manejo de procesos bloqueados
  private boolean procesarBloqueado() {
    boolean bandera = true;

    //BLOQUEAR
    if (!ejecucion.isEmpty()) {
      if (ejecucion.get(PRIMERODELISTA).bloqueado != 0) {
        if (ejecucion.get(PRIMERODELISTA).solicitud == ejecucion.get(PRIMERODELISTA).rafaga) {
          bloqueados.add(ejecucion.get(PRIMERODELISTA));
          ejecucion.remove(PRIMERODELISTA);
          bandera = false;
        }
      }
    }

    //DESBLOQUEAR O DESPERTAR
    if (!bloqueados.isEmpty()) {
      for (int i = 0; i < bloqueados.size(); i++) {
        if (bloqueados.get(i).bloqueado != 0) {
          bloqueados.get(i).bloqueado--;
        } else {
          listos.add(bloqueados.get(i));
          bloqueados.remove(i);
        }
      }
    }

    return bandera;
  }

  public class Proceso {

    String procesoID = "";
    int rafaga = 0;
    int bloqueado = 0;
    int solicitud = 0;

    public Proceso(String id, String r) {
      procesoID = id;
      rafaga = Integer.parseInt(r);
      bloqueado = aleatorioB();
      solicitud = aleatorioS();
    }

    public int aleatorioS() {
      int nAleatorio = (int) Math.floor(Math.random() * rafaga + 1);
      return nAleatorio;
    }

    public int aleatorioB() {
      int nAleatorio = (int) Math.floor(Math.random() * (rafaga - (rafaga / 2)));
      return nAleatorio;
    }
  }

  class MiHilo extends Thread {

    @Override
    public void run() {
      agregarProcesos();
    }
  }

  public class Oyente implements ActionListener {

    MiHilo hilo;

    public Oyente() {
      hilo = new MiHilo();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (!hilo.isAlive()) {
        if (hilo.getState() == Thread.State.TERMINATED) {
          hilo = new MiHilo();
        }
        hilo.start();
      }
    }
  }

  public void addEventos() {
    botonIniciar.addActionListener(new Oyente());
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    panelPrincipal = new javax.swing.JPanel();
    panelIzquierdo = new javax.swing.JPanel();
    panelDatos = new javax.swing.JPanel();
    etiquetaQuantum = new javax.swing.JLabel();
    campoQuantum = new javax.swing.JTextField();
    etiquetaProceso = new javax.swing.JLabel();
    campoProceso = new javax.swing.JTextField();
    etiquetaRafaga = new javax.swing.JLabel();
    campoRafaga = new javax.swing.JTextField();
    panelListos = new javax.swing.JPanel();
    barraListos = new javax.swing.JScrollPane();
    areaListos = new javax.swing.JTextArea();
    etiquetaListos = new javax.swing.JLabel();
    panelCentro = new javax.swing.JPanel();
    panelEjecutando = new javax.swing.JPanel();
    barraEjecutando = new javax.swing.JScrollPane();
    areaEjecutando = new javax.swing.JTextArea();
    etiquetaEjecutando = new javax.swing.JLabel();
    panelBloqueado = new javax.swing.JPanel();
    barraBloqueado = new javax.swing.JScrollPane();
    areaBloqueado = new javax.swing.JTextArea();
    etiquetaBloqueado = new javax.swing.JLabel();
    panelDerecho = new javax.swing.JPanel();
    panelTerminado = new javax.swing.JPanel();
    barraTerminado = new javax.swing.JScrollPane();
    areaTerminado = new javax.swing.JTextArea();
    etiquetaTerminado = new javax.swing.JLabel();
    panelDetalles = new javax.swing.JPanel();
    etiquetaTiempo = new javax.swing.JLabel();
    barraTiempo = new javax.swing.JScrollPane();
    areaTiempo = new javax.swing.JTextArea();
    botonIniciar = new javax.swing.JButton();
    panelTitulo = new javax.swing.JPanel();
    etiquetaTitulo = new javax.swing.JLabel();

    setLayout(new java.awt.BorderLayout());

    panelPrincipal.setLayout(new java.awt.GridLayout(1, 3));

    panelIzquierdo.setLayout(new java.awt.GridLayout(2, 1));

    etiquetaQuantum.setText("Quantum:");

    campoQuantum.setEditable(false);
    campoQuantum.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    campoQuantum.setText("5");

    etiquetaProceso.setText("Proceso en ejecución:");

    campoProceso.setEditable(false);
    campoProceso.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N

    etiquetaRafaga.setText("Ráfaga:");

    campoRafaga.setEditable(false);
    campoRafaga.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N

    javax.swing.GroupLayout panelDatosLayout = new javax.swing.GroupLayout(panelDatos);
    panelDatos.setLayout(panelDatosLayout);
    panelDatosLayout.setHorizontalGroup(
      panelDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDatosLayout.createSequentialGroup()
        .addGap(0, 60, Short.MAX_VALUE)
        .addGroup(panelDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(etiquetaQuantum, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(etiquetaRafaga, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(etiquetaProceso, javax.swing.GroupLayout.Alignment.TRAILING))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(panelDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(campoProceso, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
          .addComponent(campoQuantum)
          .addComponent(campoRafaga, javax.swing.GroupLayout.Alignment.TRAILING))
        .addGap(4, 4, 4))
    );
    panelDatosLayout.setVerticalGroup(
      panelDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(panelDatosLayout.createSequentialGroup()
        .addGap(1, 1, 1)
        .addGroup(panelDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(campoQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(etiquetaQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(panelDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(etiquetaProceso, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(campoProceso, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(panelDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(etiquetaRafaga, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(campoRafaga, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(91, Short.MAX_VALUE))
    );

    panelIzquierdo.add(panelDatos);

    panelListos.setBorder(javax.swing.BorderFactory.createLineBorder(getBackground(), 10));
    panelListos.setLayout(new java.awt.BorderLayout());

    barraListos.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    barraListos.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    areaListos.setEditable(false);
    areaListos.setColumns(20);
    areaListos.setForeground(new java.awt.Color(51, 51, 255));
    areaListos.setRows(5);
    barraListos.setViewportView(areaListos);

    panelListos.add(barraListos, java.awt.BorderLayout.CENTER);

    etiquetaListos.setFont(new java.awt.Font("Amiri", 0, 18)); // NOI18N
    etiquetaListos.setText("Lista de listos");
    panelListos.add(etiquetaListos, java.awt.BorderLayout.PAGE_START);

    panelIzquierdo.add(panelListos);

    panelPrincipal.add(panelIzquierdo);

    panelCentro.setLayout(new java.awt.GridLayout(2, 1));

    panelEjecutando.setBorder(new javax.swing.border.LineBorder(getBackground(), 50, true));
    panelEjecutando.setLayout(new java.awt.BorderLayout());

    barraEjecutando.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    barraEjecutando.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    areaEjecutando.setEditable(false);
    areaEjecutando.setColumns(20);
    areaEjecutando.setForeground(new java.awt.Color(255, 0, 0));
    areaEjecutando.setRows(5);
    barraEjecutando.setViewportView(areaEjecutando);

    panelEjecutando.add(barraEjecutando, java.awt.BorderLayout.CENTER);

    etiquetaEjecutando.setFont(new java.awt.Font("Amiri", 0, 18)); // NOI18N
    etiquetaEjecutando.setText("Ejecución");
    panelEjecutando.add(etiquetaEjecutando, java.awt.BorderLayout.PAGE_START);

    panelCentro.add(panelEjecutando);

    panelBloqueado.setBorder(javax.swing.BorderFactory.createLineBorder(getBackground(), 10));

    barraBloqueado.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    barraBloqueado.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    areaBloqueado.setEditable(false);
    areaBloqueado.setColumns(20);
    areaBloqueado.setForeground(new java.awt.Color(255, 0, 0));
    areaBloqueado.setRows(5);
    barraBloqueado.setViewportView(areaBloqueado);

    etiquetaBloqueado.setFont(new java.awt.Font("Amiri", 0, 18)); // NOI18N
    etiquetaBloqueado.setText("Bloqueado");

    javax.swing.GroupLayout panelBloqueadoLayout = new javax.swing.GroupLayout(panelBloqueado);
    panelBloqueado.setLayout(panelBloqueadoLayout);
    panelBloqueadoLayout.setHorizontalGroup(
      panelBloqueadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBloqueadoLayout.createSequentialGroup()
        .addGap(43, 43, 43)
        .addGroup(panelBloqueadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(etiquetaBloqueado, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(barraBloqueado, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap())
    );
    panelBloqueadoLayout.setVerticalGroup(
      panelBloqueadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(panelBloqueadoLayout.createSequentialGroup()
        .addGap(19, 19, 19)
        .addComponent(etiquetaBloqueado)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(barraBloqueado, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
        .addGap(20, 20, 20))
    );

    panelCentro.add(panelBloqueado);

    panelPrincipal.add(panelCentro);

    panelDerecho.setLayout(new java.awt.GridLayout(2, 1));

    panelTerminado.setBorder(javax.swing.BorderFactory.createLineBorder(getBackground(), 10));
    panelTerminado.setLayout(new java.awt.BorderLayout());

    barraTerminado.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    barraTerminado.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    areaTerminado.setEditable(false);
    areaTerminado.setColumns(20);
    areaTerminado.setForeground(new java.awt.Color(0, 204, 0));
    areaTerminado.setRows(5);
    barraTerminado.setViewportView(areaTerminado);

    panelTerminado.add(barraTerminado, java.awt.BorderLayout.CENTER);

    etiquetaTerminado.setFont(new java.awt.Font("Amiri", 0, 18)); // NOI18N
    etiquetaTerminado.setText("Terminado");
    panelTerminado.add(etiquetaTerminado, java.awt.BorderLayout.PAGE_START);

    panelDerecho.add(panelTerminado);

    panelDetalles.setBorder(javax.swing.BorderFactory.createLineBorder(getBackground(), 50));

    etiquetaTiempo.setText("Tiempo total:");

    barraTiempo.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    barraTiempo.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    areaTiempo.setEditable(false);
    areaTiempo.setColumns(20);
    areaTiempo.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    areaTiempo.setRows(5);
    barraTiempo.setViewportView(areaTiempo);

    botonIniciar.setText("Iniciar");

    javax.swing.GroupLayout panelDetallesLayout = new javax.swing.GroupLayout(panelDetalles);
    panelDetalles.setLayout(panelDetallesLayout);
    panelDetallesLayout.setHorizontalGroup(
      panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(panelDetallesLayout.createSequentialGroup()
        .addComponent(etiquetaTiempo)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(barraTiempo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDetallesLayout.createSequentialGroup()
        .addComponent(botonIniciar)
        .addGap(41, 41, 41))
    );
    panelDetallesLayout.setVerticalGroup(
      panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDetallesLayout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(etiquetaTiempo, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(barraTiempo, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(25, 25, 25)
        .addComponent(botonIniciar))
    );

    panelDerecho.add(panelDetalles);

    panelPrincipal.add(panelDerecho);

    add(panelPrincipal, java.awt.BorderLayout.CENTER);

    panelTitulo.setBackground(new java.awt.Color(153, 255, 153));
    panelTitulo.setFont(new java.awt.Font("Amiri", 0, 24)); // NOI18N

    etiquetaTitulo.setFont(new java.awt.Font("Amiri", 0, 18)); // NOI18N
    etiquetaTitulo.setText("Round-Robin");
    panelTitulo.add(etiquetaTitulo);

    add(panelTitulo, java.awt.BorderLayout.PAGE_START);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTextArea areaBloqueado;
  private javax.swing.JTextArea areaEjecutando;
  private javax.swing.JTextArea areaListos;
  private javax.swing.JTextArea areaTerminado;
  private javax.swing.JTextArea areaTiempo;
  private javax.swing.JScrollPane barraBloqueado;
  private javax.swing.JScrollPane barraEjecutando;
  private javax.swing.JScrollPane barraListos;
  private javax.swing.JScrollPane barraTerminado;
  private javax.swing.JScrollPane barraTiempo;
  private javax.swing.JButton botonIniciar;
  private javax.swing.JTextField campoProceso;
  private javax.swing.JTextField campoQuantum;
  private javax.swing.JTextField campoRafaga;
  private javax.swing.JLabel etiquetaBloqueado;
  private javax.swing.JLabel etiquetaEjecutando;
  private javax.swing.JLabel etiquetaListos;
  private javax.swing.JLabel etiquetaProceso;
  private javax.swing.JLabel etiquetaQuantum;
  private javax.swing.JLabel etiquetaRafaga;
  private javax.swing.JLabel etiquetaTerminado;
  private javax.swing.JLabel etiquetaTiempo;
  private javax.swing.JLabel etiquetaTitulo;
  private javax.swing.JPanel panelBloqueado;
  private javax.swing.JPanel panelCentro;
  private javax.swing.JPanel panelDatos;
  private javax.swing.JPanel panelDerecho;
  private javax.swing.JPanel panelDetalles;
  private javax.swing.JPanel panelEjecutando;
  private javax.swing.JPanel panelIzquierdo;
  private javax.swing.JPanel panelListos;
  private javax.swing.JPanel panelPrincipal;
  private javax.swing.JPanel panelTerminado;
  private javax.swing.JPanel panelTitulo;
  // End of variables declaration//GEN-END:variables

}
