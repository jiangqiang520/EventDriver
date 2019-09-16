package com.jace.event.support.po;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jace.event.core.common.util.ContextHolderEx;
import com.jace.event.support.util.UUIDUtil;

import lombok.Data;

@Data
@MappedSuperclass
@JsonIgnoreProperties(value={"hibernateLazyInitializer","handler"})
public class BasePO implements Serializable {

	private static final long serialVersionUID = -158291998395462981L;

	@JsonIgnore
	@Column(nullable = false, length = 30, updatable = false)
	private String createdBy;

    @JsonIgnore
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Date createdDate;

    @JsonIgnore
	@Column(nullable = false, length = 30)
	private String updatedBy;

    @JsonIgnore
	@Temporal(TemporalType.TIMESTAMP)
	@UpdateTimestamp
	@Column(nullable = false)
	private Date updatedDate;
    
    public BasePO() {
    	setCreatedBy(ContextHolderEx.getUserId());
		setUpdatedBy(ContextHolderEx.getUserId());
		generateUID();
    }

	@PreUpdate
	public void preUpdate() {
		setUpdatedBy(ContextHolderEx.getUserId());
	}

	private void generateUID() {
		Class<?> entityType = this.getClass();
		Field[] columns = entityType.getDeclaredFields();
		try {
			for (Field column : columns) {
				if (column.isAnnotationPresent(GeneratedUID.class)) {
					column.setAccessible(true);
					column.set(this, UUIDUtil.getUid());
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Exception when generateUID");
		}
	}
}
