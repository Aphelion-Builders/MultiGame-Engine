/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/


/**
 * The SharedBoardEJB handles operations between players and the shared game
 * board.  The SharedBoardEJB manges game specific events, such as validating a
 * specific move on a game board, making a specific move, and modifying a 
 * previous move.  Clients can also add chat messages to the message stream,
 * increment players turns (soon to be phased into the game rules), and get
 * a list of players for a specific game.
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.ejb.impl;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;

import mx.ecosur.multigame.ejb.interfaces.SharedBoardLocal;
import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
import mx.ecosur.multigame.enums.SuggestionStatus;
import mx.ecosur.multigame.exception.InvalidMoveException;

import mx.ecosur.multigame.enums.MoveStatus;

import mx.ecosur.multigame.exception.InvalidSuggestionException;
import mx.ecosur.multigame.grid.entity.GridGame;
import mx.ecosur.multigame.model.interfaces.*;
import mx.ecosur.multigame.MessageSender;

@Stateless
@RolesAllowed("admin")
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class SharedBoard implements SharedBoardLocal, SharedBoardRemote {


    private static final Logger logger;

    static {
        logger = Logger.getLogger(SharedBoard.class.getCanonicalName());
    }

    private MessageSender messageSender;
        
    @PersistenceContext (unitName = "MultiGamePU")
    EntityManager em;

    public SharedBoard () throws InstantiationException, IllegalAccessException,
            ClassNotFoundException
    {
        super();
        messageSender = new MessageSender();
        messageSender.initialize();
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.ejb.SharedBoardLocal#getGame(int)
     */
    public Game getGame(int gameId) {
        return em.find(GridGame.class, gameId);
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.ejb.SharedBoardRemote#move(mx.ecosur.multigame.entity.Move)
     */
    public Move doMove(Game game, Move move) throws InvalidMoveException {
        if (game.getId() == 0)
            throw new InvalidMoveException ("Move requested by unknown game (id = 0)!");

        GamePlayer player = move.getPlayerModel();
        Cell current = move.getCurrentCell();
        Cell destination = move.getDestinationCell();

        if (move.getId() == 0) {
            move.setPlayerModel(null);
            move.setCurrentCell(null);
            move.setDestinationCell(null);
            /* Persist clean move with no detached, dependent entities */
            em.persist(move);

            move.setPlayerModel(player);
            move.setDestinationCell(destination);
            move.setCurrentCell(current);
        }

        move = em.merge(move);
        game = em.find(game.getClass(), game.getId());

        /* Now that entities are managed, execute rules on move and game */
        game.setMessageSender(messageSender);
        game.move (move);
        if (move.getStatus().equals(MoveStatus.INVALID))
            throw new InvalidMoveException ("INVALID Move. [" + move.toString() + "]");
        return move;
    }


    public Suggestion makeSuggestion(Game game, Suggestion suggestion) throws InvalidSuggestionException {
        Move move = suggestion.listMove();
        GamePlayer suggestor = suggestion.listSuggestor();
        GamePlayer player = move.getPlayerModel();
        Cell current = move.getCurrentCell();
        Cell destination = move.getDestinationCell();

        if (suggestion.getId() == 0) {
            suggestion.attachSuggestor(null);
            suggestion.attachMove(null);
            em.persist(suggestion);
            if (move.getId() == 0) {
                move.setPlayerModel(null);
                move.setCurrentCell(null);
                move.setDestinationCell(null);
                /* Persist clean move with no detached, dependent entities */
                em.persist(move);
                move.setPlayerModel(player);
                move.setDestinationCell(destination);
                move.setCurrentCell(current);
            }

            suggestion.attachSuggestor(suggestor);
            suggestion.attachMove(move);
        }

        suggestion = em.merge(suggestion);
        move  = em.merge(move);
        suggestion.attachMove(move);
        game = em.find(game.getClass(), game.getId());
        game.setMessageSender(messageSender);
        suggestion = game.suggest(suggestion);
        if (suggestion.getStatus().equals(SuggestionStatus.INVALID))
            throw new InvalidSuggestionException ("INVALID Move suggested!");
        return suggestion;
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.ejb.SharedBoardRemote#getMoves(int)
     */
    public Collection<Move> getMoves(int gameId) {
        Game game = getGame(gameId);
        return game.listMoves();
    }

    public ChatMessage addMessage(ChatMessage chatMessage) {
        return em.merge (chatMessage);
    }

    /* (non-Javadoc)
     * @see mx.ecosur.multigame.ejb.interfaces.SharedBoardInterface#updateMove(mx.ecosur.multigame.entity.Move)
     */
    public Move updateMove(Move move) {
        return em.merge(move);
    }
}
