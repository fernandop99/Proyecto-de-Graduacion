/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gt.edu.umg.proyectoGraduacion.servicio;

import gt.edu.umg.proyectoGraduacion.dao.MedicionDao;
import gt.edu.umg.proyectoGraduacion.domain.Medicion;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicionServiceImpl implements MedicionService{
    
    @Autowired
    private MedicionDao medicionDao;
    
    @Override
    @Transactional(readOnly = true)
    public List<Medicion> listarMediciones(){
        return (List<Medicion>) medicionDao.findAll();
    }
    
}
