/*  Copyright (C) 2005-2008 AtKaaZ <atkaaz@users.sourceforge.net>

 	This file and its contents are part of DeMLinks.

    DeMLinks is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DeMLinks is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DeMLinks.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.demlinks.references;

import org.demlinks.debug.Debug;
import org.demlinks.crap.Position;

/**
 * handles the RefsList list at the Object level
 * ie. accepting only Object parameters where Object is the type of element 
 * 		stored in list
 * DOES NOT allow adding of NULL or DUPlicate Objects
 */
public class ObjRefsList<E> extends RefsList<E> {

//	/** unused, yet
//	 * @param position only FIRST/LAST
//	 * @return the object that was removed, or null is none was
//	 */
//	public E removeObject(Position position) {
//		Debug.nullException(position);
//		Reference<E> nr = getNodeRefAt(position);
//		if (null != nr) {
//			E nod = nr.getObject();
//			if (removeRef(nr)) {
//				return nod;
//			}
//		}
//		return null;
//	}

	/**
	 * @param obj
	 * @return
	 */
	public boolean containsObject(E obj) {
		Debug.nullException(obj);
		return (null != this.getRef(obj));
	}

	/**
	 * creates a new NodeRef to be added to this list, but it's not added via
	 * this method
	 * 
	 * @param obj
	 * @return new reference to <tt>obj</tt>
	 */
	public Reference<E> newRef(E obj) {
		Debug.nullException(obj);
		Reference<E> n = new Reference<E>();
		n.setObject(obj);
		return n;
	}

	/**
	 * @param obj
	 * @return
	 */
	public Reference<E> getRef(E obj) {
		return getRef_L0(obj);
	}

	/**
	 * @param obj
	 * @return null or the reference containing the obj
	 */
	public final Reference<E> getRef_L0(E obj) {
		Debug.nullException(obj);
		Reference<E> parser = getFirstRef();
		while (null != parser) {
			if (obj.equals(parser.getObject())) {
				break;
			}
			parser = parser.getNext();
		}
		return parser;
	}

	/**
	 * @param pos only FIRST/LAST
	 * @return null or the object at specified position
	 */
	public E getObjectAt(Position pos) {
		Debug.nullException(pos);
		Reference<E> ref = getNodeRefAt(pos);
		if (ref != null) {
			return ref.getObject();
		}
		return null;
	}
	
	/**
	 * @param pos BEFORE/AFTER...of...
	 * @param objPos ...which object
	 * @return null or the object
	 */
	public E getObjectAt(Position pos, E objPos) {
		Debug.nullException(pos, objPos);
		
		Reference<E> refPos = getRef(objPos);
		if (refPos == null) {
			//couldn't find objPos
			return null;
		}
		
		//ie. what's the ref that's BEFORE(pos) ref1(refPos) ?
		Reference<E> ref = getNodeRefAt(pos, refPos);
		if (ref != null) {
			return ref.getObject();
		}
		
		return null;
	}
	
	/**
	 * @param obj that doesn't already exist; not null
	 * @return true if object already existed and wasn't re-added or moved to end
	 */
	public boolean addLast(E obj) {
		Debug.nullException(obj);
		Reference<E> nr = getRef(obj);
		if (null != nr) {
			//already exists, not added/moved
			return true;
		}
		nr = newRef(obj);
		return addLast(nr);
	}

	// TODO addFirst
	// TODO insert(Node, Location);
	// TODO insert(Node, Location, Node);
	// TODO replace(Node, Node);
	// TODO replace(Node, Location);
	// TODO replace(Node, Location, Node);
	// find+replace current, is not an option
	/**
	 * @param obj
	 * @return true if existed; either way after call it's removed
	 */
	public boolean removeObject(E obj) {
		Debug.nullException(obj);
		Reference<E> nr = getRef(obj);
		if (null == nr) {
			return false;
		}
		return removeRef(nr);
	}

}
