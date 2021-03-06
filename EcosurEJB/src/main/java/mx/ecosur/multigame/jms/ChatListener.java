/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/


/**
 * Receives messages from the JMS queue, and adds ChatMessages into the 
 * system through the SharedBoard. 
 * 
 * @author max@alwayssunny.com
 */

package mx.ecosur.multigame.jms;

import java.util.logging.Logger;

import javax.ejb.*;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import mx.ecosur.multigame.ejb.interfaces.SharedBoardLocal;
import mx.ecosur.multigame.enums.GameEvent;
import mx.ecosur.multigame.model.interfaces.ChatMessage;

@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@MessageDriven(mappedName = "MultiGame", activationConfig = {
        @ActivationConfigProperty(
                propertyName = "messageSelector", propertyValue = "GAME_EVENT = 'CHAT'"),
        @ActivationConfigProperty(
                propertyName = "destinationType", propertyValue = "javax.jms.Topic")})
public class ChatListener implements MessageListener {

    private static final Logger logger = Logger.getLogger(ChatListener.class.getCanonicalName());

    @EJB
    private SharedBoardLocal sharedBoard;

    /**
     * Simple onMessage method appends the name of the Registrant that sent a
     * message, and the messages contents to the SharedBoard stream
     */
    public void onMessage(Message message) {
        ObjectMessage msg = (ObjectMessage) message;
        try {
            String gameEvent = message.getStringProperty("GAME_EVENT");
            if (gameEvent.equals(GameEvent.CHAT.name())) {
                ChatMessage chatMessage = (ChatMessage) msg.getObject();
                logger.finest(chatMessage.toString()); // Chat messages will show up with debug logging on (for testing)
                /* sharedBoard.addMessage(chatMessage); */ // removed due to privacy considerations (LGB)
                msg.acknowledge();
            }
        } catch (Exception e) {
            logger.warning("Not able to save game message: " + e.getMessage());
        }
    }
}
