package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.authentication.AuthenticationToken;
import io.viewserver.authentication.IAuthenticationHandler;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.SubscribeReportHandler;
import io.viewserver.messages.command.IAuthenticateCommand;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.schema.column.ColumnHolderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class CompatableVersionAuthenticationCommand implements IAuthenticationHandler {

    ICatalog systemCatalog;
    ClientVersionInfo versionInfo;
    private static final Logger log = LoggerFactory.getLogger(CompatableVersionAuthenticationCommand.class);

    public CompatableVersionAuthenticationCommand(ICatalog systemCatalog, ClientVersionInfo versionInfo) {
        this.systemCatalog = systemCatalog;
        this.versionInfo = versionInfo;
    }

    @Override
    public AuthenticationToken authenticate(IAuthenticateCommand authenticateCommandDto) {
        KeyedTable table = (KeyedTable) systemCatalog.getOperatorByPath(TableNames.CLUSTER_TABLE_NAME);
        if(table == null){
            throw new RuntimeException("Cannot authenticate as no load balancer table");
        }

        List<String> compatableVersions = authenticateCommandDto.getTokens();
        log.info("Attempting to authenticate against client version - {}",String.join(",",compatableVersions));
        if(!compatableVersions.contains(versionInfo.getCompatableClientVersion())){
            IRowSequence rows = (table.getOutput().getAllRows());
            Integer noConnectionsOnAlternative = Integer.MAX_VALUE;
            String alternativeUrl = null;
            while(rows.moveNext()){
                String url = (String) ColumnHolderUtils.getColumnValue(table, "url", rows.getRowId());
                if(!url.equals(versionInfo.getServerEndPoint()) && !Boolean.TRUE.equals(ColumnHolderUtils.getColumnValue(table, "isOffline", rows.getRowId())) &&
                        compatableVersions.contains(ColumnHolderUtils.getColumnValue(table, "clientVersion", rows.getRowId()))){
                    Integer noConnections = (Integer) ColumnHolderUtils.getColumnValue(table, "noConnections", rows.getRowId());
                    if(noConnections < noConnectionsOnAlternative){
                        alternativeUrl = url;
                        noConnectionsOnAlternative = noConnections;
                    }
                }
            }
            HashMap result = new HashMap();
            if(alternativeUrl == null){
                String format = String.format("Client version %s is not compatable with server verison %s and could not find alternative", String.join(",", compatableVersions), versionInfo.getCompatableClientVersion());
                log.info(format);
                result.put("message", format);
            }else{
                String format = String.format("Client version %s is not compatable with server verison %s and found alternative %s", String.join(",", compatableVersions), versionInfo.getCompatableClientVersion(),alternativeUrl);
                log.info(format);
                result.put("alternative",alternativeUrl);
            }
            throw new RuntimeException(ControllerUtils.toString(result));
        }
        log.info("Successfully authenticated - {}",String.join(",",compatableVersions));
        return new AuthenticationToken(authenticateCommandDto.getType(),versionInfo.getCompatableClientVersion());
    }
}
