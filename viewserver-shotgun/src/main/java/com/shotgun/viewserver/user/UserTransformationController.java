package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.slf4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface UserTransformationController extends UserPersistenceController{

    interface ITranformation<TUSer>{
        TUSer call(TUSer order);
    }

    default <T extends User> T transform(String userId, Predicate<T> tranformation, Class<T> orderClass){
        if(userId == null){
            throw new RuntimeException("User id is required");
        }
        return transform(userId,tranformation,c->{}, orderClass);
    }
    default <T extends User> T transform(String userId, Predicate<T> tranformation, Consumer<T> afterTransform, Class<T> orderClass){

        T user = getUserForId(userId, orderClass);

        if(tranformation.test(user)){
            addOrUpdateUser(user, null);
        }

        afterTransform.accept(user);
        return user;
    }




}
