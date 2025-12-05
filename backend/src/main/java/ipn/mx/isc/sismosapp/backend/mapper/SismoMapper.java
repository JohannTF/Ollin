package ipn.mx.isc.sismosapp.backend.mapper;

import ipn.mx.isc.sismosapp.backend.dto.SismoDTO;
import ipn.mx.isc.sismosapp.backend.model.Sismo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SismoMapper {

    /**
     * Entidad Sismo -> SismoDTO
     */
    public SismoDTO toDTO(Sismo sismo) {
        if (sismo == null) {
            return null;
        }
        
        return new SismoDTO(
            sismo.getId(),
            sismo.getFechaHora(),
            sismo.getLatitud(),
            sismo.getLongitud(),
            sismo.getMagnitud(),
            sismo.getProfundidadKm(),
            sismo.getLugar(),
            sismo.getFuente()
        );
    }

    /**
     * Lista de entidades Sismo -> Lista de SismoDTOs
     */
    public List<SismoDTO> toDTOList(List<Sismo> sismos) {
        if (sismos == null) {
            return null;
        }
        
        return sismos.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * SismoDTO -> Entidad Sismo
     */
    public Sismo toEntity(SismoDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Sismo sismo = new Sismo();
        sismo.setId(dto.getId());
        sismo.setFechaHora(dto.getFechaHora());
        sismo.setLatitud(dto.getLatitud());
        sismo.setLongitud(dto.getLongitud());
        sismo.setMagnitud(dto.getMagnitud());
        sismo.setProfundidadKm(dto.getProfundidadKm());
        sismo.setLugar(dto.getLugar());
        sismo.setFuente(dto.getFuente());
        
        return sismo;
    }
}
