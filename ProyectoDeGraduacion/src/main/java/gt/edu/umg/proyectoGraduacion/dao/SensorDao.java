/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package gt.edu.umg.proyectoGraduacion.dao;

import gt.edu.umg.proyectoGraduacion.domain.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SensorDao extends JpaRepository<Sensor, Long>{
    
}
