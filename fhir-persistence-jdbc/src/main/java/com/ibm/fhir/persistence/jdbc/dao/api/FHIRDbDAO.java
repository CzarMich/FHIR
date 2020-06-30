/*
 * (C) Copyright IBM Corp. 2017, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.jdbc.dao.api;

import java.sql.Connection;

import com.ibm.fhir.persistence.jdbc.exception.FHIRPersistenceDBConnectException;

/**
 * This is a root interface for child Data Access Object interfaces.
 */
public interface FHIRDbDAO {

    /**
     * Obtains a database connection. Connection is configured and ready to use.
     * If multi-tenant, the tenant session variable will have been set.
     * 
     * @return Connection - A connection to the FHIR database.
     * @throws FHIRPersistenceDBConnectException
     */
    Connection getConnection() throws FHIRPersistenceDBConnectException;

    /**
     * @return true if this DAO is connected to a DB2 database.
     */
    boolean isDb2Database();
}