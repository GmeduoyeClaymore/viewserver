package com.shotgun.viewserver.user;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface SavedBankAccount extends DynamicJsonBackedObject {
    String getBankName();
    String getLast4();
    String getSortCode();
}
