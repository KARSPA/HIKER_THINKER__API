package fr.karspa.hiker_thinker.services.impl;

import fr.karspa.hiker_thinker.dtos.filters.EquipmentSearchDTO;
import fr.karspa.hiker_thinker.dtos.responses.SourceEquipmentPageDTO;
import fr.karspa.hiker_thinker.model.SourceEquipment;
import fr.karspa.hiker_thinker.repository.EquipmentRepository;
import fr.karspa.hiker_thinker.services.EquipmentService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.springframework.stereotype.Service;


@Service
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentServiceImpl(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }


    @Override
    public ResponseModel<SourceEquipmentPageDTO> findAll(EquipmentSearchDTO searchParams) {
        SourceEquipmentPageDTO response = equipmentRepository.findAllWithPagination(searchParams);

        return ResponseModel.buildResponse("200", "Voici les équipements trouvés !", response);
    }

    @Override
    public ResponseModel<SourceEquipment> findById(Long id) {
        return null;
    }

    @Override
    public ResponseModel<SourceEquipment> findByName(String name) {
        return null;
    }

    @Override
    public ResponseModel<SourceEquipment> save(SourceEquipment sourceEquipment) {
        return null;
    }

    @Override
    public ResponseModel<SourceEquipment> update(SourceEquipment sourceEquipment) {
        return null;
    }

    @Override
    public ResponseModel<SourceEquipment> deleteById(Long id) {
        return null;
    }
}
