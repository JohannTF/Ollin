package ipn.mx.isc.sismosapp.backend.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Catálogo de estados de la República Mexicana
 * Mapeados con sus abreviaturas según el SSN
 */
public enum EstadoMexicano {
    AGUASCALIENTES("AGS", "Aguascalientes"),
    BAJA_CALIFORNIA("BC", "Baja California"),
    BAJA_CALIFORNIA_SUR("BCS", "Baja California Sur"),
    CAMPECHE("CAMP", "Campeche"),
    CHIAPAS("CHIS", "Chiapas"),
    CHIHUAHUA("CHIH", "Chihuahua"),
    CIUDAD_DE_MEXICO("CDMX", "Ciudad de México"),
    COAHUILA("COAH", "Coahuila"),
    COLIMA("COL", "Colima"),
    DURANGO("DGO", "Durango"),
    ESTADO_DE_MEXICO("MEX", "Estado de México"),
    GUANAJUATO("GTO", "Guanajuato"),
    GUERRERO("GRO", "Guerrero"),
    HIDALGO("HGO", "Hidalgo"),
    JALISCO("JAL", "Jalisco"),
    MICHOACAN("MICH", "Michoacán"),
    MORELOS("MOR", "Morelos"),
    NAYARIT("NAY", "Nayarit"),
    NUEVO_LEON("NL", "Nuevo León"),
    OAXACA("OAX", "Oaxaca"),
    PUEBLA("PUE", "Puebla"),
    QUERETARO("QRO", "Querétaro"),
    QUINTANA_ROO("QROO", "Quintana Roo"),
    SAN_LUIS_POTOSI("SLP", "San Luis Potosí"),
    SINALOA("SIN", "Sinaloa"),
    SONORA("SON", "Sonora"),
    TABASCO("TAB", "Tabasco"),
    TAMAULIPAS("TAMPS", "Tamaulipas"),
    TLAXCALA("TLAX", "Tlaxcala"),
    VERACRUZ("VER", "Veracruz"),
    YUCATAN("YUC", "Yucatán"),
    ZACATECAS("ZAC", "Zacatecas");

    private final String abreviatura;
    private final String nombreCompleto;

    EstadoMexicano(String abreviatura, String nombreCompleto) {
        this.abreviatura = abreviatura;
        this.nombreCompleto = nombreCompleto;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    /**
     * Busca un estado por su nombre completo (case-insensitive)
     */
    public static Optional<EstadoMexicano> fromNombreCompleto(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return Arrays.stream(values())
            .filter(estado -> estado.nombreCompleto.equalsIgnoreCase(nombre.trim()))
            .findFirst();
    }

    /**
     * Busca un estado por su abreviatura (case-insensitive)
     */
    public static Optional<EstadoMexicano> fromAbreviatura(String abrev) {
        if (abrev == null || abrev.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return Arrays.stream(values())
            .filter(estado -> estado.abreviatura.equalsIgnoreCase(abrev.trim()))
            .findFirst();
    }
}
