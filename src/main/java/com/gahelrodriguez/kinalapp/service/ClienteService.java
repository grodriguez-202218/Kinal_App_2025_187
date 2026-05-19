package com.gahelrodriguez.kinalapp.service;

import com.gahelrodriguez.kinalapp.entity.Cliente;
import com.gahelrodriguez.kinalapp.repository.ClienteRepository;
import com.gahelrodriguez.kinalapp.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteService implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final VentaRepository   ventaRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          VentaRepository ventaRepository) {
        this.clienteRepository = clienteRepository;
        this.ventaRepository   = ventaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarActivos() {
        return clienteRepository.findByEstado(1L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarInactivos() {
        return clienteRepository.findByEstado(0L);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        validarCliente(cliente);
        if (cliente.getEstado() == null || cliente.getEstado() == 0)
            cliente.setEstado(1L);
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> busacarPorDPI(Long dpi) {
        return clienteRepository.findById(dpi);
    }

    @Override
    public Cliente actualizar(Long dpi, Cliente cliente) {
        if (!clienteRepository.existsById(dpi))
            throw new RuntimeException("Cliente no encontrado con DPI " + dpi);
        cliente.setDPICliente(dpi);
        validarCliente(cliente);
        return clienteRepository.save(cliente);
    }


    @Override
    public void eliminar(Long dpi) {
        Cliente cliente = clienteRepository.findById(dpi)
                .orElseThrow(() -> new RuntimeException(
                        "Cliente no encontrado con DPI " + dpi));

        boolean tieneVentas = !ventaRepository.findByClienteDPICliente(dpi).isEmpty();

        if (tieneVentas) {
            // Eliminación lógica: solo desactiva
            cliente.setEstado(0L);
            clienteRepository.save(cliente);
        } else {
            // Sin dependencias: borrado físico seguro
            clienteRepository.deleteById(dpi);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exiteDPI(Long dpi) {
        return clienteRepository.existsById(dpi);
    }

    private void validarCliente(Cliente cliente) {
        if (cliente.getDPICliente() == null)
            throw new IllegalArgumentException("El DPI es un dato obligatorio");
        if (cliente.getNombreCliente() == null || cliente.getNombreCliente().trim().isEmpty())
            throw new IllegalArgumentException("El nombre es un campo obligatorio");
        if (cliente.getApellidoCliente() == null || cliente.getApellidoCliente().trim().isEmpty())
            throw new IllegalArgumentException("El apellido es un campo obligatorio");
    }
}