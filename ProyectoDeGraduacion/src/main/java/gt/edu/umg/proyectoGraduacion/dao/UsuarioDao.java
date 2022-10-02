package gt.edu.umg.proyectoGraduacion.dao;

import gt.edu.umg.proyectoGraduacion.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioDao extends JpaRepository<Usuario, Long>{
    Usuario findByUsername(String username);
}
