package microServicio.MonitoreoG.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import microServicio.MonitoreoG.model.LibroDTO;
import microServicio.MonitoreoG.model.SucursalDTO;

@Service
public class MonitoreoService {

    @Autowired
    private RestTemplate restTemplate;

    public List<LibroDTO> obtenerLibros() {

        String url ="http://localhost:8082/api/libros";

        try {

            LibroDTO[] libros = restTemplate.getForObject(url,LibroDTO[].class);

            return Arrays.asList(libros);
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<SucursalDTO> obtenerSucursales() {

        String url ="http://localhost:8083/api/sucursales";

        SucursalDTO[] sucursales =restTemplate.getForObject(url,SucursalDTO[].class);
        
        return Arrays.asList(sucursales);
    }
    
}
