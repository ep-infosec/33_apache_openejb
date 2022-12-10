/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.plugins.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedQueries;
import javax.persistence.Table;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbRelation;
import org.apache.openejb.jee.EjbRelationshipRole;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.Multiplicity;
import org.apache.openejb.jee.Relationships;
import org.apache.openejb.jee.jpa.Attributes;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.Column;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.FetchType;
import org.apache.openejb.jee.jpa.GeneratedValue;
import org.apache.openejb.jee.jpa.GenerationType;
import org.apache.openejb.jee.jpa.Id;
import org.apache.openejb.jee.jpa.IdClass;
import org.apache.openejb.jee.jpa.NamedNativeQuery;
import org.apache.openejb.jee.jpa.NamedQuery;
import org.apache.openejb.jee.jpa.QueryHint;
import org.apache.openejb.jee.jpa.SequenceGenerator;
import org.apache.openejb.jee.jpa.TableGenerator;
import org.apache.openejb.jee.jpa.UniqueConstraint;

public class EntityBeanConverter implements Converter {

	private IJDTFacade facade;

	public EntityBeanConverter(IJDTFacade annotationHelper) {
		super();
		this.facade = annotationHelper;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void addNamedQueriesAnnotations(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {

		try {
			List<NamedQuery> namedQueries = entity.getNamedQuery();
			List namedQueriesValues = new ArrayList();
			for (NamedQuery namedQuery : namedQueries) {
				String name = namedQuery.getName();
				String query = namedQuery.getQuery();

				List<QueryHint> hints = namedQuery.getHint();

				List hintProperties = new ArrayList();

				for (QueryHint hint : hints) {
					String hintName = hint.getName();
					String hintValue = hint.getValue();

					Map hintProperty = new HashMap();
					hintProperty.put("name", hintName); //$NON-NLS-1$
					hintProperty.put("value", hintValue); //$NON-NLS-1$
					hintProperties.add(hintProperty);
				}

				Map namedQueryProperties = new HashMap();
				namedQueryProperties.put("name", name); //$NON-NLS-1$
				namedQueryProperties.put("query", query); //$NON-NLS-1$
				namedQueryProperties.put("hints", hintProperties.toArray(new Object[0])); //$NON-NLS-1$

				namedQueriesValues.add(namedQueryProperties);
			}
			Map namedQueriesProperties = new HashMap();
			namedQueriesProperties.put("value", namedQueriesValues.toArray(new Object[0])); //$NON-NLS-1$
			facade.addClassAnnotation(entityBean.getEjbClass(), NamedQueries.class, namedQueriesProperties);
		} catch (Exception e) {
			String warning = String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.12"), "@javax.persistence.NamedQueries", entityBean.getEjbClass());
			facade.addWarning(warning);
		}
	}

	void addBasicAnnotations(EntityBean entityBean, List<Basic> basicAttributes) {
		for (Basic basic : basicAttributes) {
			String fieldName = basic.getName();
			Column column = basic.getColumn();

			if (fieldName == null || column == null) {
				return;
			}
			
			addBasicAnnotation(entityBean, fieldName, basic, column);
		}
	}

	private void addBasicAnnotation(EntityBean entityBean, String fieldName, Basic basic, Column column) {
		try {
			addColumnAnnotation(entityBean, fieldName, column);
			Boolean optional = basic.isOptional();
			FetchType fetchType = basic.getFetch();
			Map<String, Object> basicProps = new HashMap<String, Object>();
			if (optional != null)
				basicProps.put("optional", optional.booleanValue()); //$NON-NLS-1$
			if (fetchType != null)
				basicProps.put("fetch", fetchType.value()); //$NON-NLS-1$
			String methodName = convertFieldNameToGetterName(fieldName);
			facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, new String[0], javax.persistence.Basic.class, basicProps);
		} catch (Exception e) {
			String warning = String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.12"), "@javax.persistence.Basic", entityBean.getEjbClass());
			facade.addWarning(warning);
		}
	}

	private String convertFieldNameToGetterName(String fieldName) {
		String methodName = "get" + capitaliseFirstLetter(fieldName); //$NON-NLS-1$
		return methodName;
	}

	private String capitaliseFirstLetter(String fieldName) {
		if (fieldName == null) {
			return null;
		}

		if (fieldName.length() == 0) {
			return fieldName;
		}

		String firstLetter = fieldName.substring(0, 1).toUpperCase();
		String restOfWord = ""; //$NON-NLS-1$

		if (fieldName.length() > 1) {
			restOfWord = fieldName.substring(1);
		}

		return firstLetter + restOfWord;
	}

	void addColumnAnnotation(EntityBean entityBean, String fieldName, Column column) {
		try {
			Map<String, Object> columnProps = new HashMap<String, Object>();
			if (column.getName() != null)
				columnProps.put("name", column.getName()); //$NON-NLS-1$
			if (column.isUnique() != null)
				columnProps.put("unique", column.isUnique().booleanValue()); //$NON-NLS-1$
			if (column.isNullable() != null)
				columnProps.put("nullable", column.isNullable().booleanValue()); //$NON-NLS-1$
			if (column.isInsertable() != null)
				columnProps.put("insertable", column.isInsertable().booleanValue()); //$NON-NLS-1$
			if (column.isUpdatable() != null)
				columnProps.put("updatable", column.isUpdatable().booleanValue()); //$NON-NLS-1$
			if (column.getColumnDefinition() != null)
				columnProps.put("columnDefinition", column.getColumnDefinition()); //$NON-NLS-1$
			if (column.getTable() != null)
				columnProps.put("table", column.getTable()); //$NON-NLS-1$
			if (column.getLength() != null)
				columnProps.put("length", column.getLength().intValue()); //$NON-NLS-1$
			if (column.getPrecision() != null)
				columnProps.put("precision", column.getPrecision().intValue()); //$NON-NLS-1$
			if (column.getScale() != null)
				columnProps.put("scale", column.getScale().intValue()); //$NON-NLS-1$
			String methodName = convertFieldNameToGetterName(fieldName);
			facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, new String[0], javax.persistence.Column.class, columnProps);
		} catch (Exception e) {
			String warning = String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.12"), "@javax.persistence.Column", entityBean.getEjbClass());
			facade.addWarning(warning);
		}
	}

	void addTableAnnotation(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {
		try {
			String tableName = entity.getTable().getName();
			String schemaName = entity.getTable().getSchema();
			String catalogName = entity.getTable().getCatalog();
			
			if (tableName == null && schemaName == null && catalogName == null) return;

			Map<String, Object> tableProperties = new HashMap<String, Object>();
			if (tableName != null && tableName.length() > 0) {
				tableProperties.put("name", tableName); //$NON-NLS-1$
			}

			if (schemaName != null && schemaName.length() > 0) {
				tableProperties.put("schema", schemaName); //$NON-NLS-1$
			}

			if (catalogName != null && catalogName.length() > 0) {
				tableProperties.put("catalog", catalogName); //$NON-NLS-1$
			}

			facade.addClassAnnotation(entityBean.getEjbClass(), Table.class, tableProperties);
		} catch (Exception e) {
			String warning = String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.12"), "@javax.persistence.Table", entityBean.getEjbClass());
			facade.addWarning(warning);
		}
	}

	private org.apache.openejb.jee.jpa.Entity getEntity(EntityMappings entityMappings, String ejbName) {
		Collection<org.apache.openejb.jee.jpa.Entity> entities = entityMappings.getEntity();

		for (org.apache.openejb.jee.jpa.Entity entity : entities) {
			if (entity.getEjbName().equals(ejbName)) {
				return entity;
			}
		}

		return null;
	}

	private List<EntityBean> getEntityBeans(AppModule appModule) {
		List<EntityBean> result = new ArrayList<EntityBean>();

		List<EjbModule> ejbModules = appModule.getEjbModules();
		for (EjbModule ejbModule : ejbModules) {
			EnterpriseBean[] enterpriseBeans = ejbModule.getEjbJar().getEnterpriseBeans();

			for (EnterpriseBean enterpriseBean : enterpriseBeans) {
				if (enterpriseBean instanceof EntityBean) {
					result.add((EntityBean) enterpriseBean);
				}
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public void convert(AppModule appModule) {
		EntityMappings entityMappings = appModule.getCmpMappings();
		List<EntityBean> entityBeans = getEntityBeans(appModule);

		addRelationshipAnnotations(appModule);
		
		for (EntityBean entityBean : entityBeans) {

			checkEjbClassName(entityBean);

			facade.addClassAnnotation(entityBean.getEjbClass(), Entity.class, null);
			org.apache.openejb.jee.jpa.Entity entity = getEntity(entityMappings, entityBean.getEjbName());

			if (entity != null) {
				addTableAnnotation(entityBean, entity);

				Attributes attributes = entity.getAttributes();

				addIdAnnotations(entityBean, attributes.getId());
				addIdClassAnnotation(entityBean, entity);
				addBasicAnnotations(entityBean, attributes.getBasic());
				addNamedQueriesAnnotations(entityBean, entity);
				addNamedNativeQueriesAnnotations(entityBean, entity);
			}
		}
	}

	void addRelationshipAnnotations(AppModule appModule) {
		List<EjbModule> ejbModules = appModule.getEjbModules();
		for (EjbModule ejbModule : ejbModules) {
			EjbJar ejbJar = ejbModule.getEjbJar();
			Relationships relationships = ejbJar.getRelationships();
			
			if (relationships == null)
				return;
			
			List<EjbRelation> relations = relationships.getEjbRelation();
			
			for (EjbRelation relation : relations) {
				List<EjbRelationshipRole> roles = relation.getEjbRelationshipRole();
				if (roles == null || roles.size() != 2) {
					// dunno what to do with this
					continue;
				}
				
				EjbRelationshipRole left = roles.get(0);
				EjbRelationshipRole right = roles.get(1);
				
				String leftClass = getEjbClass(ejbJar, left.getRelationshipRoleSource().getEjbName());
				String rightClass = getEjbClass(ejbJar, right.getRelationshipRoleSource().getEjbName());
				String leftMethod = null;
				try {
					leftMethod = "get" + capitaliseFirstLetter(left.getCmrField().getCmrFieldName());
				} catch (Exception e) {
				}
		
				String rightMethod = null;
				try {
					rightMethod = "get" + capitaliseFirstLetter(right.getCmrField().getCmrFieldName());
				} catch (Exception e) {
				}
				
				boolean leftCascade = left.getCascadeDelete();
				boolean rightCascade = right.getCascadeDelete();
				
				Map<String, Object> leftProps = new HashMap<String, Object>();
				leftProps.put("targetEntity", rightClass);
				
				if (leftCascade) {
					leftProps.put("cascade", new Object[] { javax.persistence.CascadeType.REMOVE }); //$NON-NLS-1$
				}
				

				Map<String, Object> rightProps = new HashMap<String, Object>();
				rightProps.put("targetEntity", leftClass);
				
				if (rightCascade) {
					rightProps.put("cascade", new Object[] { javax.persistence.CascadeType.REMOVE }); //$NON-NLS-1$
				}
				
				if (Multiplicity.MANY.equals(left.getMultiplicity()) && Multiplicity.MANY.equals(right.getMultiplicity())) {
					if (leftMethod != null)
						facade.addMethodAnnotation(leftClass, leftMethod, new String[0], javax.persistence.ManyToMany.class, leftProps);
					if (rightMethod != null)
						facade.addMethodAnnotation(rightClass, rightMethod, new String[0], javax.persistence.ManyToMany.class, rightProps);
				} else if (Multiplicity.MANY.equals(left.getMultiplicity()) && Multiplicity.ONE.equals(right.getMultiplicity())) {
					if (leftMethod != null)
						facade.addMethodAnnotation(leftClass, leftMethod, new String[0], javax.persistence.ManyToOne.class, leftProps);
					if (rightMethod != null)
						facade.addMethodAnnotation(rightClass, rightMethod, new String[0], javax.persistence.OneToMany.class, rightProps);
				} else if (Multiplicity.ONE.equals(left.getMultiplicity()) && Multiplicity.MANY.equals(right.getMultiplicity())) {
					if (leftMethod != null)
						facade.addMethodAnnotation(leftClass, leftMethod, new String[0], javax.persistence.OneToMany.class, leftProps);
					if (rightMethod != null)
						facade.addMethodAnnotation(rightClass, rightMethod, new String[0], javax.persistence.ManyToOne.class, rightProps);
				} else if (Multiplicity.ONE.equals(left.getMultiplicity()) && Multiplicity.ONE.equals(right.getMultiplicity())) {
					if (leftMethod != null)
						facade.addMethodAnnotation(leftClass, leftMethod, new String[0], javax.persistence.OneToOne.class, leftProps);
					if (rightMethod != null)
						facade.addMethodAnnotation(rightClass, rightMethod, new String[0], javax.persistence.OneToOne.class, rightProps);
				}
			}
			
		}
	}

	private String getEjbClass(EjbJar ejbJar, String ejbName) {
		EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
		if (enterpriseBean instanceof EntityBean) {
			return enterpriseBean.getEjbClass();
		}
		
		return null;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void addIdClassAnnotation(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {
		try {
			IdClass idClass = entity.getIdClass();
			if (idClass == null) {
				return;
			}
			String cls = idClass.getClazz();
			Map props = new HashMap();
			props.put("value", cls); //$NON-NLS-1$
			facade.addClassAnnotation(entityBean.getEjbClass(), javax.persistence.IdClass.class, props);
		} catch (Exception e) {
			String warning = String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.12"), "@javax.persistence.IdClass", entityBean.getEjbClass());
			facade.addWarning(warning);
		}
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void addNamedNativeQueriesAnnotations(EntityBean entityBean, org.apache.openejb.jee.jpa.Entity entity) {
		try {
			List<NamedNativeQuery> namedQueries = entity.getNamedNativeQuery();
			List namedQueriesValues = new ArrayList();
			for (NamedNativeQuery namedQuery : namedQueries) {
				String name = namedQuery.getName();
				String query = namedQuery.getQuery();

				List<QueryHint> hints = namedQuery.getHint();

				List hintProperties = new ArrayList();

				for (QueryHint hint : hints) {
					String hintName = hint.getName();
					String hintValue = hint.getValue();

					Map hintProperty = new HashMap();
					hintProperty.put("name", hintName); //$NON-NLS-1$
					hintProperty.put("value", hintValue); //$NON-NLS-1$
					hintProperties.add(hintProperty);
				}

				Map namedQueryProperties = new HashMap();
				namedQueryProperties.put("name", name); //$NON-NLS-1$
				namedQueryProperties.put("query", query); //$NON-NLS-1$
				namedQueryProperties.put("hints", hintProperties.toArray(new Object[0])); //$NON-NLS-1$

				namedQueriesValues.add(namedQueryProperties);
			}
			if (namedQueriesValues.size() > 0) {
				Map namedQueriesProperties = new HashMap();
				namedQueriesProperties.put("value", namedQueriesValues.toArray(new Object[0])); //$NON-NLS-1$

				facade.addClassAnnotation(entityBean.getEjbClass(), NamedNativeQueries.class, namedQueriesProperties);
			}
		} catch (Exception e) {
			String warning = String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.12"), "@javax.persistence.NamedNativeQueries", entityBean.getEjbClass());
			facade.addWarning(warning);
		}
	}

	private void addIdAnnotations(EntityBean entityBean, List<Id> ids) {
		for (Id id : ids) {
			addIdAnnotation(entityBean, id);

			String fieldName = id.getName();
			Column column = id.getColumn();

			if (fieldName != null && column != null)
				addColumnAnnotation(entityBean, fieldName, column);
		}
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	void addIdAnnotation(EntityBean entityBean, Id id) {
		try {
			String fieldName = id.getName();
			String methodName = convertFieldNameToGetterName(fieldName);
			String[] emptySignature = new String[0];
			facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, emptySignature, javax.persistence.Id.class, null);
			GeneratedValue generatedValue = id.getGeneratedValue();
			if (generatedValue != null) {
				Map<String, Object> generatedValueProps = new HashMap<String, Object>();
				if (generatedValue.getGenerator() != null) {
					generatedValueProps.put("generator", generatedValue.getGenerator()); //$NON-NLS-1$
				}

				GenerationType strategy = generatedValue.getStrategy();
				switch (strategy) {
				case AUTO:
					generatedValueProps.put("strategy", javax.persistence.GenerationType.AUTO); //$NON-NLS-1$
					break;
				case IDENTITY:
					generatedValueProps.put("strategy", javax.persistence.GenerationType.IDENTITY); //$NON-NLS-1$
					break;
				case SEQUENCE:
					generatedValueProps.put("strategy", javax.persistence.GenerationType.SEQUENCE); //$NON-NLS-1$
					break;
				case TABLE:
					generatedValueProps.put("strategy", javax.persistence.GenerationType.TABLE); //$NON-NLS-1$
					break;
				}

				facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, emptySignature, javax.persistence.GeneratedValue.class, generatedValueProps);
			}
			SequenceGenerator sequenceGenerator = id.getSequenceGenerator();
			if (sequenceGenerator != null) {
				Map<String, Object> sequenceGeneratorProps = new HashMap<String, Object>();
				sequenceGeneratorProps.put("name", sequenceGenerator.getName()); //$NON-NLS-1$
				sequenceGeneratorProps.put("sequenceName", sequenceGenerator.getSequenceName()); //$NON-NLS-1$
				sequenceGeneratorProps.put("initialValue", sequenceGenerator.getInitialValue().intValue()); //$NON-NLS-1$
				sequenceGeneratorProps.put("allocationSize", sequenceGenerator.getAllocationSize().intValue()); //$NON-NLS-1$

				facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, emptySignature, javax.persistence.SequenceGenerator.class, sequenceGeneratorProps);
			}
			TableGenerator tableGenerator = id.getTableGenerator();
			if (tableGenerator != null) {
				Map<String, Object> tableGeneratorProps = new HashMap<String, Object>();
				tableGeneratorProps.put("name", tableGenerator.getName()); //$NON-NLS-1$
				tableGeneratorProps.put("table", tableGenerator.getTable()); //$NON-NLS-1$
				tableGeneratorProps.put("catalog", tableGenerator.getCatalog()); //$NON-NLS-1$
				tableGeneratorProps.put("schema", tableGenerator.getSchema()); //$NON-NLS-1$
				tableGeneratorProps.put("pkColumnName", tableGenerator.getPkColumnName()); //$NON-NLS-1$
				tableGeneratorProps.put("valueColumnName", tableGenerator.getValueColumnName()); //$NON-NLS-1$
				tableGeneratorProps.put("pkColumnValue", tableGenerator.getPkColumnValue()); //$NON-NLS-1$
				tableGeneratorProps.put("initialValue", tableGenerator.getInitialValue().intValue()); //$NON-NLS-1$
				tableGeneratorProps.put("allocationSize", tableGenerator.getAllocationSize().intValue()); //$NON-NLS-1$

				List uniqueConstraintPropsList = new ArrayList();

				List<UniqueConstraint> uniqueConstraints = tableGenerator.getUniqueConstraint();
				for (UniqueConstraint uniqueConstriant : uniqueConstraints) {
					Map<String, Object> uniqueConstraintProps = new HashMap<String, Object>();
					List<String> columns = uniqueConstriant.getColumnName();
					uniqueConstraintProps.put("columnNames", columns.toArray(new String[0])); //$NON-NLS-1$
					uniqueConstraintPropsList.add(uniqueConstraintProps);
				}

				tableGeneratorProps.put("uniqueConstraints", uniqueConstraintPropsList.toArray(new Object[0])); //$NON-NLS-1$
				facade.addMethodAnnotation(entityBean.getEjbClass(), methodName, emptySignature, javax.persistence.TableGenerator.class, tableGeneratorProps);
			}
		} catch (Exception e) {
			String warning = String.format(Messages.getString("org.apache.openejb.helper.annotation.warnings.12"), "@javax.persistence.Id", entityBean.getEjbClass());
			facade.addWarning(warning);
		}
	}

	private void checkEjbClassName(EntityBean entityBean) {
		String ejbClass = entityBean.getEjbClass();
		String ejbSuperClass = facade.getSuperClass(ejbClass);
		if (ejbSuperClass != null && ejbSuperClass.length() > 0) {
			if (facade.classImplements(ejbSuperClass, "javax.ejb.EntityBean")) { //$NON-NLS-1$
				entityBean.setEjbClass(ejbSuperClass);
			}
		}
	}
}
