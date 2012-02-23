/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.VariableReference;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.ioc.client.InterfaceInjectionContext;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.JSR330QualifyingMetadataFactory;
import org.jboss.errai.ioc.rebind.ioc.QualifyingMetadataFactory;
import org.jboss.errai.ioc.rebind.ioc.TypeDiscoveryListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IOCProcessingContext  {
  protected Context context;
  protected BuildMetaClass bootstrapClass;
  protected BlockBuilder<?> blockBuilder;
  protected List<String> packages;
  
  protected List<Statement> appendToEnd;
  protected List<TypeDiscoveryListener> typeDiscoveryListeners;

  protected TreeLogger treeLogger;
  protected GeneratorContext generatorContext;

  protected SourceWriter writer;
  protected TypeOracle oracle;
  
  protected Variable contextVariable = Variable.create("ctx", InterfaceInjectionContext.class);

  protected QualifyingMetadataFactory qualifyingMetadataFactory = new JSR330QualifyingMetadataFactory();

  public IOCProcessingContext(TreeLogger treeLogger,
                              GeneratorContext generatorContext,
                              SourceWriter writer,
                              TypeOracle oracle,
                              Context context,
                              BuildMetaClass bootstrapClass,
                              BlockBuilder<?> blockBuilder) {
    this.treeLogger = treeLogger;
    this.generatorContext = generatorContext;
    this.writer = writer;
    this.context = context;
    this.bootstrapClass = bootstrapClass;
    this.blockBuilder = blockBuilder;
    this.appendToEnd = new ArrayList<Statement>();
    this.typeDiscoveryListeners = new ArrayList<TypeDiscoveryListener>();
  }


  public BlockBuilder<?> getBlockBuilder() {
    return blockBuilder;
  }

  public BlockBuilder<?> append(Statement statement) {
    return blockBuilder.append(statement);
  }

  public void appendToEnd(Statement statement) {
    appendToEnd.add(statement);
  }
  
  public List<Statement> getAppendToEnd() {
    return Collections.unmodifiableList(appendToEnd);
  }
  
  public BuildMetaClass getBootstrapClass() {
    return bootstrapClass;
  }

  public Context getContext() {
    return context;
  }

  public void setPackages(List<String> packages) {
    this.packages = packages;
  }

  public List<String> getPackages() {
    return packages;
  }

  public TreeLogger getTreeLogger() {
    return treeLogger;
  }

  public GeneratorContext getGeneratorContext() {
    return generatorContext;
  }

  public SourceWriter getWriter() {
    return writer;
  }

  public TypeOracle getOracle() {
    return oracle;
  }

  public Variable getContextVariable() {
    return contextVariable;
  }
  
  public VariableReference getContextVariableReference() {
    return contextVariable.getReference();
  }

  public QualifyingMetadataFactory getQualifyingMetadataFactory() {
    return qualifyingMetadataFactory;
  }

  public void setQualifyingMetadataFactory(QualifyingMetadataFactory qualifyingMetadataFactory) {
    this.qualifyingMetadataFactory = qualifyingMetadataFactory;
  }

  public void registerTypeDiscoveryListener(TypeDiscoveryListener discoveryListener) {
    this.typeDiscoveryListeners.add(discoveryListener);
  }

  public void handleDiscoveryOfType(InjectionPoint injectionPoint) {
    for (TypeDiscoveryListener listener : typeDiscoveryListeners) {
      listener.onDiscovery(this, injectionPoint);
    }
  }
}
