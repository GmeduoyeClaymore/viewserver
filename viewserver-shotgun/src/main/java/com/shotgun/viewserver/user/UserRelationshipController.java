package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.IDatabaseUpdater;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.shotgun.viewserver.ControllerUtils.getUserId;

@Controller(name = "userRelationshipController")
public class UserRelationshipController {
    private static final Logger log = LoggerFactory.getLogger(UserRelationshipController.class);
    private IDatabaseUpdater iDatabaseUpdater;

    public UserRelationshipController(IDatabaseUpdater iDatabaseUpdater) {
        this.iDatabaseUpdater = iDatabaseUpdater;
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
                    .addValue("relationshipStatus", UserRelationshipStatus.REQUESTED.name());

            iDatabaseUpdater.addOrUpdateRow(TableNames.USER_RELATIONSHIP_TABLE_NAME, "userRelationship", relationshipRecord);
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
                    .addValue("relationshipStatus", status.name());

            iDatabaseUpdater.addOrUpdateRow(TableNames.USER_RELATIONSHIP_TABLE_NAME, "userRelationship", relationshipRecord);
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

            iDatabaseUpdater.addOrUpdateRow(TableNames.USER_RELATIONSHIP_TABLE_NAME, "userRelationship", relationshipRecord);
        } catch (Exception e) {
            log.error("There was a problem updating user relationship type", e);
            throw new RuntimeException(e);
        }
    }

}
