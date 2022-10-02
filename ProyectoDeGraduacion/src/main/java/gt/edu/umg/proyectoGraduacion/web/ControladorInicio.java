package gt.edu.umg.proyectoGraduacion.web;

import com.amazonaws.internal.StaticCredentialsProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import gt.edu.umg.proyectoGraduacion.domain.Persona;
import gt.edu.umg.proyectoGraduacion.domain.Sensor;
import gt.edu.umg.proyectoGraduacion.servicio.MedicionService;
import gt.edu.umg.proyectoGraduacion.servicio.PersonaService;
import gt.edu.umg.proyectoGraduacion.servicio.SensorService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import software.amazon.awssdk.core.SdkBytes;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.AWSLambdaException;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.lambda.AWSLambda;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@Slf4j
public class ControladorInicio {

    BasicAWSCredentials awsCreds = new BasicAWSCredentials(
            "AKIA2LZQSTBPBDJFLBVM",
            "QX9cDdvTbpAOqi6tU3e8KMYibU4tk1VtOGLKBNvz");

    Regions region = Regions.US_EAST_2;
    

    AmazonSNS snsClient;

    AWSLambda lambdaClient;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private MedicionService medicionService;

    @Autowired
    private SensorService sensorService;

    public ControladorInicio() {
        this.lambdaClient = AWSLambdaClient.builder().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(region).build();
        
        this.snsClient = AmazonSNSClient.builder().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(region).build();
               
    }

    @GetMapping("/")
    public String inicio(Model model, @AuthenticationPrincipal User user) {
        
        var personas = personaService.listarPersonas();
        //log.info("ejecutando el controlador Spring MVC");
        //log.info("usuario que hizo login:" + user);
        model.addAttribute("personas", personas);
        var saldoTotal = 0D;
        for (var p : personas) {
            saldoTotal += p.getSaldo();
        }
        model.addAttribute("saldoTotal", saldoTotal);
        model.addAttribute("totalClientes", personas.size());

        var sensores = sensorService.listarSensores();
        model.addAttribute("sensores", sensores);
        model.addAttribute("totalSensores", sensores.size());

        //Traer las mediciones de la base de datos
        List<String[]> mediciones = new ArrayList<>();
        var medicionesBD = medicionService.listarMediciones();
        for (var m : medicionesBD) {
            //System.out.println(m.getFecha());
            Timestamp ts = new Timestamp(m.getFecha().getTime());
            //System.out.println("Fecha: " + ts.getTime() + " Humedad:" + m.getHumedad() + " Sensor:" + m.getSensor());
            String[] medicion = {String.valueOf(ts.getTime()),
                String.valueOf(m.getHumedad()),};
            mediciones.add(medicion);
        }

        //Para hacer pruebas de generacion del grafico
        /* Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp);                  // 2021-03-24 17:12:03.311
        System.out.println(timestamp.getTime());        // 1616577123311

        List<String[]> mediciones = new ArrayList<>();
        for (int x=0; x<1440; x++){
            String[] medicion = {String.valueOf(timestamp.getTime() - 60000L * x),
                                 String.valueOf(ThreadLocalRandom.current().nextInt(1, 100)),                                          
                                 String.valueOf(ThreadLocalRandom.current().nextInt(1, 100)),
                                 };
            mediciones.add(medicion);
        }*/
        model.addAttribute("datosMediciones", mediciones);

        return "index";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Sensor sensor, Errors errores) {
        if (errores.hasErrors()) {
            return "modificar";
        }
        sensorService.guardar(sensor);
        subEmail(snsClient, "arn:aws:sns:us-east-2:712529582174:EnviaNotificacionTema", sensor.getEmail());
        invokeFunction(lambdaClient, "arn:aws:lambda:us-east-2:712529582174:function:EnviaMensajeHardware", sensor);
        return "redirect:/";
    }

    @GetMapping("/editar/{idSensor}")
    public String editar(Sensor sensor, Model model) {
        sensor = sensorService.encontrarSensor(sensor);
        model.addAttribute("sensor", sensor);
        return "modificar";
    }

    @GetMapping("/agregar")
    public String agregar(Persona persona) {
        return "modificar";
    }

    @PostMapping("/guardar2")
    public String guardar2(@Valid Persona persona, Errors errores) {
        if (errores.hasErrors()) {
            return "modificar";
        }
        personaService.guardar(persona);
        return "redirect:/";
    }

    @GetMapping("/editar2/{idPersona}")
    public String editar2(Persona persona, Model model) {
        persona = personaService.encontrarPersona(persona);
        model.addAttribute("persona", persona);
        return "modificar";
    }

    @GetMapping("/eliminar")
    public String eliminar(Persona persona) {
        personaService.eliminar(persona);
        return "redirect:/";
    }

    public static void subEmail(AmazonSNS snsClient, String topicArn, String email) {

        try {
            
            SubscribeRequest request = new SubscribeRequest();
            request.setProtocol("email");
            request.setEndpoint(email);
            request.setTopicArn(topicArn);
            
            /*SubscribeRequest request = SubscribeRequest.builder()
                    .protocol("email")
                    .endpoint(email)
                    .returnSubscriptionArn(true)
                    .topicArn(topicArn)
                    .build();*/

            SubscribeResult result = snsClient.subscribe(request);
            System.out.println(result);
            //System.out.println("Subscription ARN: " + result.subscriptionArn() + "\n\n Status is " + result.sdkHttpResponse().statusCode());

        } catch (AmazonSNSException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    public static void invokeFunction(AWSLambda awsLambda, String functionName, Sensor sensor) {

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json="";
        try {
            json = ow.writeValueAsString(sensor);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(ControladorInicio.class.getName()).log(Level.SEVERE, null, ex);
        }

        InvokeResult res = null;
        try {
            //Need a SdkBytes instance for the payload
            //SdkBytes payload = SdkBytes.fromUtf8String(json);

            //Setup an InvokeRequest
            InvokeRequest request = new InvokeRequest();
            request.setFunctionName(functionName);
            request.setPayload(json);
            /*InvokeRequest request = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(payload)
                    .build();*/

            res = awsLambda.invoke(request);
            String value = res.getPayload().toString();
            System.out.println(value);

        } catch (AWSLambdaException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
