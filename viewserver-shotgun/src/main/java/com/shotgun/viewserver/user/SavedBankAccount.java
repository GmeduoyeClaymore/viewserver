package com.shotgun.viewserver.user;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface SavedBankAccount extends DynamicJsonBackedObject {
    String getId();
    String getBankName();
    String getLast4();
    String getSortCode();
    String getCountry();
}
