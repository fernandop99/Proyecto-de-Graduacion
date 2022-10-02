/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package gt.edu.umg.proyectoGraduacion.dao;

import gt.edu.umg.proyectoGraduacion.domain.Medicion;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MedicionDao extends JpaRepository<Medicion, Long>{
    
}
