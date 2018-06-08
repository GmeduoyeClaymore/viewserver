package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.authentication.AuthenticationToken;
import io.viewserver.authentication.IAuthenticationHandler;
import io.viewserver.catalog.ICatalog;
import io.viewserver.messages.command.IAuthenticateCommand;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.schema.column.ColumnHolderUtils;

import java.util.HashMap;
import java.util.List;

public class CompatableVersionEvenlyDistributedAuthenticationCommand implements IAuthenticationHandler {

        ICatalog systemCatalog;
        private ClientVersionInfo versionInfo;


        public CompatableVersionEvenlyDistributedAuthenticationCommand(ICatalog systemCatalog, ClientVersionInfo versionInfo) {
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
            IRowSequence rows = (table.getOutput().getAllRows());
            Integer myNoConnections = compatableVersions.contains(versionInfo.getCompatableClientVersion()) ? (Integer)ColumnHolderUtils.getColumnValue(table, "noConnections",versionInfo.getServerEndPoint()) : Integer.MAX_VALUE;

            String alternativeUrl = null;
            while(rows.moveNext()){
                String url = (String) ColumnHolderUtils.getColumnValue(table, "url", rows.getRowId());
                if(!url.equals(versionInfo.getServerEndPoint()) && !Boolean.TRUE.equals(ColumnHolderUtils.getColumnValue(table, "isOffline", rows.getRowId())) && compatableVersions.contains(ColumnHolderUtils.getColumnValue(table, "clientVersion", rows.getRowId()))){
                    Integer noConnections = (Integer) ColumnHolderUtils.getColumnValue(table, "noConnections", rows.getRowId());
                    if(noConnections < myNoConnections){
                        alternativeUrl = url;
                        myNoConnections = noConnections;
                    }
                }
            }
            if(alternativeUrl == null && myNoConnections != Integer.MAX_VALUE) {
                return new AuthenticationToken(authenticateCommandDto.getType(), versionInfo.getCompatableClientVersion());
            }

            HashMap result = new HashMap();
            if(alternativeUrl == null){
                result.put("message",String.format("Client version %s is not compatable with server verison %s and could not find alternative",String.join(",",compatableVersions),versionInfo.getCompatableClientVersion()));
            }else{
                result.put("alternative",alternativeUrl);
            }
            throw new RuntimeException(ControllerUtils.toString(result));
        }

}
