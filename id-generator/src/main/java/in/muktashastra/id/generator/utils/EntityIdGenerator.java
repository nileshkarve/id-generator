/**
 * 
 */
package in.muktashastra.id.generator.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nilesh
 *
 */
public class EntityIdGenerator {
	
	private JdbcTemplate jdbcTemplate;
	
	@Transactional
	public Long generateAndSetId(PersistentEntity entity) {
		Long id = generateIdFor(entity.getEntityTableName());
		entity.setId(id);
		return id;
	}
	
	@Transactional
	public List<Long> generateAndSetIds(List<? extends PersistentEntity> entities) {
		List<Long> ids = generateRangeOfIdsFor(entities.get(0).getEntityTableName(), entities.size());
		entities.stream().forEachOrdered(entity -> entity.setId(ids.remove(0)));
		return ids;
	}
	
	@Transactional
	public Long generateIdFor(String tableName) {
		return generateRangeOfIdsFor(tableName, 1).get(0);
	}

	@Transactional
	public List<Long> generateRangeOfIdsFor(String tableName, int numberOfIds) {
		String updateLockQuery = "UPDATE entity_id_rgstry SET crnt_id = (SELECT rgstry.crnt_id FROM entity_id_rgstry rgstry WHERE rgstry.entity = ?) WHERE entity = ?";
		jdbcTemplate.update(updateLockQuery, tableName, tableName);
		
		
		List<Long> ids = new ArrayList<>();
		String entitySelector = "SELECT crnt_id FROM entity_id_rgstry WHERE entity = ?";
		Long currentEntityId = null;
		try {
			currentEntityId = jdbcTemplate.queryForObject(entitySelector, Long.class, tableName);
		}
		catch(DataAccessException e) {
			currentEntityId = 0L;
		}
		String insertEntity = "INSERT INTO entity_id_rgstry(entity,crnt_id)VALUES(?,?)";
		jdbcTemplate.update(insertEntity, tableName, currentEntityId);
		populateIds(currentEntityId, numberOfIds, ids);
		String updateQuery = "UPDATE entity_id_rgstry SET crnt_id = ? WHERE entity = ?";
		jdbcTemplate.update(updateQuery, Long.sum(currentEntityId, numberOfIds), tableName);
		return ids;
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private void populateIds(Long currentEntityId, int numberOfIds, List<Long> ids) {
		for(int i=1; i<=numberOfIds; i++) {
			ids.add(Long.sum(currentEntityId, Long.valueOf(i)));
		}
	}

}
