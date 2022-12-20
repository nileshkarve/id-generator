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
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd'T'hhmmss.SSS");
	
	private static final Set<String> entitiesWithGeneratedId = new HashSet<>();
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Transactional(transactionManager = "transactionManager")
	public <T extends PersistentEntity> Long generateAndSetId(T entity) {
		Long id = generateIdFor(entity.getEntityTableName());
		entity.setId(id);
		return id;
	}
	
	@Transactional(transactionManager = "transactionManager")
	public <T extends PersistentEntity> List<Long> generateAndSetIds(List<T> entities) {
		if(null == entities || entities.isEmpty()) {
			return new ArrayList<>();
		}
		else {
			List<Long> ids = generateRangeOfIdsFor(entities.get(0).getEntityTableName(), entities.size());
			entities.stream().forEachOrdered(entity -> entity.setId(ids.remove(0)));
			return ids;
		}
	}
	
	@Transactional(transactionManager = "transactionManager")
	public Long generateIdFor(String tableName) {
		return generateRangeOfIdsFor(tableName, 1).get(0);
	}

	@Transactional(transactionManager = "transactionManager")
	public List<Long> generateRangeOfIdsFor(String tableName, int numberOfIds) {
		if(!isIdPresentForEntity(tableName)) {
			String insertEntity = "INSERT INTO ENTITY_ID_REGISTRY(ENTITY, CURRENT_ID)VALUES(?,?)";
			jdbcTemplate.update(insertEntity, tableName, 999L);
			entitiesWithGeneratedId.add(tableName);
		}
		String lockString = generateUniqueStr();
		
		String updateLockQuery = "UPDATE ENTITY_ID_REGISTRY SET LOCK_STR = ? WHERE ENTITY = ?";
		jdbcTemplate.update(updateLockQuery, lockString, tableName);
		
		
		List<Long> ids = new ArrayList<>();
		String entitySelector = "SELECT CURRENT_ID FROM ENTITY_ID_REGISTRY WHERE ENTITY = ? AND LOCK_STR = ?";
		Long currentEntityId = jdbcTemplate.queryForObject(entitySelector, Long.class, tableName, lockString);

		populateIds(currentEntityId, numberOfIds, ids);
		String updateQuery = "UPDATE ENTITY_ID_REGISTRY SET CURRENT_ID = ?, LOCK_STR = ? WHERE ENTITY = ? AND LOCK_STR = ?";
		jdbcTemplate.update(updateQuery, Long.sum(currentEntityId, numberOfIds), null, tableName, lockString);
		
		return ids;
	}
	
	private boolean isIdPresentForEntity(String tableName) {
		if(entitiesWithGeneratedId.contains(tableName)) {
			return true;
		}
		String entitySelector = "SELECT CURRENT_ID FROM ENTITY_ID_REGISTRY WHERE ENTITY = ?";
		Long currentEntityId = null;
		try {
			currentEntityId = jdbcTemplate.queryForObject(entitySelector, Long.class, tableName);
			if(null != currentEntityId) {
				entitiesWithGeneratedId.add(tableName);
			}
			return null != currentEntityId;
		}
		catch(DataAccessException e) {
			return false;
		}
	}

	private String generateUniqueStr() {
		SecureRandom random = new SecureRandom();
		Integer randomInt = 100000 + random.nextInt(900000);
		String lockString = dateFormatter.format(new Date()).concat(randomInt.toString());
		return lockString;
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
