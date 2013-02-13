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
package org.apache.solr.handler.dataimport.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DataImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A {@link Transformer} implementation which uses Regular Expressions to extract, split
 * and replace data in fields.
 * </p>
 * <p/>
 * <p>
 * Refer to <a
 * href="http://wiki.apache.org/solr/DataImportHandler">http://wiki.apache.org/solr/DataImportHandler</a>
 * for more details.
 * </p>
 * <p/>
 * <b>This API is experimental and may change in the future.</b>
 *
 * @since solr 1.3
 * @see Pattern
 */
public class RegexTransformer extends Transformer {
  private static final Logger LOG = LoggerFactory.getLogger(RegexTransformer.class);

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> transformRow(Map<String, Object> row,
                                          Context ctx) {
    final List<Map<String, String>> fields = ctx.getAllEntityFields();
    for (final Map<String, String> field : fields) {
      final String col = field.get(DataImporter.COLUMN);
      final String reStr = ctx.replaceTokens(field.get(REGEX));
      final String splitBy = ctx.replaceTokens(field.get(SPLIT_BY));
      final String replaceWith = ctx.replaceTokens(field.get(REPLACE_WITH));
      final String groupNames = ctx.replaceTokens(field.get(GROUP_NAMES));
      if (reStr != null || splitBy != null) {
        String srcColName = field.get(SRC_COL_NAME);
        if (srcColName == null) {
          srcColName = col;
        }
        final Object tmpVal = row.get(srcColName);
        if (tmpVal == null)
          continue;

        if (tmpVal instanceof List) {
          final List<String> inputs = (List<String>) tmpVal;
          final List results = new ArrayList();
          Map<String,List> otherVars= null;
          for (final String input : inputs) {
            final Object o = process(col, reStr, splitBy, replaceWith, input, groupNames);
            if (o != null){
              if (o instanceof Map) {
                final Map map = (Map) o;
                for (final Object e : map.entrySet()) {
                  final Map.Entry<String ,Object> entry = (Map.Entry<String, Object>) e;
                  List l = results;
                  if(!col.equals(entry.getKey())){
                    if(otherVars == null) otherVars = new HashMap<String, List>();
                    l = otherVars.get(entry.getKey());
                    if(l == null){
                      l = new ArrayList();
                      otherVars.put(entry.getKey(), l);
                    }
                  }
                  if (entry.getValue() instanceof Collection) {
                    l.addAll((Collection) entry.getValue());
                  } else {
                    l.add(entry.getValue());
                  }
                }
              } else {
                if (o instanceof Collection) {
                  results.addAll((Collection) o);
                } else {
                  results.add(o);
                }
              }
            }
          }
          row.put(col, results);
          if(otherVars != null) row.putAll(otherVars);
        } else {
          final String value = tmpVal.toString();
          final Object o = process(col, reStr, splitBy, replaceWith, value, groupNames);
          if (o != null){
            if (o instanceof Map) {
              row.putAll((Map) o);
            } else{
              row.put(col, o);
            }
          }
        }
      }
    }
    return row;
  }

  private Object process(String col, String reStr, String splitBy,
                         String replaceWith, String value, String groupNames) {
    if (splitBy != null) {
      return readBySplit(splitBy, value);
    } else if (replaceWith != null) {
      final Pattern p = getPattern(reStr);
      final Matcher m = p.matcher(value);
      return m.find()? m.replaceAll(replaceWith): null;
    } else {
      return readfromRegExp(reStr, value, col, groupNames);
    }
  }

  @SuppressWarnings("unchecked")
  private List<String> readBySplit(String splitBy, String value) {
    final String[] vals = value.split(splitBy);
    final List<String> l = new ArrayList<String>();
    l.addAll(Arrays.asList(vals));
    return l;
  }

  @SuppressWarnings("unchecked")
  private Object readfromRegExp(String reStr, String value, String columnName, String gNames) {
    String[] groupNames = null;
    if(gNames != null && gNames.trim().length() >0){
      groupNames =  gNames.split(",");
    }
    final Pattern regexp = getPattern(reStr);
    final Matcher m = regexp.matcher(value);
    if (m.find() && m.groupCount() > 0) {
      if (m.groupCount() > 1) {
        List l = null;
        Map<String ,String > map = null;
        if(groupNames == null){
          l = new ArrayList();
        } else {
          map =  new HashMap<String, String>();
        }
        for (int i = 1; i <= m.groupCount(); i++) {
          try {
            if(l != null){
              l.add(m.group(i));
            } else if (map != null ){
              if(i <= groupNames.length){
                final String nameOfGroup = groupNames[i-1];
                if(nameOfGroup != null && nameOfGroup.trim().length() >0){
                  map.put(nameOfGroup, m.group(i));
                }
              }
            }
          } catch (final Exception e) {
            LOG.warn("Parsing failed for field : " + columnName, e);
          }
        }
        return l == null ? map: l;
      } else {
        return m.group(1);
      }
    }

    return null;
  }

  private Pattern getPattern(String reStr) {
    Pattern result = PATTERN_CACHE.get(reStr);
    if (result == null) {
      PATTERN_CACHE.put(reStr, result = Pattern.compile(reStr));
    }
    return result;
  }

  private final HashMap<String, Pattern> PATTERN_CACHE = new HashMap<String, Pattern>();

  public static final String REGEX = "regex";

  public static final String REPLACE_WITH = "replaceWith";

  public static final String SPLIT_BY = "splitBy";

  public static final String SRC_COL_NAME = "sourceColName";

  public static final String GROUP_NAMES = "groupNames";

}
