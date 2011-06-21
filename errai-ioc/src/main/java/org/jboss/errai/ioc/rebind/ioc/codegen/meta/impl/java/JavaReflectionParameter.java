/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassMember;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;
import org.mvel2.util.ReflectionUtil;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionParameter implements MetaParameter {
  private String name;
  private Class<?> type;
  private Annotation[] annotations;
  private MetaClassMember declaredBy;

  public JavaReflectionParameter(Class<?> type, Annotation[] annotations, MetaClassMember declaredBy) {
    this.name = ReflectionUtil.getPropertyFromAccessor(type.getSimpleName());
    this.type = type;
    this.annotations = annotations;
    this.declaredBy = declaredBy;
  }

  public String getName() {
    return name;
  }

  public MetaClass getType() {
    return MetaClassFactory.get(type);
  }

  public Annotation[] getAnnotations() {
    return annotations == null ? new Annotation[0] : annotations;
  }

  public final <A extends Annotation> A getAnnotation(Class<A> annotation) {
    for (Annotation a : getAnnotations()) {
      if (a.annotationType().equals(annotation)) return (A) a;
    }
    return null;
  }

  public final boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
  }


  public MetaClassMember getDeclaringMember() {
    return declaredBy;
  }
}
