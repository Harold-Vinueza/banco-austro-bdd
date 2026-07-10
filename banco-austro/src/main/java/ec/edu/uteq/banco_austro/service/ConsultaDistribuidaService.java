package ec.edu.uteq.banco_austro.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConsultaDistribuidaService {

    private final JdbcTemplate jdbcCuenca;
    private final JdbcTemplate jdbcQuito;

    public ConsultaDistribuidaService(
            @Qualifier("dsCuenca") DataSource dsCuenca,
            @Qualifier("dsQuito") DataSource dsQuito) {
        this.jdbcCuenca = new JdbcTemplate(dsCuenca);
        this.jdbcQuito = new JdbcTemplate(dsQuito);
    }

    public Map<String, Object> consultarSaldo(String numero) {
        JdbcTemplate destino = enrutar(numero);
        String sql = "SELECT numero, saldo, oficina "
                + "FROM cuentas WHERE numero = ?";
        List<Map<String, Object>> filas =
                destino.queryForList(sql, numero);
        if (filas.isEmpty()) {
            return Map.of(
                    "error", "Cuenta no encontrada",
                    "numero", numero);
        }
        return filas.get(0);
    }

    public List<Map<String, Object>> listarTodosLosClientes() {
        String sql = "SELECT cedula, nombre, ciudad FROM clientes";
        List<Map<String, Object>> union = new ArrayList<>();
        union.addAll(jdbcCuenca.queryForList(sql));
        union.addAll(jdbcQuito.queryForList(sql));
        return union;
    }

    private JdbcTemplate enrutar(String numero) {
        if (numero == null || numero.length() < 2) {
            throw new IllegalArgumentException(
                    "Numero de cuenta invalido: " + numero);
        }
        String prefijo = numero.substring(0, 2);
        return switch (prefijo) {
            case "22" -> jdbcCuenca;
            case "17" -> jdbcQuito;
            default -> throw new IllegalArgumentException(
                    "Prefijo de oficina no reconocido: " + prefijo);
        };
    }
}