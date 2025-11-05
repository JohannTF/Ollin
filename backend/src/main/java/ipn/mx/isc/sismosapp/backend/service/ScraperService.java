package ipn.mx.isc.sismosapp.backend.service;

import ipn.mx.isc.sismosapp.backend.model.Sismo;
import org.springframework.beans.factory.annotation.Value;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScraperService {

    private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

    @Value("${ssn.url}")
    private String ssnUrl;
    private static final int TIMEOUT = 30000;

    public List<Sismo> scrapeSismos() {
        List<Sismo> sismos = new ArrayList<>();

        try {
            logger.info("Iniciando scraping");
            Document document = Jsoup.connect(ssnUrl)
                .timeout(TIMEOUT)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .get();

            Elements rows = document.select("tr.1days, tr.2days");
            logger.info("Sismos encontrados: {}", rows.size());

            for (Element row : rows) {
                try {
                    Sismo sismo = parseSismoFromRow(row);
                    if (sismo != null) {
                        sismos.add(sismo);
                    }
                } catch (Exception e) {
                    logger.error("Error al parsear fila de sismo: {}", e.getMessage());
                }
            }

            logger.info("Scraping completado. Sismos extraidos: {}", sismos.size());

        } catch (Exception e) {
            logger.error("Error durante scraping: {}", e.getMessage(), e);
        }

        return sismos;
    }

    private Sismo parseSismoFromRow(Element row) {
        try {
            String magnitudText = row.select("td.latest-mag").text().trim();
            String fechaText = row.select("span[id^=date_]").text().trim();
            String horaText = row.select("span[id^=time_]").text().trim();
            String lugarText = row.select("span[id^=epi_]").text().trim();
            String latitudText = row.select("span[id^=lat_]").text().trim();
            String longitudText = row.select("span[id^=lon_]").text().trim();
            String profundidadText = row.select("td[id^=prof_]").text().trim();

            if (magnitudText.isEmpty() || fechaText.isEmpty() || horaText.isEmpty()) {
                return null;
            }

            Double magnitud = Double.parseDouble(magnitudText);
            LocalDate fecha = LocalDate.parse(fechaText, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime hora = LocalTime.parse(horaText, DateTimeFormatter.ISO_LOCAL_TIME);
            java.time.LocalDateTime localFechaHora = LocalDateTime.of(fecha, hora);
            java.time.OffsetDateTime fechaHora = java.time.OffsetDateTime.of(localFechaHora, java.time.ZoneOffset.UTC);
            Double latitud = Double.parseDouble(latitudText);
            Double longitud = Double.parseDouble(longitudText);
            Double profundidad = parseProfundidad(profundidadText);

            Sismo sismo = new Sismo();
            sismo.setFechaHora(fechaHora);
            sismo.setMagnitud(magnitud);
            sismo.setLatitud(latitud);
            sismo.setLongitud(longitud);
            sismo.setProfundidadKm(profundidad);
            sismo.setLugar(lugarText);
            sismo.setFuente("SSN");

            return sismo;

        } catch (Exception e) {
            logger.error("Error parseando datos de sismo: {}", e.getMessage());
            return null;
        }
    }

    private Double parseProfundidad(String profundidadText) {
        try {
            String cleaned = profundidadText.replace("km", "").trim();
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            logger.warn("No se pudo parsear profundidad: {}", profundidadText);
            return 0.0;
        }
    }
}
