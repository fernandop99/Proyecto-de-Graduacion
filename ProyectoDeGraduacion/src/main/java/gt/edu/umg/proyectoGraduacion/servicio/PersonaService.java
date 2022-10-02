package gt.edu.umg.proyectoGraduacion.servicio;

import java.util.List;
import gt.edu.umg.proyectoGraduacion.domain.Persona;

public interface PersonaService {
    
    public List<Persona> listarPersonas();
    
    public void guardar(Persona persona);
    
    public void eliminar(Persona persona);
    
    public Persona encontrarPersona(Persona persona);
}
