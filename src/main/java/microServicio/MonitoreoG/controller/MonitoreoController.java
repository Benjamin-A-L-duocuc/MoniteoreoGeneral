package microServicio.MonitoreoG.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import microServicio.MonitoreoG.model.LibroDTO;
import microServicio.MonitoreoG.model.SucursalDTO;
import microServicio.MonitoreoG.service.MonitoreoService;

@RestController
@RequestMapping("/api/monitoreo")
public class MonitoreoController {

    @Autowired
    private MonitoreoService service;

    @GetMapping("/libros")
    public List<LibroDTO> libros() {
        return service.obtenerLibros();
    }

    @GetMapping("/sucursales")
    public List<SucursalDTO> sucursales() {
        return service.obtenerSucursales();
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumenGeneral() {

        List<LibroDTO> libros = service.obtenerLibros();

        List<SucursalDTO> sucursales = service.obtenerSucursales();

        int stockTotal = libros.stream().mapToInt(LibroDTO::getStock).sum();

        Map<String, Object> datos = new HashMap<>();
        datos.put("totalLibros",libros.size());
        datos.put("stockTotal",stockTotal);
        datos.put("totalSucursales",sucursales.size());
        
        return datos;
    }
}
