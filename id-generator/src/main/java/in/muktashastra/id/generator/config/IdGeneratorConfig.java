/**
 * 
 */
package in.muktashastra.id.generator.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import in.muktashastra.id.generator.utils.EntityIdGenerator;

/**
 * @author Nilesh
 *
 */
@Configuration
public class IdGeneratorConfig {
	
	@Bean("entityIdGenerator")
	public EntityIdGenerator entityIdGenerator(@Qualifier("coreJdbcTemplate") JdbcTemplate coreJdbcTemplate) {
		EntityIdGenerator entityIdGenerator = new EntityIdGenerator();
		entityIdGenerator.setJdbcTemplate(coreJdbcTemplate);
		return entityIdGenerator;
	}

}
