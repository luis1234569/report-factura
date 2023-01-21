package facturacion.api_factura.factura;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.FileNotFoundException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.ResourceUtils;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import facturacion.api_factura.cliente.CustomerClient;
import facturacion.api_factura.cliente.CustomerDTO;


@Service
public class FacturaService {
    @Autowired FacturaRepository facturaRepository;
    @Autowired CustomerClient customerClient;


    public Factura save(Factura entity){
        return facturaRepository.save(entity);
    }

    public Factura findById( Long id){
        return facturaRepository.findById(id).orElse(new Factura());
    }

    public void deleteById(Long id){
        facturaRepository.deleteById(id);
    }

    public List<Factura> findAll(){
        return facturaRepository.findAll();
    }

    public JasperPrint getFacturaReporte(Long id) {

        Map<String, Object> reportParameters = new HashMap<String, Object>();
        Factura factura = findById(id);
        if (factura.getId()==null)
            return null;
        
        reportParameters.put("nro", factura.getNumeroFactura());
        reportParameters.put("fecha",Date.valueOf(factura.getFecha()));

        CustomerDTO cliente =  customerClient.findCustomerById(factura.getClienteId());
        reportParameters.put("nombre_cliente", cliente.getNombre());
        reportParameters.put("identificacion", cliente.getCedula());
        
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (FacturaLinea linea : factura.getLineas()){
            Map<String, Object> data = new HashMap<>();
            data.put("nombreProducto", linea.getProducto().getNombre());
            data.put("cantidad",linea.getCantidad());
            data.put("precio",linea.getPrecio());
            data.put("subtotal",linea.getCantidad().multiply(linea.getPrecio()));
            dataList.add(data);
        }
        reportParameters.put("lineasFactura", new JRBeanCollectionDataSource(dataList));

        JasperPrint reportJasperPrint = null;
        try {
            reportJasperPrint = JasperFillManager.fillReport(
                    JasperCompileManager.compileReport(
                            ResourceUtils.getFile("classpath:jrxml/Factura.jrxml")
                                    .getAbsolutePath()) // path of the jasper report
                    , reportParameters // dynamic parameters
                    , new JREmptyDataSource());
        } catch (FileNotFoundException | JRException e) {
            e.printStackTrace();
        }
        return reportJasperPrint;
    }

}
