package ipn.mx.isc.sismosapp.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ipn.mx.isc.sismosapp.backend.model.dto.SismoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);
    private static final String RECENT_SISMOS_KEY = "sismos:recent";
    private static final long CACHE_TTL_HOURS = 24;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisCacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void guardarSismosRecientes(List<SismoDTO> sismos) {
        try {
            String json = objectMapper.writeValueAsString(sismos);
            redisTemplate.opsForValue().set(RECENT_SISMOS_KEY, json, CACHE_TTL_HOURS, TimeUnit.HOURS);
            logger.info("Sismos guardados en Redis cache");
        } catch (JsonProcessingException e) {
            logger.error("Error al serializar sismos a JSON: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error al almacenar sismos en Redis: {}", e.getMessage());
        }
    }

    public List<SismoDTO> obtenerSismosRecientes() {
        try {
            String json = redisTemplate.opsForValue().get(RECENT_SISMOS_KEY);
            if (json != null && !json.isEmpty()) {
                logger.info("Sismos obtenidos desde Redis cache");
                return objectMapper.readValue(json, new TypeReference<List<SismoDTO>>() {});
            }
        } catch (JsonProcessingException e) {
            logger.error("Error al deserializar sismos desde JSON: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error extrayendo sismos desde Redis: {}", e.getMessage());
        }
        return null;
    }
}
