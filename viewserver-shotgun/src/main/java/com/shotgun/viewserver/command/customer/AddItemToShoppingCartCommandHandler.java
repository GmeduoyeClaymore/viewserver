package com.shotgun.viewserver.command.customer;

import com.shotgun.viewserver.messages.customer.IAddItemtoShoppingCartMessage;
import io.viewserver.command.CommandHandlerBase;
import io.viewserver.command.CommandResult;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;

/**
 * Created by Gbemiga on 13/10/17.
 */
public class AddItemToShoppingCartCommandHandler extends CommandHandlerBase<IAddItemtoShoppingCartMessage> { {
}

    protected AddItemToShoppingCartCommandHandler(Class<IAddItemtoShoppingCartMessage> clazz) {
        super(clazz);
    }

    @Override
    protected void handleCommand(Command command, IAddItemtoShoppingCartMessage data, IPeerSession peerSession, CommandResult commandResult) {

    }

}
