package fr.karspa.hiker_thinker.repository;

import fr.karspa.hiker_thinker.dtos.filters.EquipmentSearchDTO;
import fr.karspa.hiker_thinker.dtos.responses.SourceEquipmentPageDTO;
import fr.karspa.hiker_thinker.model.SourceEquipment;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;

@Repository
@AllArgsConstructor
public class EquipmentRepository {

    private MongoTemplate mongoTemplate;


    public List<SourceEquipment> findAll(EquipmentSearchDTO searchParams){

        // Constuire la requete avec les params
        Query query = new Query(this.buildCriteriaForFilters(searchParams));

        //Ajouter ceux de pagination
        this.addPageToQuery(searchParams, query);

        // Aller récupérer les équipements correspondant dans la collection 'equipments'
        return mongoTemplate.find(query, SourceEquipment.class);
    }
    public List<SourceEquipment> findAll(EquipmentSearchDTO searchParams, Query query){

        //Ajouter ceux de pagination
        this.addPageToQuery(searchParams, query);

        // Aller récupérer les équipements correspondant dans la collection 'equipments'
        return mongoTemplate.find(query, SourceEquipment.class);
    }

    public SourceEquipmentPageDTO findAllWithPagination(EquipmentSearchDTO searchParams){

        Query query = new Query(this.buildCriteriaForFilters(searchParams));

        Long totalCount = this.countItemsWithFilters(query);

        //Appliquer le tri après la requete de comptage (par référence)
        this.applySortToQuery(searchParams, query);

        List<SourceEquipment> sourceEquipments = this.findAll(searchParams, query);


        return new SourceEquipmentPageDTO(totalCount, sourceEquipments);
    }


    public Long countItemsWithFilters(EquipmentSearchDTO searchParams){
        Query query = new Query(this.buildCriteriaForFilters(searchParams));

        return mongoTemplate.count(query, SourceEquipment.class, "equipments");
    }

    private Long countItemsWithFilters(Query query){

        return mongoTemplate.count(query, SourceEquipment.class, "equipments");

    }


    private void addPageToQuery(EquipmentSearchDTO searchParams, Query query){ //Modification par référence de la Query

        //Ajouter ceux de pagination
        int controlledPageSize = Math.min(searchParams.getPageSize(), 40);
        int controlledPageNumber = Math.max(searchParams.getPageNumber(), 0);
        query.skip((long) controlledPageNumber * controlledPageSize);
        query.limit(searchParams.getPageSize());

    }


    private void applySortToQuery(EquipmentSearchDTO searchParams, Query query){

        if (searchParams.getSortBy() != null && !searchParams.getSortBy().isBlank()) {
            // Choix de la direction
            Sort.Direction direction = "DESC".equalsIgnoreCase(searchParams.getSortDir())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            // Traduction du sortBy en champ Mongo
            String mongoField = switch (searchParams.getSortBy()) {
                case "weight" -> "weight_g";
                case "name"   -> "name";
                default       -> null;
            };

            if (mongoField != null) {
                query.with(Sort.by(direction, mongoField));
            }
        }
    }


    private Criteria buildCriteriaForFilters(EquipmentSearchDTO searchParams) {
        Criteria criteria = new Criteria();

        // 1) Si un nom est fourni, on cherche en "contains" (regex, insensible à la casse)
        if (searchParams.getName() != null && !searchParams.getName().isBlank()) {
            // Utilise une regex pour retrouver "name" partiel, sans tenir compte de la casse
            criteria = criteria.and("name")
                    .regex(".*" + Pattern.quote(searchParams.getName()) + ".*", "i");
        }

        // 2) Si une marque est fournie
        if (searchParams.getBrand() != null && !searchParams.getBrand().isBlank()) {
            criteria = criteria.and("brand")
                    .regex(".*" + Pattern.quote(searchParams.getBrand()) + ".*", "i");
        }

        if (searchParams.getMinWeight() != null || searchParams.getMaxWeight() != null) {
            Criteria weightCrit = Criteria.where("weight_g");
            if (searchParams.getMinWeight() != null) {
                weightCrit = weightCrit.gte(searchParams.getMinWeight());
            }
            if (searchParams.getMaxWeight() != null) {
                weightCrit = weightCrit.lte(searchParams.getMaxWeight());
            }
            // On ajoute ce sous-criteria à l'ensemble
            criteria = criteria.andOperator(weightCrit);
        }



        return criteria;
    }


}
