/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package gt.edu.umg.proyectoGraduacion.servicio;


import gt.edu.umg.proyectoGraduacion.domain.Sensor;
import java.util.List;


public interface SensorService {
    public List<Sensor> listarSensores();
    
    public void guardar(Sensor sensor);
    
    public void eliminar(Sensor sensor);
    
    public Sensor encontrarSensor(Sensor sensor);
}
