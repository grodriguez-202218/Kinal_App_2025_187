package com.gahelrodriguez.kinalapp.service;

import com.gahelrodriguez.kinalapp.entity.Producto;
import com.gahelrodriguez.kinalapp.repository.DetalleVentaRepository;
import com.gahelrodriguez.kinalapp.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService implements IProductoService {

    private final ProductoRepository      productoRepository;
    private final DetalleVentaRepository  detalleVentaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           DetalleVentaRepository detalleVentaRepository) {
        this.productoRepository     = productoRepository;
        this.detalleVentaRepository = detalleVentaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarTodos() { return productoRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarActivos() { return productoRepository.findByEstado(1L); }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarInactivos() { return productoRepository.findByEstado(0L); }

    @Override
    public Producto guardar(Producto producto) {
        validarProducto(producto);
        if (producto.getEstado() == null || producto.getEstado() == 0L)
            producto.setEstado(1L);
        return productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorCodigo(Long codigo) {
        return productoRepository.findById(codigo);
    }

    @Override
    public Producto actualizar(Long codigo, Producto producto) {
        if (!productoRepository.existsById(codigo))
            throw new RuntimeException("Producto no encontrado con codigo " + codigo);
        producto.setCodigoProducto(codigo);
        validarProducto(producto);
        return productoRepository.save(producto);
    }

    @Override
    public void eliminar(Long codigo) {
        Producto producto = productoRepository.findById(codigo)
                .orElseThrow(() -> new RuntimeException(
                        "Producto no encontrado con codigo " + codigo));

        boolean tieneDetalles = !detalleVentaRepository
                .findByProductoCodigoProducto(codigo).isEmpty();

        if (tieneDetalles) {
            producto.setEstado(0L);
            productoRepository.save(producto);
        } else {
            productoRepository.deleteById(codigo);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCodigo(Long codigo) {
        return productoRepository.existsById(codigo);
    }

    private void validarProducto(Producto producto) {
        if (producto.getNombreProducto() == null || producto.getNombreProducto().trim().isEmpty())
            throw new IllegalArgumentException("El nombre del producto es un campo obligatorio");
        if (producto.getPrecio() == null)
            throw new IllegalArgumentException("El precio es un campo obligatorio");
        if (producto.getPrecio().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El precio no puede ser negativo");
        if (producto.getStock() == null || producto.getStock() < 0L)
            throw new IllegalArgumentException("El stock no puede ser negativo");
    }
}