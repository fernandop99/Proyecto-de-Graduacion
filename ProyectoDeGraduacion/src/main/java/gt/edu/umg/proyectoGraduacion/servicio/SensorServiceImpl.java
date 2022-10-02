package gt.edu.umg.proyectoGraduacion.servicio;

import java.util.List;
import gt.edu.umg.proyectoGraduacion.dao.SensorDao;
import gt.edu.umg.proyectoGraduacion.domain.Sensor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorServiceImpl implements SensorService {

    @Autowired
    private SensorDao sensorDao;
    
    @Override
    @Transactional(readOnly = true)
    public List<Sensor> listarSensores() {
        return (List<Sensor>) sensorDao.findAll();
    }

    @Override
    @Transactional
    public void guardar(Sensor sensor) {
        sensorDao.save(sensor);
    }

    @Override
    @Transactional
    public void eliminar(Sensor sensor) {
        sensorDao.delete(sensor);
    }

    @Override
    @Transactional(readOnly = true)
    public Sensor encontrarSensor(Sensor sensor) {
        return sensorDao.findById(sensor.getIdSensor()).orElse(null);
    }
}
