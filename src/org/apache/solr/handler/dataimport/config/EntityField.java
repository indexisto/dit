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

package org.apache.solr.handler.dataimport.config;

import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.handler.dataimport.ConfigParseUtil;
import org.apache.solr.handler.dataimport.DataImportHandlerException;
import org.apache.solr.handler.dataimport.DataImporter;
import org.w3c.dom.Element;

public class EntityField {
  private final String column;
  private final String name;
  private final float boost;
  private final boolean toWrite;
  private final boolean multiValued;
  private final boolean dynamicName;
  private final Entity entity;
  private final Map<String, String> allAttributes;

  public EntityField(Builder b) {
    column = b.column;
    name = b.name;
    boost = b.boost;
    toWrite = b.toWrite;
    multiValued = b.multiValued;
    dynamicName = b.dynamicName;
    entity = b.entity;
    allAttributes = Collections.unmodifiableMap(new HashMap<String,String>(b.allAttributes));
  }

  public String getName() {
    return name == null ? column : name;
  }

  public Entity getEntity() {
    return entity;
  }

  public String getColumn() {
    return column;
  }

  public float getBoost() {
    return boost;
  }

  public boolean isToWrite() {
    return toWrite;
  }

  public boolean isMultiValued() {
    return multiValued;
  }

  public boolean isDynamicName() {
    return dynamicName;
  }

  public Map<String,String> getAllAttributes() {
    return allAttributes;
  }

  public static class Builder {
    public String column;
    public String name;
    public float boost;
    public boolean toWrite = true;
    public boolean multiValued = false;
    public boolean dynamicName = false;
    public Entity entity;
    public Map<String, String> allAttributes = new HashMap<String,String>();

    public Builder(Element e) {
      name = ConfigParseUtil.getStringAttribute(e, DataImporter.NAME, null);
      column = ConfigParseUtil.getStringAttribute(e, DataImporter.COLUMN, null);
      // Vladimir Mikhel
      multiValued = Boolean.parseBoolean(ConfigParseUtil.getStringAttribute(e, DataImporter.MULTI_VALUED, "false"));
      // /
      if (column == null) {
        throw new DataImportHandlerException(SEVERE, "Field must have a column attribute");
      }
      boost = Float.parseFloat(ConfigParseUtil.getStringAttribute(e, "boost", "1.0f"));
      allAttributes = new HashMap<String, String>(ConfigParseUtil.getAllAttributes(e));
    }

    public String getNameOrColumn() {
      return name==null ? column : name;
    }
  }

}
