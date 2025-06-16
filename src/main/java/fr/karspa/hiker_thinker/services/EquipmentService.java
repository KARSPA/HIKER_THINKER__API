package fr.karspa.hiker_thinker.services;

import fr.karspa.hiker_thinker.dtos.filters.EquipmentSearchDTO;
import fr.karspa.hiker_thinker.dtos.responses.SourceEquipmentPageDTO;
import fr.karspa.hiker_thinker.model.SourceEquipment;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EquipmentService {

    ResponseModel<SourceEquipmentPageDTO> findAll(EquipmentSearchDTO searchParams);

    ResponseModel<SourceEquipment> findById(Long id);

    ResponseModel<SourceEquipment> findByName(String name);



    ResponseModel<SourceEquipment> save(SourceEquipment sourceEquipment);
    ResponseModel<SourceEquipment> update(SourceEquipment sourceEquipment);
    ResponseModel<SourceEquipment> deleteById(Long id);

}
