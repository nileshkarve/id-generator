/**
 * 
 */
package in.muktashastra.id.generator.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import liquibase.integration.spring.SpringLiquibase;

/**
 * @author Nilesh
 *
 */
public class IdGeneratorDatabaseConfig {
	
	@Value("${id.generator.database.url}")
	private String idGeneratorDatabaseUrl;
	@Value("${id.generator.database.driver}")
	private String idGeneratorDatabaseDriver;
	@Value("${id.generator.database.username}")
	private String idGeneratorDatabaseUserName;
	@Value("${id.generator.database.password}")
	private String idGeneratorDatabasePswd;
	
	@Bean("idGeneratorDataSource")
	public DataSource idGeneratorDataSource() {
		DataSourceBuilder<?> builder = DataSourceBuilder.create();
		builder.url(idGeneratorDatabaseUrl).driverClassName(idGeneratorDatabaseDriver).username(idGeneratorDatabaseUserName).password(idGeneratorDatabasePswd);
		return builder.build();
	}
	
	@Bean("idGeneratorLiquibase")
	public SpringLiquibase springCoreLiquibase(@Qualifier("idGeneratorDataSOurce") DataSource idGeneratorDataSource) {
		SpringLiquibase liquibase = new SpringLiquibase();
	    liquibase.setChangeLog("classpath:liquibase/id-generator-changeLog.xml");
	    liquibase.setDataSource(idGeneratorDataSource);
	    return liquibase;
	}
	
	@Bean("idGeneratorJdbcTemplate")
	public JdbcTemplate idGeneratorJdbcTemplate(@Qualifier("idGeneratorDataSource") DataSource idGeneratorDataSource) {
	    return new JdbcTemplate(idGeneratorDataSource);
	}
}
