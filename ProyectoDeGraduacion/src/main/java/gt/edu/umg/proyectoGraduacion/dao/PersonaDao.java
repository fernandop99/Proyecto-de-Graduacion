package gt.edu.umg.proyectoGraduacion.dao;

import gt.edu.umg.proyectoGraduacion.domain.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaDao extends JpaRepository<Persona, Long>{
    
}
