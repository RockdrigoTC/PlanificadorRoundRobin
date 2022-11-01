package planificadorroundrobin;

import javax.swing.JFrame;

/**
 * @author Equipo 1
 */
public class PlanificadorRoundRobin {

  public static void main(String[] args) {
    PanelProceso panel = new PanelProceso();
    JFrame f = new JFrame("Planificador Round-Robin");
    f.setSize(700, 430);
    f.add(panel);
    f.setLocationRelativeTo(null);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(true);
    
  }

}
