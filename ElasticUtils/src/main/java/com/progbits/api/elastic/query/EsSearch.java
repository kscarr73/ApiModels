package com.progbits.api.elastic.query;

import com.progbits.api.elastic.aggs.Aggregates;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulate Search Requirements for ElasticSearch
 *
 * @author scarr
 */
public class EsSearch implements Query {

    private MainQuery query = new MainQuery();
    private Aggregates aggregates = new Aggregates();

    private Integer start = null;
    private Integer count = null;
    private List<String> fields = new ArrayList<>();
    private List<String> sortFields = new ArrayList<>();

    private String _version = "2";

    public EsSearch() {

    }

    public EsSearch(String version) {
        _version = version;
    }

    public void setVersion(String ver) {
        _version = ver;
    }

    public MainQuery getQuery() {
        return query;
    }

    public void setQuery(MainQuery query) {
        this.query = query;
    }

    public Aggregates getAggregates() {
        return aggregates;
    }

    public void setAggregates(Aggregates aggregates) {
        this.aggregates = aggregates;
    }

    public int getStart() {
        if (start == null) {
            return 0;
        } else {
            return start;
        }
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getCount() {
        if (count == null) {
            return 0;
        } else {
            return count;
        }
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addField(String field) {
        fields.add(field);
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public void addSortField(String field) {
        sortFields.add(field);
    }

    public List<String> getSortFields() {
        return sortFields;
    }

    public void setSortFields(List<String> sortFields) {
        this.sortFields = sortFields;
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();

        toJson(sb);

        return sb.toString();
    }

    @Override
    public void toJson(StringBuilder sb) {
        boolean bCountWritten = false;

        sb.append(" { ");

        if (count != null && count > 0) {
            JsonFunctions.processFieldName(sb, "from");

            if (start == null) {
                JsonFunctions.processFieldValue(sb, 0);
            } else {
                JsonFunctions.processFieldValue(sb, start);
            }

            sb.append(",");

            JsonFunctions.processFieldName(sb, "size");
            JsonFunctions.processFieldValue(sb, count);

            if (fields != null && fields.size() > 0) {
                sb.append(",");

                JsonFunctions.processFieldName(sb, "_source");

                sb.append("[");

                int iCnt = 0;
                for (String fld : fields) {
                    if (iCnt > 0) {
                        sb.append(",");
                    }

                    JsonFunctions.processFieldValue(sb, fld, false);

                    iCnt++;
                }

                sb.append("]");
            }
            bCountWritten = true;
        }

        if (bCountWritten) {
            sb.append(",");
        }

        query.toJson(sb);

        if (sortFields != null && sortFields.size() > 0) {
            sb.append(",");

            JsonFunctions.processFieldName(sb, "sort");

            sb.append(" [ ");

            int iSort = 0;
            for (String sortField : sortFields) {
                if (iSort > 0) {
                    sb.append(",");
                }

                if (sortField.startsWith("+")) {
                    sb.append("{\"");
                    sb.append(sortField.substring(1));
                    sb.append("\" : {\"order\" : \"asc\"}}");
                } else if (sortField.startsWith("-")) {
                    sb.append("{\"");
                    sb.append(sortField.substring(1));
                    sb.append("\" : {\"order\" : \"desc\"}}");
                } else {
                    sb.append("{\"");
                    sb.append(sortField.substring(1));
                    sb.append("\" : {\"order\" : \"asc\"}}");
                }

                iSort++;
            }

            sb.append(" ] ");
        }

        if (aggregates != null && aggregates.getAggregates().size() > 0) {
            sb.append(",");

            //sb.append(" { ");
            aggregates.toJson(sb);

            //sb.append(" } ");
        }

        sb.append(" } ");
    }

}
