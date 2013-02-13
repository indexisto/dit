/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.handler.dataimport.processor;

import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.WARN;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.wrapAndThrow;
import static org.apache.solr.handler.dataimport.processor.EntityProcessorBase.ABORT;
import static org.apache.solr.handler.dataimport.processor.EntityProcessorBase.ON_ERROR;
import static org.apache.solr.handler.dataimport.processor.EntityProcessorBase.SKIP;
import static org.apache.solr.handler.dataimport.processor.EntityProcessorBase.TRANSFORMER;
import static org.apache.solr.handler.dataimport.processor.EntityProcessorBase.TRANSFORM_ROW;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrException;
import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DataImportHandlerException;
import org.apache.solr.handler.dataimport.DocBuilder;
import org.apache.solr.handler.dataimport.VariableResolverImpl;
import org.apache.solr.handler.dataimport.config.ConfigNameConstants;
import org.apache.solr.handler.dataimport.config.Entity;
import org.apache.solr.handler.dataimport.datasource.DataSource;
import org.apache.solr.handler.dataimport.transformer.ScriptTransformer;
import org.apache.solr.handler.dataimport.transformer.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Wrapper over {@link EntityProcessor} instance which performs transforms and handles multi-row outputs correctly.
 *
 * @since solr 1.4
 */
public class EntityProcessorWrapper extends EntityProcessor {
  private static final Logger log = LoggerFactory.getLogger(EntityProcessorWrapper.class);

  private final EntityProcessor delegate;
  private final Entity entity;
  private DataSource datasource;
  private final List<EntityProcessorWrapper> children = new ArrayList<EntityProcessorWrapper>();
  private final DocBuilder docBuilder;
  private boolean initalized;
  private String onError;
  private Context context;
  private VariableResolverImpl resolver;
  private String entityName;

  protected List<Transformer> transformers;

  protected List<Map<String, Object>> rowcache;

  public EntityProcessorWrapper(EntityProcessor delegate, Entity entity, DocBuilder docBuilder) {
    this.delegate = delegate;
    this.entity = entity;
    this.docBuilder = docBuilder;
  }

  @Override
  public void init(Context context) {
    rowcache = null;
    this.context = context;
    resolver = (VariableResolverImpl) context.getVariableResolver();
    if (entityName == null) {
      onError = resolver.replaceTokens(context.getEntityAttribute(ON_ERROR));
      if (onError == null) onError = ABORT;
      entityName = context.getEntityAttribute(ConfigNameConstants.NAME);
    }
    delegate.init(context);

  }

  @SuppressWarnings("unchecked")
  void loadTransformers() {
    final String transClasses = context.getEntityAttribute(TRANSFORMER);

    if (transClasses == null) {
      transformers = Collections.EMPTY_LIST;
      return;
    }

    final String[] transArr = transClasses.split(",");
    transformers = new ArrayList<Transformer>() {
      @Override
      public boolean add(Transformer transformer) {
        if (docBuilder != null && docBuilder.verboseDebug) {
          transformer = docBuilder.getDebugLogger().wrapTransformer(transformer);
        }
        return super.add(transformer);
      }
    };
    for (final String aTransArr : transArr) {
      final String trans = aTransArr.trim();
      if (trans.startsWith("script:")) {
        final String functionName = trans.substring("script:".length());
        final ScriptTransformer scriptTransformer = new ScriptTransformer();
        scriptTransformer.setFunctionName(functionName);
        transformers.add(scriptTransformer);
        continue;
      }
      try {
        final Class clazz = DocBuilder.loadClass(trans, context.getSolrCore());
        if (Transformer.class.isAssignableFrom(clazz)) {
          transformers.add((Transformer) clazz.newInstance());
        } else {
          final Method meth = clazz.getMethod(TRANSFORM_ROW, Map.class);
          transformers.add(new ReflectionTransformer(meth, clazz, trans));
        }
      } catch (final NoSuchMethodException nsme){
         final String msg = "Transformer :"
                    + trans
                    + "does not implement Transformer interface or does not have a transformRow(Map<String.Object> m)method";
            log.error(msg);
            wrapAndThrow(SEVERE, nsme,msg);
      } catch (final Exception e) {
        log.error("Unable to load Transformer: " + aTransArr, e);
        wrapAndThrow(SEVERE, e,"Unable to load Transformer: " + trans);
      }
    }

  }

  @SuppressWarnings("unchecked")
  public static class ReflectionTransformer extends Transformer {
    final Method meth;

    final Class clazz;

    public final String trans;

    final Object o;

    public ReflectionTransformer(Method meth, Class clazz, String trans)
            throws Exception {
      this.meth = meth;
      this.clazz = clazz;
      this.trans = trans;
      o = clazz.newInstance();
    }

    @Override
    public Object transformRow(Map<String, Object> aRow, Context context) {
      try {
        return meth.invoke(o, aRow);
      } catch (final Exception e) {
        log.warn("method invocation failed on transformer : " + trans, e);
        throw new DataImportHandlerException(WARN, e);
      }
    }
  }

  protected Map<String, Object> getFromRowCache() {
    final Map<String, Object> r = rowcache.remove(0);
    if (rowcache.isEmpty())
      rowcache = null;
    return r;
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> applyTransformer(Map<String, Object> row) {
    if(row == null) return null;
    if (transformers == null)
      loadTransformers();
    if (transformers == Collections.EMPTY_LIST)
      return row;
    Map<String, Object> transformedRow = row;
    List<Map<String, Object>> rows = null;
    boolean stopTransform = checkStopTransform(row);
    final VariableResolverImpl resolver = (VariableResolverImpl) context.getVariableResolver();
    for (final Transformer t : transformers) {
      if (stopTransform) break;
      try {
        if (rows != null) {
          final List<Map<String, Object>> tmpRows = new ArrayList<Map<String, Object>>();
          for (final Map<String, Object> map : rows) {
            resolver.addNamespace(entityName, map);
            final Object o = t.transformRow(map, context);
            if (o == null)
              continue;
            if (o instanceof Map) {
              final Map oMap = (Map) o;
              stopTransform = checkStopTransform(oMap);
              tmpRows.add((Map) o);
            } else if (o instanceof List) {
              tmpRows.addAll((List) o);
            } else {
              log.error("Transformer must return Map<String, Object> or a List<Map<String, Object>>");
            }
          }
          rows = tmpRows;
        } else {
          resolver.addNamespace(entityName, transformedRow);
          final Object o = t.transformRow(transformedRow, context);
          if (o == null)
            return null;
          if (o instanceof Map) {
            final Map oMap = (Map) o;
            stopTransform = checkStopTransform(oMap);
            transformedRow = (Map) o;
          } else if (o instanceof List) {
            rows = (List) o;
          } else {
            log.error("Transformer must return Map<String, Object> or a List<Map<String, Object>>");
          }
        }
      } catch (final Exception e) {
        log.warn("transformer threw error", e);
        if (ABORT.equals(onError)) {
          wrapAndThrow(SEVERE, e);
        } else if (SKIP.equals(onError)) {
          wrapAndThrow(DataImportHandlerException.SKIP, e);
        }
        // onError = continue
      }
    }
    if (rows == null) {
      return transformedRow;
    } else {
      rowcache = rows;
      return getFromRowCache();
    }

  }

  private boolean checkStopTransform(Map oMap) {
    return oMap.get("$stopTransform") != null
            && Boolean.parseBoolean(oMap.get("$stopTransform").toString());
  }

  @Override
  public Map<String, Object> nextRow() {
    if (rowcache != null) {
      return getFromRowCache();
    }
    while (true) {
      Map<String, Object> arow = null;
      try {
        arow = delegate.nextRow();
      } catch (final Exception e) {
        if(ABORT.equals(onError)){
          wrapAndThrow(SEVERE, e);
        } else {
          //SKIP is not really possible. If this calls the nextRow() again the Entityprocessor would be in an inconisttent state
          SolrException.log(log, "Exception in entity : "+ entityName, e);
          return null;
        }
      }
      if (arow == null) {
        return null;
      } else {
        arow = applyTransformer(arow);
        if (arow != null) {
          delegate.postTransform(arow);
          return arow;
        }
      }
    }
  }

  @Override
  public Map<String, Object> nextModifiedRowKey() {
    Map<String, Object> row = delegate.nextModifiedRowKey();
    row = applyTransformer(row);
    rowcache = null;
    return row;
  }

  @Override
  public Map<String, Object> nextDeletedRowKey() {
    Map<String, Object> row = delegate.nextDeletedRowKey();
    row = applyTransformer(row);
    rowcache = null;
    return row;
  }

  @Override
  public Map<String, Object> nextModifiedParentRowKey() {
    return delegate.nextModifiedParentRowKey();
  }

  @Override
  public void destroy() {
    delegate.destroy();
  }

  public VariableResolverImpl getVariableResolver() {
    return (VariableResolverImpl) context.getVariableResolver();
  }

  public Context getContext() {
    return context;
  }

  @Override
  public void close() {
    delegate.close();
  }

  public Entity getEntity() {
    return entity;
  }

  public List<EntityProcessorWrapper> getChildren() {
    return children;
  }

  public DataSource getDatasource() {
    return datasource;
  }

  public void setDatasource(DataSource datasource) {
    this.datasource = datasource;
  }

  public boolean isInitalized() {
    return initalized;
  }

  public void setInitalized(boolean initalized) {
    this.initalized = initalized;
  }
}
