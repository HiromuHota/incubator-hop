/*! ******************************************************************************
 *
 * Hop : The Hop Orchestration Platform
 *
 * http://www.project-hop.org
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.apache.hop.metastore.stores.delegate;

import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.metastore.api.IMetaStoreAttribute;
import org.apache.hop.metastore.api.IMetaStoreElement;
import org.apache.hop.metastore.api.IMetaStoreElementType;
import org.apache.hop.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.apache.hop.metastore.api.exceptions.MetaStoreElementExistException;
import org.apache.hop.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.apache.hop.metastore.api.exceptions.MetaStoreException;
import org.apache.hop.metastore.api.security.Base64TwoWayPasswordEncoder;
import org.apache.hop.metastore.api.security.IMetaStoreElementOwner;
import org.apache.hop.metastore.api.security.ITwoWayPasswordEncoder;
import org.apache.hop.metastore.api.security.MetaStoreElementOwnerType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This class can be used as a wrapper around one or more meta stores. For example, if you have a local XML metastore, a
 * workgroup database metastore and an enterprise EE metastore, you can put them in reverse order in the meta stores
 * list.
 * <p>
 * There are 2 ways to work with the delegating meta store. The first is if you set an active meta store. That way, it
 * works as if you're working with the specified meta store.
 * <p>
 * If you didn't specify an active store, all namespaces and elements in all listed meta stores are considered. This
 * operating mode will prevent write operations.
 * <p>
 * That way, if you ask for the list of elements, you will get a unique list (by element ID) based on all stores.
 *
 * @author matt
 */
public class DelegatingMetaStore implements IMetaStore {

  /**
   * Maps the name of the metastore to the physical implementation
   */
  protected List<IMetaStore> metaStoreList;

  /**
   * The active metastore
   */
  protected String activeMetaStoreName;

  /**
   * The two way password encoder to use
   */
  protected ITwoWayPasswordEncoder passwordEncoder;

  public DelegatingMetaStore() {
    metaStoreList = new ArrayList<>();
    passwordEncoder = new Base64TwoWayPasswordEncoder();
  }

  public DelegatingMetaStore( IMetaStore... stores ) {
    metaStoreList = new ArrayList<>( Arrays.asList( stores ) );
    passwordEncoder = new Base64TwoWayPasswordEncoder();
  }

  public void addMetaStore( IMetaStore metaStore ) throws MetaStoreException {
    metaStoreList.add( metaStore );
  }

  public void addMetaStore( int index, IMetaStore metaStore ) throws MetaStoreException {
    metaStoreList.add( index, metaStore );
  }

  public boolean removeMetaStore( IMetaStore metaStore ) throws MetaStoreException {
    return removeMetaStore( metaStore.getName() );
  }

  public List<IMetaStore> getMetaStoreList() {
    return metaStoreList;
  }

  public void setMetaStoreList( List<IMetaStore> metaStoreList ) {
    this.metaStoreList = metaStoreList;
  }

  private List<IMetaStore> getReadMetaStoreList() throws MetaStoreException {
    if ( activeMetaStoreName != null ) {
      return Arrays.asList( getMetaStore( activeMetaStoreName ) );
    }
    return metaStoreList;
  }

  private IMetaStore getWriteMetaStore() throws MetaStoreException {
    if ( activeMetaStoreName != null ) {
      IMetaStore activeMetaStore = getMetaStore( activeMetaStoreName );
      if ( activeMetaStore != null ) {
        return activeMetaStore;
      }
      throw new MetaStoreException( "Active metaStore could not be found but required for write operations." );
    }
    throw new MetaStoreException( "Active metaStore not set but required for write operations." );
  }

  public boolean removeMetaStore( String metaStoreName ) throws MetaStoreException {
    for ( Iterator<IMetaStore> it = metaStoreList.iterator(); it.hasNext(); ) {
      IMetaStore store = it.next();
      if ( store.getName().equalsIgnoreCase( metaStoreName ) ) {
        it.remove();
        if ( activeMetaStoreName != null && metaStoreName.equalsIgnoreCase( activeMetaStoreName ) ) {
          activeMetaStoreName = null;
        }
        return true;
      }
    }
    return false;
  }

  public void setActiveMetaStoreName( String activeMetaStoreName ) {
    this.activeMetaStoreName = activeMetaStoreName;
  }

  public String getActiveMetaStoreName() {
    return activeMetaStoreName;
  }

  public IMetaStore getActiveMetaStore() throws MetaStoreException {
    if ( activeMetaStoreName == null ) {
      return null;
    }

    IMetaStore metaStore = getMetaStore( activeMetaStoreName );
    return metaStore;
  }

  public IMetaStore getMetaStore( String metaStoreName ) throws MetaStoreException {
    for ( IMetaStore metaStore : metaStoreList ) {
      if ( metaStore.getName().equalsIgnoreCase( metaStoreName ) ) {
        return metaStore;
      }
    }
    return null;
  }

  private IMetaStoreElementType getElementTypeByName( List<IMetaStoreElementType> types, String name ) {
    for ( IMetaStoreElementType type : types ) {
      if ( type.getName().equalsIgnoreCase( name ) ) {
        return type;
      }
    }
    return null;
  }

  @Override
  public List<IMetaStoreElementType> getElementTypes() throws MetaStoreException {
    List<IMetaStoreElementType> elementTypes = new ArrayList<IMetaStoreElementType>();
    for ( IMetaStore metaStore : getReadMetaStoreList() ) {
      for ( IMetaStoreElementType elementType : metaStore.getElementTypes() ) {
        if ( getElementTypeByName( elementTypes, elementType.getName() ) == null ) {
          elementTypes.add( elementType );
        }
      }
    }
    return elementTypes;
  }

  @Override
  public List<String> getElementTypeIds() throws MetaStoreException {
    List<String> elementTypeIds = new ArrayList<>();
    for ( IMetaStoreElementType elementType : getElementTypes() ) {
      elementTypeIds.add( elementType.getId() );
    }
    return elementTypeIds;
  }

  @Override
  public IMetaStoreElementType getElementType( String elementTypeId ) throws MetaStoreException {
    for ( IMetaStoreElementType type : getElementTypes() ) {
      if ( type.getId().equals( elementTypeId ) ) {
        return type;
      }
    }
    return null;
  }

  @Override
  public IMetaStoreElementType getElementTypeByName( String elementTypeName )
    throws MetaStoreException {
    return getElementTypeByName( getElementTypes(), elementTypeName );
  }

  @Override
  public void createElementType( IMetaStoreElementType elementType ) throws MetaStoreException,
    MetaStoreElementTypeExistsException {
    IMetaStore metaStore = getWriteMetaStore();
    metaStore.createElementType( elementType );
  }

  @Override
  public void updateElementType( IMetaStoreElementType elementType ) throws MetaStoreException {
    IMetaStore metaStore = getWriteMetaStore();
    metaStore.updateElementType( elementType );
  }

  @Override
  public void deleteElementType( IMetaStoreElementType elementType ) throws MetaStoreException,
    MetaStoreDependenciesExistsException {
    IMetaStore metaStore = getWriteMetaStore();
    metaStore.deleteElementType( elementType );
  }

  private IMetaStoreElement getElementByName( List<IMetaStoreElement> elements, String name ) {
    for ( IMetaStoreElement element : elements ) {
      if ( element.getName().equals( name ) ) {
        return element;
      }
    }
    return null;
  }

  @Override
  public List<IMetaStoreElement> getElements( IMetaStoreElementType elementType )
    throws MetaStoreException {
    List<IMetaStoreElement> elements = new ArrayList<IMetaStoreElement>();
    for ( IMetaStore metaStore : getReadMetaStoreList() ) {
      IMetaStoreElementType localElementType = metaStore.getElementTypeByName( elementType.getName() );
      if ( localElementType != null ) {
        for ( IMetaStoreElement element : metaStore.getElements( localElementType ) ) {
          if ( getElementByName( elements, element.getName() ) == null ) {
            elements.add( element );
          }
        }
      }
    }
    return elements;
  }

  @Override
  public List<String> getElementIds( IMetaStoreElementType elementType ) throws MetaStoreException {
    List<String> elementIds = new ArrayList<>();
    for ( IMetaStoreElement element : getElements( elementType ) ) {
      elementIds.add( element.getId() );
    }
    return elementIds;
  }

  @Override
  public IMetaStoreElement getElement( IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    for ( IMetaStore localStore : getReadMetaStoreList() ) {
      if ( elementType.getMetaStoreName() == null || elementType.getMetaStoreName().equals( localStore.getName() ) ) {
        IMetaStoreElementType localType = localStore.getElementTypeByName( elementType.getName() );
        if ( localType != null ) {
          for ( IMetaStoreElement element : localStore.getElements( localType ) ) {
            if ( element.getId().equals( elementId ) ) {
              return element;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public IMetaStoreElement getElementByName( IMetaStoreElementType elementType, String name )
    throws MetaStoreException {
    return getElementByName( getElements( elementType ), name );
  }

  @Override
  public void createElement( IMetaStoreElementType elementType, IMetaStoreElement element )
    throws MetaStoreException, MetaStoreElementExistException {
    getWriteMetaStore().createElement( elementType, element );
  }

  @Override
  public void deleteElement( IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    getWriteMetaStore().deleteElement( elementType, elementId );
  }

  @Override
  public void updateElement( IMetaStoreElementType elementType, String elementId,
                             IMetaStoreElement element ) throws MetaStoreException {
    getWriteMetaStore().updateElement( elementType, elementId, element );
  }

  @Override
  public IMetaStoreElementType newElementType() throws MetaStoreException {
    return getWriteMetaStore().newElementType();
  }

  @Override
  public IMetaStoreElement newElement() throws MetaStoreException {
    return getWriteMetaStore().newElement();
  }

  @Override
  public IMetaStoreElement newElement( IMetaStoreElementType elementType, String id, Object value )
    throws MetaStoreException {
    return getWriteMetaStore().newElement( elementType, id, value );
  }

  public IMetaStoreAttribute newAttribute( String id, Object value ) throws MetaStoreException {
    return getWriteMetaStore().newAttribute( id, value );
  }

  @Override
  public IMetaStoreElementOwner newElementOwner( String name, MetaStoreElementOwnerType ownerType )
    throws MetaStoreException {
    return getWriteMetaStore().newElementOwner( name, ownerType );
  }

  @Override
  public String getName() throws MetaStoreException {
    IMetaStore activeMetaStore = getActiveMetaStore();
    if ( activeMetaStore != null ) {
      return activeMetaStore.getName();
    }
    return "DelegatingMetaStore";
  }

  @Override
  public String getDescription() throws MetaStoreException {
    IMetaStore activeMetaStore = getActiveMetaStore();
    if ( activeMetaStore != null ) {
      return activeMetaStore.getDescription();
    }
    return "The DelegatingMetaStore can act as a read-only aggregation of multiple MetaStores and can write if an active one is set";
  }

  @Override
  public void setTwoWayPasswordEncoder( ITwoWayPasswordEncoder encoder ) {
    this.passwordEncoder = encoder;
  }

  @Override
  public ITwoWayPasswordEncoder getTwoWayPasswordEncoder() {
    return passwordEncoder;
  }
}
