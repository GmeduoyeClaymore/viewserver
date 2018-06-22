package com.shotgun.viewserver.servercomponents;

import com.github.zafarkhaja.semver.Version;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.authentication.AuthenticationToken;
import io.viewserver.authentication.IAuthenticationHandler;
import io.viewserver.catalog.ICatalog;
import io.viewserver.messages.command.IAuthenticateCommand;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.schema.column.ColumnHolderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;

public class CompatibleVersionAuthenticationCommand implements IAuthenticationHandler {

    ICatalog systemCatalog;
    ClientVersionInfo versionInfo;
    private static final Logger log = LoggerFactory.getLogger(CompatibleVersionAuthenticationCommand.class);

    public CompatibleVersionAuthenticationCommand(ICatalog systemCatalog, ClientVersionInfo versionInfo) {
        this.systemCatalog = systemCatalog;
        this.versionInfo = versionInfo;
    }

    @Override
    public AuthenticationToken authenticate(IAuthenticateCommand authenticateCommandDto) {
        KeyedTable table = (KeyedTable) systemCatalog.getOperatorByPath(TableNames.CLUSTER_TABLE_NAME);
        if(table == null){
            throw new RuntimeException("Cannot authenticate as no load balancer table");
        }

        log.info("MILESTONE Attempting to authenticate - {}",authenticateCommandDto);

        Version clientVersion = Version.valueOf(authenticateCommandDto.getClientVersion());
        log.info("Attempting to authenticate against client version - {}", clientVersion.toString());

        if(!clientVersion.satisfies(versionInfo.getCompatibleClientVersion())){
            IRowSequence rows = (table.getOutput().getAllRows());
            Integer noConnectionsOnAlternative = Integer.MAX_VALUE;
            String alternativeUrl = null;
            while(rows.moveNext()){
                String url = (String) ColumnHolderUtils.getColumnValue(table, "url", rows.getRowId());
                if(url == null){
                    continue;
                }
                if(!url.equals(versionInfo.getServerEndPoint()) && !Boolean.TRUE.equals(ColumnHolderUtils.getColumnValue(table, "isOffline", rows.getRowId())) &&
                        clientVersion.satisfies((String)ColumnHolderUtils.getColumnValue(table, "clientVersion", rows.getRowId()))){
                    Integer noConnections = (Integer) ColumnHolderUtils.getColumnValue(table, "noConnections", rows.getRowId());
                    noConnections = noConnections == null ? 0 : noConnections;
                    if(noConnections < noConnectionsOnAlternative || (alternativeUrl != null && noConnections.equals(noConnectionsOnAlternative) && url.compareTo(alternativeUrl) < 0)) {
                        alternativeUrl = url;
                        noConnectionsOnAlternative = noConnections;
                    }
                }
            }
            HashMap result = new HashMap();
            if(alternativeUrl == null){
                String format = String.format("Client version %s is not compatible with server verison %s and could not find alternative", clientVersion.toString(), versionInfo.getCompatibleClientVersion());
                log.debug(format);
                result.put("message", format);
            }else{
                String format = String.format("Client version %s is not compatible with server verison %s and found alternative %s", clientVersion.toString(), versionInfo.getCompatibleClientVersion(),alternativeUrl);
                log.debug(format);
                result.put("alternative",alternativeUrl);
            }
            throw new RuntimeException(ControllerUtils.toString(result));
        }
        log.info("MILESTONE Successfully authenticated - {}", clientVersion.toString());
        return new AuthenticationToken(authenticateCommandDto.getType(),versionInfo.getCompatibleClientVersion());
    }
}
