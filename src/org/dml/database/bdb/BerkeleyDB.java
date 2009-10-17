/**
 * File creation: May 31, 2009 7:46:58 PM
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


package org.dml.database.bdb;



import java.io.File;

import org.dml.tools.NonNullHashSet;
import org.dml.tools.RunTime;
import org.javapart.logger.Log;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SecondaryDatabase;



/**
 * 
 *
 */
public class BerkeleyDB {
	
	private String								envHomeDir;
	private final EnvironmentConfig				environmentConfig	= new EnvironmentConfig();
	private Environment							env					= null;
	private DBMapJIDsToNodeIDs					db1					= null;
	
	// a database where all sequences will be stored:(only 1 db per bdb env)
	private Database							seqDb				= null;
	private final static String					seqDb_NAME			= "db5_AllSequences";
	private DatabaseConfig						seqDbConf			= null;
	
	private final NonNullHashSet<DBSequence>	ALL_SEQ_INSTANCES	= new NonNullHashSet<DBSequence>();
	
	/**
	 * singleton
	 * 
	 * @return the database handling the one to one mapping between JIDs and
	 *         NodeIDs
	 * @throws DatabaseException
	 */
	public DBMapJIDsToNodeIDs getDBMapJIDsToNodeIDs() throws DatabaseException {

		if ( null == db1 ) {
			db1 = new DBMapJIDsToNodeIDs( this, "map(JID<->NodeID)" );
			RunTime.assertNotNull( db1 );
		}
		return db1;
	}
	
	
	public BerkeleyDB( String envHomeDir1 ) throws DatabaseException {

		this.init( envHomeDir1, false );
	}
	
	/**
	 * @param envHomeDir2
	 * @param internalDestroyBeforeInit
	 * @throws DatabaseException
	 */
	public BerkeleyDB( String envHomeDir1, boolean internalDestroyBeforeInit )
			throws DatabaseException {

		this.init( envHomeDir1, internalDestroyBeforeInit );
	}
	
	/**
	 * intended to be used for JUnit testing when a clean start is required ie.
	 * no leftovers from previous JUnits or runs in the database<br>
	 * this should wipe all logs and locks of BDB Environment (which is
	 * supposedly everything incl. DBs)<br>
	 * <br>
	 * <code>envHomeDir</code> must be set before calling this
	 */
	private void internalWipeEnv() {

		File dir = new File( envHomeDir );
		String[] allThoseInDir = dir.list();
		if ( null != allThoseInDir ) {
			for ( String element : allThoseInDir ) {
				File n = new File( envHomeDir + File.separator + element );
				if ( !n.isFile() ) {
					continue;
				}
				if ( ( !n.getPath().matches( ".*\\.jdb" ) )
						&& ( !( n.getPath().matches( ".*\\.lck" ) ) ) ) {
					continue;
				}
				Log.special( "removing " + n.getPath() );
				if ( !n.delete() ) {
					Log.warn( "Failed removing " + n.getAbsolutePath() );
				}
			}
		}
	}
	
	/**
	 * call before all
	 * 
	 * @throws DatabaseException
	 */
	private final void init( String envHomeDir1,
			boolean internalDestroyBeforeInit ) throws DatabaseException {

		// maybe it would be needed to set the envhome dir
		RunTime.assertNotNull( envHomeDir1 );
		envHomeDir = envHomeDir1;
		if ( internalDestroyBeforeInit ) {
			this.internalWipeEnv();
		}
		// Environment init isn't needed, only deInit();
		this.getEnvironment();// forces env open or create
		// DBSequence init isn't needed, only deInit()
		
		// getDBMapJIDsToNodeIDs() is initing that when needed
		
		// db1=db1.init();
		
	}
	
	
	/**
	 *call when all done
	 */
	public final void deInit() {

		if ( null != db1 ) {
			db1 = db1.deInit();
		}
		this.deInitSeqSystem();
		this.closeDBEnvironment();
	}
	
	/**
	 * @return singleton of the BDB Environment
	 * @throws DatabaseException
	 */
	public final Environment getEnvironment() throws DatabaseException {

		if ( null == env ) {
			// make new now:
			this.firstTimeCreateEnvironment();
			RunTime.assertNotNull( env );
		}
		
		return env;
	}
	
	
	/**
	 * @param input
	 * @param output
	 */
	public final static void stringToEntry( String input, DatabaseEntry output ) {

		RunTime.assertNotNull( input, output );
		StringBinding.stringToEntry( input, output );
	}
	
	/**
	 * @param input
	 * @return
	 */
	public final static String entryToString( DatabaseEntry input ) {

		RunTime.assertNotNull( input );
		return StringBinding.entryToString( input );
	}
	
	

	/**
	 * @throws DatabaseException
	 * 
	 */
	private final void firstTimeCreateEnvironment() throws DatabaseException {

		environmentConfig.setAllowCreate( true );
		environmentConfig.setLocking( true );
		environmentConfig.setTransactional( true );
		environmentConfig.setTxnNoSync( false );
		environmentConfig.setTxnSerializableIsolation( true );
		environmentConfig.setTxnWriteNoSync( false );
		environmentConfig.setSharedCache( false );
		environmentConfig.setConfigParam( EnvironmentConfig.TRACE_LEVEL, "FINE" );
		environmentConfig.setConfigParam( EnvironmentConfig.TRACE_CONSOLE,
				"false" );
		environmentConfig.setConfigParam( EnvironmentConfig.TRACE_FILE, "true" );
		environmentConfig.setConfigParam( EnvironmentConfig.TRACE_DB, "false" );
		
		// perform other environment configurations
		File file = new File( envHomeDir );
		try {
			file.mkdirs();
			env = new Environment( file, environmentConfig );
		} catch ( DatabaseException de ) {
			Log.thro( "when creating BerkeleyDB Environment: "
					+ de.getMessage() );
			throw de;
		}
		
	}
	
	

	/**
	 * silently closing database
	 * no throws
	 * 
	 * @return null
	 * @param db
	 */
	public static final Database silentCloseAnyDB( Database db, String dbname ) {

		if ( null != db ) {
			try {
				db.close();
				Log.mid( "closed BerkeleyDB with name: " + dbname );
			} catch ( DatabaseException de ) {
				Log.thro( "failed closing BerkeleyDB with specified name: '"
						+ dbname );
				// ignore
			}
		} else {
			Log.mid( "wasn't open BerkeleyDB with name: " + dbname );
		}
		return null;
	}
	
	/**
	 * silently closing SecondaryDatabase
	 * no throws
	 * 
	 * @return null
	 * @param secDb
	 */
	public static final SecondaryDatabase silentCloseAnySecDB(
			SecondaryDatabase secDb, String secDbName ) {

		if ( null != secDb ) {
			try {
				secDb.close();
				Log.mid( "closed SecDB with name: " + secDbName );
			} catch ( DatabaseException de ) {
				Log.thro( "failed closing SecDB with specified name: '"
						+ secDbName );
				// ignore
			}
		} else {
			Log.mid( "wasn't open SecDB with name: " + secDbName );
		}
		return null;
	}
	
	/**
	 * 
	 */
	public final void closeDBEnvironment() {

		if ( null != env ) {
			try {
				env.close();
				Log.exit( "BerkeleyDB env closed" );
			} catch ( DatabaseException de ) {
				Log.thro( "failed BerkeleyDB environment close:"
						+ de.getCause().getLocalizedMessage() );
				// ignore
			} finally {
				env = null;
			}
		} else {
			Log.mid( "BerkeleyDB env wasn't open" );
		}
	}
	
	
	/**
	 * new instance of DBSequence
	 * 
	 * @param seqName1
	 *            name of the Sequence
	 * @return
	 */
	public final DBSequence newDBSequence( String seqName1 ) {

		DBSequence dbs = new DBSequence( this, seqName1 );
		if ( !ALL_SEQ_INSTANCES.add( dbs ) ) {
			RunTime.Bug( "couldn't have already existed!" );
		}
		RunTime.assertNotNull( dbs );
		return dbs;
	}
	
	/**
	 * 
	 */
	private final void silentCloseAllSequences() {

		Log.entry();
		for ( DBSequence dbs : ALL_SEQ_INSTANCES ) {
			dbs.silentCloseSeq();
			ALL_SEQ_INSTANCES.remove( dbs );
		}
		RunTime.assertTrue( ALL_SEQ_INSTANCES.isEmpty() );
	}
	
	/**
	 * closing all sequences first, then the BerkeleyDB
	 */
	private void silentCloseAllSequencesAndTheirDB() {

		Log.entry();
		if ( !ALL_SEQ_INSTANCES.isEmpty() ) {
			this.silentCloseAllSequences();
		}
		
		if ( !ALL_SEQ_INSTANCES.isEmpty() ) {
			// BUG, avoiding throw because it's silent
			Log.bug( "should be empty now" );
		}
		
		if ( null != seqDb ) {
			seqDb = BerkeleyDB.silentCloseAnyDB( seqDb, seqDb_NAME );
		} else {
			Log.warn( "close() called on a not yet inited/open database" );
		}
	}
	
	/**
	 * safely closes all active sequences and the database holding them<br>
	 * for the current environment only
	 */
	public final void deInitSeqSystem() {

		this.silentCloseAllSequencesAndTheirDB();
	}
	
	/**
	 * @return
	 * @throws DatabaseException
	 */
	protected Database getSeqsDB() throws DatabaseException {

		if ( null == seqDb ) {
			// init first time:
			seqDb = this.openSeqDB();
			RunTime.assertNotNull( seqDb );
		}
		return seqDb;
	}
	
	/**
	 * one time open the database containing all stored sequences, and future
	 * ones
	 * 
	 * @param dbName
	 * @return
	 * @throws DatabaseException
	 */
	private final Database openSeqDB() throws DatabaseException {

		RunTime.assertNotNull( seqDb_NAME );
		RunTime.assertFalse( seqDb_NAME.isEmpty() );
		
		if ( null == seqDbConf ) {
			// init once:
			seqDbConf = new DatabaseConfig();
			seqDbConf.setAllowCreate( true );
			seqDbConf.setDeferredWrite( false );
			seqDbConf.setKeyPrefixing( true );
			seqDbConf.setSortedDuplicates( false );//
			seqDbConf.setTransactional( true );
		}
		
		return this.getEnvironment().openDatabase( null, seqDb_NAME, seqDbConf );
	}
	
}// class
