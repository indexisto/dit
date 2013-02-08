package com.indexisto.dit.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DataSource;
import org.apache.solr.handler.dataimport.EntityProcessorBase;
import org.apache.solr.handler.dataimport.VariableResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indexisto.dit.data.exception.DataSourceException;
import com.indexisto.dit.data.exception.MappingException;

/*
 * TODO: check cache support
 */
public class HierarchyProcessor extends EntityProcessorBase {
    private static final Logger log = LoggerFactory.getLogger(HierarchyProcessor.class);

    public static final String QUERY = "query";
    public static final String START_VALUE = "startValue";
    public static final String PARENT_ID_FIELD = "parentIdField";
    public static final String HIERARCHY_END_VALUE = "hierarchyEndValue";
    public static final String PLACEHOLDER = "placeholder";

    protected DataSource<Iterator<Map<String, Object>>> dataSource;
    private Map<String, Object> row;
    private List<Map<String, Object>> rows;
    private Iterator<Map<String, Object>> recursiveRowIterator;
    private Map<String, Object> result;

    private String startValue;
    private String parentIdField;
    private String hierarchyEndValue;
    private String queryTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public void init(Context context) {
        super.init(context);
        dataSource = context.getDataSource();
    }

    protected String prepareQuery(String id) {
        final Map<String, Object> defaults = new HashMap<String, Object>();
        defaults.put(PLACEHOLDER, id);
        final VariableResolver vr = context.getVariableResolver();
        vr.addNamespace(null, defaults);
        return vr.replaceTokens(queryTemplate);
    }

    protected Map<String, Object> getFirst(Iterator<Map<String, Object>> rowIterator) {
        if (rowIterator == null) return null;
        if (rowIterator.hasNext()) return rowIterator.next();
        return null;
    }

    protected void initQuery() {
        try {
            rows = new ArrayList<Map<String, Object>>();
            result = new HashMap<String, Object>();
            rows.add(result);
            rowIterator = rows.iterator();

            recursiveRowIterator = dataSource.getData(prepareQuery(startValue));
            row = getFirst(recursiveRowIterator);
            if (row == null || row.size() == 0) return;

            Set<String> keys = row.keySet();
            for (final String key : keys) {
                final List list = new ArrayList<Object>();
                list.add(row.get(key));
                result.put(key, list);
            }

            Object parentId = row.get(parentIdField);
            while (parentId.toString() != null && !parentId.equals(hierarchyEndValue)) {
                recursiveRowIterator = dataSource.getData(prepareQuery((String) parentId));
                row = getFirst(recursiveRowIterator);
                if (row != null && row.size() > 0) {
                    keys = row.keySet();
                    for (final String key : keys) {
                        ((ArrayList<Object>) result.get(key)).add(row.get(key));
                    }
                    parentId = row.get(parentIdField);
                } else {
                    break;
                }
            }

            query = queryTemplate;
        } catch (final Exception e) {
            final String message = "The query failed: '" + queryTemplate + "'";
            log.error(message, e);
            throw new DataSourceException(message, e);
        }
    }

    @Override
    public Map<String, Object> nextRow() {
        if (rowIterator == null) {
            startValue = getStartValue();
            parentIdField = getParentIdField();
            hierarchyEndValue = getHierarchyEndValue();
            queryTemplate = getQuery();
            log.debug("startValue: " + startValue);
            log.debug("parentIdField: " + parentIdField);
            log.debug("hierarchyEndValue: " + hierarchyEndValue);
            log.debug("queryTemplate: " + queryTemplate);
            if (startValue == null || parentIdField == null || queryTemplate == null ||
                    startValue.equals("") || parentIdField.equals("") || queryTemplate.equals("")) {
                throw new MappingException("startValue or parentIdField attribute not specified");
            }
            initQuery();
        }
        return getNext();
    }


    public String getQuery() {
        final String queryString = context.getEntityAttribute(QUERY);
        return queryString;
    }

    public String getStartValue() {
        final String startValue = context.getResolvedEntityAttribute(START_VALUE);
        return startValue;
    }

    public String getParentIdField() {
        final String parentIdField = context.getResolvedEntityAttribute(PARENT_ID_FIELD);
        return parentIdField;
    }

    public String getHierarchyEndValue() {
        final String hierarchyEndValue = context.getResolvedEntityAttribute(HIERARCHY_END_VALUE);
        return hierarchyEndValue;
    }
}