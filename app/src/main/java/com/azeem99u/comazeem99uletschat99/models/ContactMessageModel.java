package com.azeem99u.comazeem99uletschat99.models;

import java.util.Objects;

public class ContactMessageModel {
    String userKey;


    public ContactMessageModel(String userKey) {
        this.userKey = userKey;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactMessageModel that = (ContactMessageModel) o;
        return Objects.equals(userKey, that.userKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userKey);
    }
}
