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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.handler.dataimport.DataImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigParseUtil {
  private static final Logger LOG = LoggerFactory .getLogger(ConfigParseUtil.class);

  public static String getStringAttribute(Element e, String name, String def) {
    String r = e.getAttribute(name);
    if (r == null || "".equals(r.trim())) r = def;
    return r;
  }

  public static HashMap<String,String> getAllAttributes(Element e) {
    final HashMap<String,String> m = new HashMap<String,String>();
    final NamedNodeMap nnm = e.getAttributes();
    for (int i = 0; i < nnm.getLength(); i++) {
      m.put(nnm.item(i).getNodeName(), nnm.item(i).getNodeValue());
    }
    return m;
  }

  public static String getText(Node elem, StringBuilder buffer) {
    if (elem.getNodeType() != Node.CDATA_SECTION_NODE) {
      final NodeList childs = elem.getChildNodes();
      for (int i = 0; i < childs.getLength(); i++) {
        final Node child = childs.item(i);
        final short childType = child.getNodeType();
        if (childType != Node.COMMENT_NODE
            && childType != Node.PROCESSING_INSTRUCTION_NODE) {
          getText(child, buffer);
        }
      }
    } else {
      buffer.append(elem.getNodeValue());
    }

    return buffer.toString();
  }

  public static List<Element> getChildNodes(Element e, String byName) {
    final List<Element> result = new ArrayList<Element>();
    final NodeList l = e.getChildNodes();
    for (int i = 0; i < l.getLength(); i++) {
      if (e.equals(l.item(i).getParentNode())
          && byName.equals(l.item(i).getNodeName())) result.add((Element) l
          .item(i));
    }
    return result;
  }

  public static void verifyWithSchema(DataImporter di, Map<String,EntityField> fields) {
    // Solr cut off
    /*Map<String,SchemaField> schemaFields = null;
    if (di.getSchema() == null) {
      schemaFields = Collections.emptyMap();
    } else {
      schemaFields = di.getSchema().getFields();
    }
    for (final Map.Entry<String,SchemaField> entry : schemaFields.entrySet()) {
      final SchemaField sf = entry.getValue();
      if (!fields.containsKey(sf.getName())) {
        if (sf.isRequired()) {
          LOG
              .info(sf.getName()
                  + " is a required field in SolrSchema . But not found in DataConfig");
        }
      }
    }
    for (final Map.Entry<String,EntityField> entry : fields.entrySet()) {
      final EntityField fld = entry.getValue();
      final SchemaField field = di.getSchemaField(fld.getName());
      if (field == null) {
        LOG
            .info("The field :"
                + fld.getName()
                + " present in DataConfig does not have a counterpart in Solr Schema");
      }
    }*/
  }

  public static Map<String,EntityField> gatherAllFields(DataImporter di, Entity e) {
    final Map<String,EntityField> fields = new HashMap<String,EntityField>();
    if (e.getFields() != null) {
      for (final EntityField f : e.getFields()) {
        fields.put(f.getName(), f);
      }
    }
    for (final Entity e1 : e.getChildren()) {
      fields.putAll(gatherAllFields(di, e1));
    }
    return fields;
  }
}
