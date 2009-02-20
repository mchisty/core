/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.webbeans.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javassist.util.proxy.ProxyFactory;

/**
 * Utilties for working with Javassist proxies
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 */
public class Proxies
{

   public static class TypeInfo
   {

      private static final Class<?>[] EMPTY_INTERFACES_ARRAY = new Class<?>[0];

      private final Set<Class<?>> interfaces;
      private final Set<Class<?>> classes;

      private TypeInfo()
      {
         super();
         this.interfaces = new LinkedHashSet<Class<?>>();
         this.classes = new LinkedHashSet<Class<?>>();
      }

      public Class<?> getSuperClass()
      {
         if (classes.isEmpty())
         {
            throw new AssertionError("TypeInfo not properly initialized");
         }
         Iterator<Class<?>> it = classes.iterator();
         Class<?> superclass = it.next();
         while (it.hasNext())
         {
            Class<?> clazz = it.next();
            if (superclass.isAssignableFrom(clazz))
            {
               superclass = clazz;
            }
         }
         return superclass;
      }

      private Class<?>[] getInterfaces()
      {
         return interfaces.toArray(EMPTY_INTERFACES_ARRAY);
      }

      public ProxyFactory createProxyFactory()
      {
         ProxyFactory proxyFactory = new ProxyFactory();
         proxyFactory.setSuperclass(getSuperClass());
         proxyFactory.setInterfaces(getInterfaces());
         return proxyFactory;
      }

      private void add(Type type)
      {
         if (type instanceof Class)
         {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isInterface())
            {
               interfaces.add(clazz);
            }
            else
            {
               classes.add(clazz);
            }
         }
         else
         {
            throw new IllegalArgumentException("Cannot proxy non-Class Type " + type);
         }
      }

      public static TypeInfo ofTypes(Set<Type> types)
      {
         TypeInfo typeInfo = new TypeInfo();
         for (Type type : types)
         {
            typeInfo.add(type);
         }
         return typeInfo;
      }

      public static TypeInfo ofClasses(Set<Class<?>> classes)
      {
         TypeInfo typeInfo = new TypeInfo();
         for (Class<?> type : classes)
         {
            typeInfo.add(type);
         }
         return typeInfo;
      }

   }

   /**
    * Get the proxy factory for the given set of types
    * 
    * @param types The types to create the proxy factory for
    * @param classes Additional interfaces the proxy should implement
    * @return the proxy factory
    */
   public static ProxyFactory getProxyFactory(Set<Type> types)
   {
      return TypeInfo.ofTypes(types).createProxyFactory();
   }

   /**
    * Indicates if a class is proxyable
    * 
    * @param type The class to test
    * @return True if proxyable, false otherwise
    */
   public static boolean isTypeProxyable(Type type)
   {
      if (type instanceof Class)
      {
         return isClassProxyable((Class<?>) type);
      }
      else if (type instanceof ParameterizedType)
      {
         Type rawType = ((ParameterizedType) type).getRawType();
         if (rawType instanceof Class)
         {
            return isClassProxyable((Class<?>) rawType);
         }
      }
      return false;
   }
      
   private static boolean isClassProxyable(Class<?> clazz)
   {
      if (clazz.isInterface())
      {
         return true;
      }
      else if (Reflections.getConstructor(clazz) == null)
      {
         return false;
      }
      else if (Reflections.isTypeOrAnyMethodFinal(clazz))
      {
         return false;
      }
      else if (Reflections.isPrimitive(clazz))
      {
         return false;
      }
      else if (Reflections.isArrayType(clazz))
      {
         return false;
      }
      else
      {
         return true;
      }
   }

}
