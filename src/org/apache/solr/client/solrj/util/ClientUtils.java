package org.apache.solr.client.solrj.util;

/*
 * cut off class,
 *  for full version look in Apache Solr
 */
public class ClientUtils {

    /**
     * See: {@link org.apache.lucene.queryparser.classic queryparser syntax} for
     * more information on Escaping Special Characters
     */
    public static String escapeQueryChars(String s) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            // These characters are part of the query syntax and must be escaped
            if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '('
                    || c == ')' || c == ':' || c == '^' || c == '[' || c == ']'
                    || c == '\"' || c == '{' || c == '}' || c == '~'
                    || c == '*' || c == '?' || c == '|' || c == '&' || c == ';'
                    || c == '/' || Character.isWhitespace(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}