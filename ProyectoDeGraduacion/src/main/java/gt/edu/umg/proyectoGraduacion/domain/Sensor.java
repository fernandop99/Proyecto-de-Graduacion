/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gt.edu.umg.proyectoGraduacion.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Entity
@Table(name = "sensor")
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSensor;
    
    @NotEmpty
    private String nombre;
    
    @NotEmpty
    private String descripcion;
    
    
    private int tipoRiego;
    
    
    private int tiempoRiego;
    
    @NotEmpty
    @Email
    private String email;
    
    
    private int tiempoEnvioMensaje;
    
    
    private int notificaciones;
     
    
    private int humedadMinima;

    public Long getIdSensor() {
        return idSensor;
    }

    public void setIdSensor(Long idSensor) {
        this.idSensor = idSensor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getTipoRiego() {
        return tipoRiego;
    }

    public void setTipoRiego(int tipoRiego) {
        this.tipoRiego = tipoRiego;
    }

    public int getTiempoRiego() {
        return tiempoRiego;
    }

    public void setTiempoRiego(int tiempoRiego) {
        this.tiempoRiego = tiempoRiego;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTiempoEnvioMensaje() {
        return tiempoEnvioMensaje;
    }

    public void setTiempoEnvioMensaje(int tiempoEnvioMensaje) {
        this.tiempoEnvioMensaje = tiempoEnvioMensaje;
    }

    public int getNotificaciones() {
        return notificaciones;
    }

    public void setNotificaciones(int notificaciones) {
        this.notificaciones = notificaciones;
    }

    public int getHumedadMinima() {
        return humedadMinima;
    }

    public void setHumedadMinima(int humedadMinima) {
        this.humedadMinima = humedadMinima;
    }
    
    
      
}
