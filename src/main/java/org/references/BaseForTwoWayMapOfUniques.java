/**
 * 
 * Copyright (c) 2005-2011, AtKaaZ
 * All rights reserved.
 * this file is part of DemLinks
 * File created on Aug 4, 2011 11:55:28 PM
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of 'DemLinks' nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.references;


/**
 * just make sure you call assertInvariants(); before each method call, <br>
 * which makes sure you're not using them after discard() was called<br>
 * if you override discard() make sure you call super() first<br>
 * 
 * @param <KEY>
 * @param <DATA>
 * 
 */
public abstract class BaseForTwoWayMapOfUniques<KEY, DATA> implements GenericTwoWayMapOfUniques<KEY, DATA> {
	
	// FIXME: if overridders are allowed to be synchronized then while waiting on the lock, the other thread could discard this
	// first but the subclass will still execute that method after lock acquire; so to fix, we must make these in base
	// synchronized, but the downside is not all implementations need these to be synchronized so loosing concurrency?
	private volatile boolean	discarded	= false;
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.references.TwoWayMapOfUniques#isEmpty()
	 */
	@Override
	public final boolean isEmpty() {
		assertInvariants();
		return 0 == size();
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.references.TwoWayMapOfUniques#discard()
	 */
	/**
	 * if you override this, call super() first!<br>
	 * this will make sure it's not already discarded and set discarded flag<br>
	 * 
	 * @see org.references.GenericTwoWayMapOfUniques#discard()
	 */
	@Override
	public final void discard() {
		assert !discarded;
		discarded = true;
		internalForOverride_discard();
	}
	
	
	protected void internalForOverride_discard() {
		// allowed to be overridden, but optional
	}
	
	
	protected final void assertInvariants() {
		assert !discarded;
		internalForOverride_assertInvariants();
	}
	
	
	/**
	 * override this and it will be automatically called via {@link #assertInvariants()}<br>
	 * which happens on the beginning of every method, defined in base class<br>
	 * if you define new methods you need to call {@link #assertInvariants()} as first statement yourself<br>
	 * otherwise no need<br>
	 */
	protected void internalForOverride_assertInvariants() {
		// allowed to be overridden, but optional
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.references.TwoWayMapOfUniques#getKey(java.lang.Object)
	 */
	@Override
	public final KEY getKey( final DATA data ) {
		assertInvariants();
		return internalForOverride_getKey( data );
	}
	
	
	protected abstract KEY internalForOverride_getKey( final DATA data );
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.references.TwoWayMapOfUniques#getData(java.lang.Object)
	 */
	@Override
	public final DATA getData( final KEY key ) {
		assertInvariants();
		return internalForOverride_getData( key );
	}
	
	
	protected abstract DATA internalForOverride_getData( final KEY key );
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.references.TwoWayMapOfUniques#ensureExists(java.lang.Object, java.lang.Object)
	 */
	@Override
	public final boolean ensureExists( final KEY key, final DATA data ) {
		assertInvariants();
		return internalForOverride_ensureExists( key, data );
	}
	
	
	protected abstract boolean internalForOverride_ensureExists( final KEY key, final DATA data );
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.references.TwoWayMapOfUniques#removeByKey(java.lang.Object)
	 */
	@Override
	public final boolean removeByKey( final KEY key ) {
		assertInvariants();
		return internalForOverride_removeByKey( key );
	}
	
	
	protected abstract boolean internalForOverride_removeByKey( final KEY key );
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.references.TwoWayMapOfUniques#removeAll()
	 */
	@Override
	public final void removeAll() {
		assertInvariants();
		internalForOverride_removeAll();
	}
	
	
	protected abstract void internalForOverride_removeAll();
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.references.TwoWayMapOfUniques#size()
	 */
	@Override
	public final int size() {
		assertInvariants();
		return internalForOverride_size();
	}
	
	
	protected abstract int internalForOverride_size();
}
