package ipn.mx.isc.sismosapp.backend.service;

import ipn.mx.isc.sismosapp.backend.dto.SismoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);
    private static final String RECENT_SISMOS_KEY = "sismos:recent";
    private static final long CACHE_TTL_HOURS = 24;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void guardarSismosRecientes(List<SismoDTO> sismos) {
        try {
            redisTemplate.opsForValue().set(RECENT_SISMOS_KEY, sismos, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.error("Error al actualizar/almacenar sismos en Redis: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<SismoDTO> obtenerSismosRecientes() {
        try {
            Object cached = redisTemplate.opsForValue().get(RECENT_SISMOS_KEY);
            if (cached != null) {
                logger.info("Sismos obtenidos desde Redis cache");
                return (List<SismoDTO>) cached;
            }
        } catch (Exception e) {
            logger.error("Error extrayendo sismos desde Redis: {}", e.getMessage());
        }
        return null;
    }
}
