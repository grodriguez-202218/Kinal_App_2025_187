package com.gahelrodriguez.kinalapp.repository;

import com.gahelrodriguez.kinalapp.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVentaCodigoVenta(Long codigoVenta);

    // Necesario para la eliminación inteligente de Producto
    List<DetalleVenta> findByProductoCodigoProducto(Long codigoProducto);
}