package com.github.xini1.apigateway.dto;

import com.github.xini1.orders.read.rpc.TopOrderedItemResponse;

import java.util.Objects;

/**
 * @author Maxim Tereshchenko
 */
public final class TopOrderedItemDto {

    private String id;
    private String name;
    private long timesOrdered;

    public TopOrderedItemDto() {
    }

    public TopOrderedItemDto(TopOrderedItemResponse topOrderedItemResponse) {
        this.id = topOrderedItemResponse.getId();
        this.name = topOrderedItemResponse.getName();
        this.timesOrdered = topOrderedItemResponse.getTimesOrdered();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimesOrdered() {
        return timesOrdered;
    }

    public void setTimesOrdered(long timesOrdered) {
        this.timesOrdered = timesOrdered;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, timesOrdered);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (TopOrderedItemDto) object;
        return timesOrdered == that.timesOrdered &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }
}
