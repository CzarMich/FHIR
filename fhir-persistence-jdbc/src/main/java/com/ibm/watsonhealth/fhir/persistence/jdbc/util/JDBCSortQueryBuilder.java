/**
 * (C) Copyright IBM Corp. 2017,2018,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.persistence.jdbc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.watsonhealth.fhir.model.Resource;
import com.ibm.watsonhealth.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.watsonhealth.fhir.persistence.exception.FHIRPersistenceNotSupportedException;
import com.ibm.watsonhealth.fhir.persistence.jdbc.dao.api.FHIRDbDAO;
import com.ibm.watsonhealth.fhir.search.SortParameter;
import com.ibm.watsonhealth.fhir.search.SortParameter.SortDirection;
import com.ibm.watsonhealth.fhir.search.context.FHIRSearchContext;

/**
 * This query builder class extends the base JDBCQueryBuilder, and provides support for sorting search results.
 * Methods in this class will consume the SQL generated by the superclass and modify it to insert the necessary
 * sort related clauses and parameters.
 * @author markd
 *
 */
public class JDBCSortQueryBuilder extends JDBCQueryBuilder {
	private static final Logger log = java.util.logging.Logger.getLogger(JDBCSortQueryBuilder.class.getName());
	private static final String CLASSNAME = JDBCSortQueryBuilder.class.getName();
	
	// Constants used in SQL string construction
	private static final String MIN = "MIN";
	private static final String MAX = "MAX";
	private static final String SORT_PARAMETER_ALIAS = "s";
	private static final String NAME = "name";
	private static final String ON = " ON ";
	private static final String AND = " AND ";
	

	public JDBCSortQueryBuilder(FHIRDbDAO dao) {
		super(dao);
	}
	
	/**
	 * Builds a query that returns the count of the search results that would be found by applying the search
	 * parameters contained within the passed search context.
	 * @param resourceType - The type of resource being searched for.
	 * @param searchContext - The search context containing the search parameters.
	 * @return String - A count query SQL string
	 * @throws Exception 
	 */
	public String buildCountQuery(Class<? extends Resource> resourceType, FHIRSearchContext searchContext)
			throws Exception {
		final String METHODNAME = "buildCountQuery";
		log.entering(CLASSNAME, METHODNAME, new Object[] {resourceType.getSimpleName(), searchContext.getSearchParameters()});
		
		String sqlQueryString;
				
		// The superclass build query is called which produces a SELECT string that is devoid
		// of any sort logic. This SELECT string is the basis for building a 'count' query.
		sqlQueryString = super.buildQuery(resourceType, searchContext);
		if (sqlQueryString != null) {
			sqlQueryString = this.removeDefaultOrderBy(sqlQueryString);
			sqlQueryString = sqlQueryString.replace(SELECT_ROOT, SELECT_COUNT_ROOT);
		}
				
		log.exiting(CLASSNAME, METHODNAME, sqlQueryString);
		return sqlQueryString;
	}
	
	/**
	 * Overrides the superclass method in order to incorporate the necessary sort related clauses into the 
	 * SQL SELECT generated by the superclass.
	 * Note that the query returned by this method does not SELECT resources, but rather the Resource ids. This is due to a 
	 * SQL restriction that BLOB type attributes cannot by included in GROUP BY or ORDER BY clauses.
	 * Here is an example of SQL returned for a Patient search, sorted by family name and gender:
	 * SELECT r.id,MIN(s1.valueString),MAX(s2.valueSystem),MAX(s2.valueCode) FROM Resource r 
	 * JOIN Parameter p1 ON p1.resource_id=r.id
	 * LEFT OUTER JOIN Parameter s1 ON (s1.name='family' AND s1.resource_id=r.id) 
	 * LEFT OUTER JOIN Parameter s2 ON (s2.name='gender' AND s2.resource_id=r.id) 
	 * WHERE r.resourceType = 'Patient' 
	 *       AND r.versionId = (SELECT MAX(r2.versionId) FROM Resource r2 WHERE r2.logicalId = r.logicalId) 
	 *       AND (p1.name = 'telecom' AND ((p1.valueCode = '555-1234' AND p1.valueSystem = 'phone'))) 
     * GROUP BY r.id  
     * ORDER BY MIN(s1.valueString) asc NULLS LAST,MAX(s2.valueSystem) desc NULLS LAST,MAX(s2.valueCode) desc NULLS LAST
	 * @throws Exception 
	  */
	@Override
	public String buildQuery(Class<? extends Resource> resourceType, FHIRSearchContext searchContext)
			throws Exception {
		final String METHODNAME = "buildQuery";
		log.entering(CLASSNAME, METHODNAME, new Object[] {resourceType.getSimpleName(), searchContext.getSearchParameters()});
		
		String baseQueryString = null;
		String sqlSortQueryString = null;
		StringBuilder sqlSortQuery = new StringBuilder();
		List<SortParameter> sortParms = searchContext.getSortParameters();
		
		if (!searchContext.hasSortParameters()) {
			throw new FHIRPersistenceException("No sort parameters found in passed searchContext");
		}
		
		// First get the query SQL generated without sort related clauses.
		baseQueryString = super.buildQuery(resourceType, searchContext);
		baseQueryString = this.removeDefaultOrderBy(baseQueryString);
		
		// Rebuild SELECT clause
		this.buildSelectClause(baseQueryString, sortParms, sqlSortQuery);
		
		// Build LEFT OUTER JOIN clause
		this.buildJoinClause(baseQueryString, sortParms, sqlSortQuery);
		
		// Add in the WHERE clause
		this.buildWhereClause(baseQueryString, sortParms, sqlSortQuery);
		
		// Build GROUP BY clause
		this.buildGroupByClause(baseQueryString, sortParms, sqlSortQuery);
		
		// Build ORDER BY clause
		this.buildOrderByClause(baseQueryString, sortParms, sqlSortQuery);
		
		// Add in clauses to support pagination
		this.addPaginationClauses(sqlSortQuery, searchContext);
		
		sqlSortQueryString = sqlSortQuery.length() > 0 ? sqlSortQuery.toString() : null;
		log.exiting(CLASSNAME, METHODNAME,sqlSortQueryString);
		return sqlSortQueryString;
	}
	
	/**
	 * Builds the SELECT clause necessary to return sorted Resource ids. 
	 * For example:
	 * SELECT r.id,MIN(s1.valueString),MAX(s2.valueSystem),MAX(s2.valueCode) FROM Resource r 
	 * 
	 * @param baseQueryString - The SELECT string generated by the superclass buildQueryString() method
	 * @param sortParms - The sort parameters from the incoming search request
	 * @param queryBuffer - The buffer used to build up the SELECT string generated by this.buildQueryString()
	 * @throws FHIRPersistenceException
	 */
	private void buildSelectClause(String baseQueryString, List<SortParameter> sortParms, StringBuilder queryBuffer)
				throws FHIRPersistenceException {
		final String METHODNAME = "buildSelectSegment";
		log.entering(CLASSNAME, METHODNAME);
		
		queryBuffer.append("SELECT r.id");
		
		// Build MIN and/or MAX clauses
		for (int i = 0; i < sortParms.size(); i++) {
			queryBuffer.append(COMMA);
			queryBuffer.append(this.buildAggregateExpression(sortParms.get(i), i+1, false));
		}
		
		queryBuffer.append(" FROM Resource r ");
			
		log.exiting(CLASSNAME, METHODNAME);
	}
	
	/**
	 * Builds the JOIN clauses necessary to return sorted Resource ids. 
	 * For example:
	 * JOIN r.parameters p1 
	 * LEFT OUTER JOIN Parameter s1 ON (s1.name='family' AND s1.resource=r) 
     * LEFT OUTER JOIN Parameter s2 ON (s2.name='gender' AND s2.resource=r) 
	 * 
	 * @param baseQueryString - The SELECT string generated by the superclass buildQueryString() method
	 * @param sortParms - The sort parameters from the incoming search request
	 * @param queryBuffer - The buffer used to build up the SELECT string generated by this.buildQueryString()
	 * @throws FHIRPersistenceException
	 */
	private void buildJoinClause(String baseQueryString, List<SortParameter> sortParms, StringBuilder queryBuffer)
			throws FHIRPersistenceException {
		final String METHODNAME = "buildJoinClause";
		log.entering(CLASSNAME, METHODNAME);
		
		int beginJoin = baseQueryString.indexOf("JOIN");
		int endJoin = baseQueryString.indexOf("WHERE") - 1;
		
		// Include JOIN generated by superclass
		if (beginJoin > -1) {
			queryBuffer.append(baseQueryString.substring(beginJoin, endJoin));
		}
		
		// Build the LEFT OUTER JOINs needed to access the required sort parameters.
		int sortParmIndex = 1;
		for (SortParameter sortParm: sortParms) {
			queryBuffer.append(" LEFT OUTER JOIN Parameter ");
			queryBuffer.append(SORT_PARAMETER_ALIAS).append(sortParmIndex);
			queryBuffer.append(ON );
			queryBuffer.append(LEFT_PAREN);
			queryBuffer.append(SORT_PARAMETER_ALIAS).append(sortParmIndex).append(DOT).append(NAME)
					   .append(EQUALS).append(QUOTE).append(sortParm.getName()).append(QUOTE)
					   .append(AND)
					   .append(SORT_PARAMETER_ALIAS).append(sortParmIndex).append(DOT).append("resource_id").append(EQUALS).append("r.id");
			queryBuffer.append(RIGHT_PAREN);
					
			sortParmIndex++;
		}
					
		log.exiting(CLASSNAME, METHODNAME);
	}
	
	/**
	 * Copies the WHERE clause in the passed base query string to the queryBuffer.
	 * @param baseQueryString - The SELECT string generated by the superclass buildQueryString() method
	 * @param sortParms - The sort parameters from the incoming search request
	 * @param queryBuffer - The buffer used to build up the SELECT string generated by this.buildQueryString()
	 * @throws FHIRPersistenceException
	 */
	private void buildWhereClause(String baseQueryString, List<SortParameter> sortParms, StringBuilder queryBuffer)
			throws FHIRPersistenceException {
		final String METHODNAME = "buildWhereClause";
		log.entering(CLASSNAME, METHODNAME);
		
		int whereBegin = baseQueryString.indexOf(WHERE);
		if (whereBegin > -1) {
			queryBuffer.append(baseQueryString.substring(whereBegin));
		}
		
		log.exiting(CLASSNAME, METHODNAME);
	}
	
	/**
	 * Builds the ORDER BY clause necessary to return sorted Resource ids. 
	 * For example:
	 * GROUP BY r.id 
	 * @param baseQueryString
	 * @param sortParms
	 * @param queryBuffer
	 * @throws FHIRPersistenceException
	 */
	private void buildGroupByClause(String baseQueryString, List<SortParameter> sortParms, StringBuilder queryBuffer)
			throws FHIRPersistenceException {
		final String METHODNAME = "buildGroupByClause";
		log.entering(CLASSNAME, METHODNAME);
		
		queryBuffer.append(" GROUP BY r.id ");
			
		log.exiting(CLASSNAME, METHODNAME);
	}
	
	/**
	 * Builds the ORDER BY clause necessary to return sorted Resource ids. 
	 * For example:
	 * ORDER BY MIN(s1.valueString) asc NULLS LAST,MAX(s2.valueSystem) desc NULLS LAST,MAX(s2.valueCode) desc NULLS LAST
	 * 
	 * @param baseQueryString - The SELECT string generated by the superclass buildQueryString() method
	 * @param sortParms - The sort parameters from the incoming search request
	 * @param queryBuffer - The buffer used to build up the SELECT string generated by this.buildQueryString()
	 * @throws FHIRPersistenceException
	 */
	private void buildOrderByClause(String baseQueryString, List<SortParameter> sortParms, StringBuilder queryBuffer)
			throws FHIRPersistenceException {
		final String METHODNAME = "buildOrderByClause";
		log.entering(CLASSNAME, METHODNAME);
		
		queryBuffer.append(" ORDER BY "); 
		// Build MIN and/or MAX clauses
		for (int i = 0; i < sortParms.size(); i++) {
			if (i > 0) {
				queryBuffer.append(COMMA);
			}
			queryBuffer.append(this.buildAggregateExpression(sortParms.get(i), i+1, true));
		}
			
		log.exiting(CLASSNAME, METHODNAME);
	}
	
	/**
	 * Builds the required MIN or MAX aggregate expressions for the passed sort parameter. 
	 * @param sortParm A valid sort parameter.
	 * @param sortParmIndex An integer representing the position of the sort parameter in a collection of sort parameters.
	 * @param useInOrderByClause A flag indicating whether or not the returned aggregate expression is to be used in an ORDER BY clause.
	 * @return
	 * @throws FHIRPersistenceException
	 */
	private String buildAggregateExpression(SortParameter sortParm, int sortParmIndex, boolean useInOrderByClause) throws FHIRPersistenceException {
		final String METHODNAME = "buildAggregateExpression";
		log.entering(CLASSNAME, METHODNAME);
		
		StringBuilder expression = new StringBuilder();
		List<String> valueAttributeNames;
				
		valueAttributeNames = this.getValueAttributeNames(sortParm);
		boolean nameProcessed = false;
		for(String attributeName : valueAttributeNames) {
			if (nameProcessed) {
				expression.append(COMMA);
			}
			if (sortParm.getDirection().equals(SortDirection.ASCENDING)) {
				expression.append(MIN);
			}
			else {
				expression.append(MAX);
			}
			expression.append(LEFT_PAREN);
			expression.append(SORT_PARAMETER_ALIAS).append(sortParmIndex).append(DOT);
			expression.append(attributeName);
			expression.append(RIGHT_PAREN);
			if (useInOrderByClause) {
				expression.append(" ").append(sortParm.getDirection().value()) 
					.append(" NULLS LAST");
			}
			nameProcessed = true;
		}
				
		log.exiting(CLASSNAME, METHODNAME);
		return expression.toString();
	}
	
	/**
	 * Returns the names of the Parameter attributes containing the values corresponding to the passed sort parameter.
	 * @throws FHIRPersistenceException
	 */
	private List<String> getValueAttributeNames(SortParameter sortParm) throws FHIRPersistenceException {
		final String METHODNAME = "getValueAttributeName";
		log.entering(CLASSNAME, METHODNAME);
		
		List<String> attributeNames = new ArrayList<>();
		switch(sortParm.getType()) {
		case STRING:    attributeNames.add(VALUE_STRING);
			    		break;
		case REFERENCE: attributeNames.add(VALUE_STRING);
						break;
		case DATE:      attributeNames.add(VALUE_DATE);
		        		break;
		case TOKEN:     attributeNames.add(VALUE_SYSTEM);
						attributeNames.add(VALUE_CODE);
						break;
		case NUMBER:    attributeNames.add(VALUE_NUMBER);
						break;
		case QUANTITY:  attributeNames.add(VALUE_NUMBER);
						break;
		case URI:  		attributeNames.add(VALUE_STRING);
						break;
		default: throw new FHIRPersistenceNotSupportedException("Parm type not supported: " + sortParm.getType().value());
		}
				
		log.exiting(CLASSNAME, METHODNAME);
		return attributeNames;
		
	}

}
