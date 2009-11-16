/**
 * File creation: Jun 8, 2009 3:25:28 PM
 * 
 * Copyright (C) 2005-2009 AtKaaZ <atkaaz@users.sourceforge.net>
 * Copyright (C) 2005-2009 UnKn <unkn@users.sourceforge.net>
 * 
 * This file and its contents are part of DeMLinks.
 * 
 * DeMLinks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DeMLinks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DeMLinks. If not, see <http://www.gnu.org/licenses/>.
 */


package org.dml.database;



import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.dml.JUnits.Consts;
import org.dml.database.bdb.level1.Level1_Storage_BerkeleyDB;
import org.dml.database.bdb.level2.DBMapTupleNodeIDs;
import org.dml.level1.Level1_DMLEnvironment;
import org.dml.level1.NodeID;
import org.dml.level1.NodeJavaID;
import org.dml.storagewrapper.StorageException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.references.method.MethodParams;
import org.references.method.PossibleParams;

import com.sleepycat.je.DatabaseException;



/**
 * 
 *
 */
public class DBMapTupleNodeIDsTest {
	
	DBMapTupleNodeIDs			tdb;
	Level1_DMLEnvironment		dmlEnv;
	Level1_Storage_BerkeleyDB	bdb;
	
	@Before
	public void setUp() throws DatabaseException {

		MethodParams<Object> params = new MethodParams<Object>();
		params.init( null );
		params.set( PossibleParams.homeDir, Consts.BDB_ENV_PATH );
		params.set( PossibleParams.jUnit_wipeDB, true );
		params.set( PossibleParams.jUnit_wipeDBWhenDone, true );
		bdb = new Level1_Storage_BerkeleyDB();
		bdb.init( params );
		params.deInit();
		tdb = new DBMapTupleNodeIDs( bdb, "tupleIDs" );
		
	}
	
	@After
	public void tearDown() {

		// tdb.silentClose();
		tdb = null;
		bdb.deInit();
		bdb = null;
	}
	
	@Test
	public void test1() throws DatabaseException, StorageException {

		String strA = "A";
		NodeJavaID jidA = NodeJavaID.ensureJavaIDFor( strA );
		NodeID _a = bdb.getDBMap_JavaIDs_To_NodeIDs().ensureNodeID( jidA );
		NodeID _b = bdb.getDBMap_JavaIDs_To_NodeIDs().ensureNodeID(
				NodeJavaID.ensureJavaIDFor( "B" ) );
		assertNotNull( _a );
		assertNotNull( _b );
		
		assertTrue( jidA.equals( bdb.getDBMap_JavaIDs_To_NodeIDs().getNodeJavaID(
				_a ) ) );
		assertTrue( bdb.getDBMap_JavaIDs_To_NodeIDs().getNodeJavaID( _a ) == jidA );
		
		org.junit.Assert.assertFalse( tdb.isVector( _a, _b ) );
		org.junit.Assert.assertFalse( tdb.ensureVector( _a, _b ) );
		
		assertTrue( tdb.isVector( _a, _b ) );
		assertTrue( tdb.ensureVector( _a, _b ) );
		NodeID _d = bdb.getDBMap_JavaIDs_To_NodeIDs().ensureNodeID(
				NodeJavaID.ensureJavaIDFor( "D" ) );
		NodeID _e = bdb.getDBMap_JavaIDs_To_NodeIDs().ensureNodeID(
				NodeJavaID.ensureJavaIDFor( "E" ) );
		assertFalse( tdb.ensureVector( _d, _e ) );
		NodeID _c = bdb.getDBMap_JavaIDs_To_NodeIDs().ensureNodeID(
				NodeJavaID.ensureJavaIDFor( "C" ) );
		assertFalse( tdb.ensureVector( _a, _c ) );
		assertTrue( tdb.isVector( _a, _c ) );
		assertFalse( tdb.ensureVector( _c, _a ) );
		assertFalse( tdb.ensureVector( _c, _b ) );
		assertTrue( tdb.isVector( _c, _a ) );
		assertTrue( tdb.isVector( _c, _b ) );
	}
}
