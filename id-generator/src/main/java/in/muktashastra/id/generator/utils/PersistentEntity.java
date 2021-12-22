/**
 * 
 */
package in.muktashastra.id.generator.utils;

import java.io.Serializable;

/**
 * @author Nilesh
 *
 */
public interface PersistentEntity extends Serializable {

	public void setId(Long id);
	
	public String getEntityTableName();
}
