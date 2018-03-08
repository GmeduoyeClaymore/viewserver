package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.TableUpdater;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(name = "userRelationshipController")
public class UserRelationshipController {
    private static final Logger log = LoggerFactory.getLogger(UserRelationshipController.class);
    private TableUpdater tableUpdater;

    public UserRelationshipController(TableUpdater tableUpdater) {
        this.tableUpdater = tableUpdater;
    }

    @ControllerAction(path = "requestUserRelationship", isSynchronous = true)
    public String requestUserRelationship(@ActionParam(name = "toUserId", required = true) String toUserId, @ActionParam(name = "type", required = true) UserRelationshipType type){
        try {
            String fromUserId = getUserId();
            log.debug(String.format("Requesting user relationship from \"%s\" to \"%s\" type \"%s\"",fromUserId,toUserId,type));

            String value = ControllerUtils.generateGuid();
            Record relationshipRecord = new Record()
                    .addValue("relationshipId", value)
                    .addValue("fromUserId", fromUserId)
                    .addValue("toUserId", toUserId)
                    .addValue("type", type.name())
                    .addValue("status", UserRelationshipStatus.REQUESTED.name());

            tableUpdater.addOrUpdateRow(TableNames.USER_RELATIONSHIP_TABLE_NAME, "userRelationship", relationshipRecord);
            return value;

        } catch (Exception e) {
            log.error("There was a problem requesting user relationship", e);
            throw new RuntimeException(e);
        }
    }

    @ControllerAction(path = "updateUserRelationshipStatus", isSynchronous = true)
    public void updateUserRelationshipStatus(@ActionParam(name = "relationshipId", required = true) String relationshipId, @ActionParam(name = "type", required = true) UserRelationshipStatus status){
        try {
            Record relationshipRecord = new Record()
                    .addValue("relationshipId", relationshipId)
                    .addValue("status", status.name());

            tableUpdater.addOrUpdateRow(TableNames.USER_RELATIONSHIP_TABLE_NAME, "userRelationship", relationshipRecord);
        } catch (Exception e) {
            log.error("There was a problem updating user relationship status", e);
            throw new RuntimeException(e);
        }
    }

    @ControllerAction(path = "updateUserRelationshipType", isSynchronous = true)
    public void updateUserRelationshipType(@ActionParam(name = "relationshipId", required = true) String relationshipId, @ActionParam(name = "type", required = true) UserRelationshipType type){
        try {
            Record relationshipRecord = new Record()
                    .addValue("relationshipId", relationshipId)
                    .addValue("type", type.name());

            tableUpdater.addOrUpdateRow(TableNames.USER_RELATIONSHIP_TABLE_NAME, "userRelationship", relationshipRecord);
        } catch (Exception e) {
            log.error("There was a problem updating user relationship type", e);
            throw new RuntimeException(e);
        }
    }


    private String getUserId() {
        String userId = (String) ControllerContext.get("userId");
        if (userId == null) {
            throw new RuntimeException("Cannot find user id in controller context. Either you aren't logged in or you're doing this on a strange thread");
        }
        return userId;
    }

}
