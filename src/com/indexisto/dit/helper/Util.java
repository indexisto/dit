package com.indexisto.dit.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
//import org.apache.log4j.Logger;

public class Util {

    //private static Logger log = Logger.getLogger(Util.class);

    @SuppressWarnings("unchecked")
    public static Map createMap(Object... args) {
        final Map result = new LinkedHashMap();
        if (args == null || args.length == 0)
            return result;
        for (int i = 0; i < args.length - 1; i += 2)
            result.put(args[i], args[i + 1]);
        return result;
    }

    public static List<Map<String, Object>> mapJsonArray(String arrayJsonString)
            throws JSONException {
        final List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();

        final JSONArray jsonArray = new JSONArray(arrayJsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            final Map<String, Object> responseRow = new HashMap<String, Object>();
            final JSONObject row = jsonArray.getJSONObject(i);
            final Iterator<String> keys = row.keys();
            while (keys.hasNext()) {
                final String key = keys.next();
                responseRow.put(key, row.get(key).toString());
            }
            response.add(responseRow);
        }
        return response;
    }

    public static String readFileToString(String fileName) {
        FileInputStream fileInputStream = null;
        try {
            final File file = new File(fileName);
            fileInputStream = new FileInputStream(file);
            return IOUtils.toString(fileInputStream, "UTF-8");
        } catch (final Exception e) {} finally {
            try {
                if (fileInputStream != null) fileInputStream.close();
            } catch (final IOException ex) {}
        }
        return null;
    }
}