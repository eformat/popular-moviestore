package org.acme.data;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class MovieItem {
    private String id;
    private Integer count;

    public MovieItem() {
    }

    @ProtoFactory
    public MovieItem(String id, Integer count) {
        this.id = id;
        this.count = count;
    }

    @ProtoField(number = 1)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ProtoField(number = 2)
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
