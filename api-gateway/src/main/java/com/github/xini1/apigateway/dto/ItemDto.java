package com.github.xini1.apigateway.dto;

import com.github.xini1.orders.read.rpc.ItemResponse;

import java.util.Objects;

/**
 * @author Maxim Tereshchenko
 */
public final class ItemDto {

    private String id;
    private String name;
    private boolean active;
    private long version;

    public ItemDto() {
    }

    public ItemDto(ItemResponse itemResponse) {
        this.id = itemResponse.getId();
        this.name = itemResponse.getName();
        this.active = itemResponse.getActive();
        this.version = itemResponse.getVersion();
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, active, version);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var itemDto = (ItemDto) object;
        return active == itemDto.active &&
                version == itemDto.version &&
                Objects.equals(id, itemDto.id) &&
                Objects.equals(name, itemDto.name);
    }
}
