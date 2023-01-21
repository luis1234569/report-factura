package facturacion.api_factura.cliente;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    private String nombre;
    private String cedula;
}

