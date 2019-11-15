package com.cordalo.template.contracts;

import ch.cordalo.corda.common.contracts.StateVerifier;
import com.cordalo.template.states.ChatMessageState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class ChatMessageContract implements Contract {
    public static final String ID = "com.cordalo.template.contracts.ChatMessageContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, ChatMessageContract.Commands.class);
        ChatMessageContract.Commands commandData = (ChatMessageContract.Commands)verifier.command();
        commandData.verify(tx, verifier);
    }

    public interface Commands extends CommandData {
        public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;

        /*
        @CordaSerializable
        public class Reference extends ReferenceContract.Commands.Reference<ChatMessageState> implements ChatMessageContract.Commands {
            public Reference(ChatMessageState myState) {
                super(myState);
            }

            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                this.verify(tx);
            }
        }
        */

        class Send implements ChatMessageContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().empty("input must be empty");
                    ChatMessageState message = verifier
                            .output()
                            .one()
                            .one(ChatMessageState.class)
                            .isNotEmpty(ChatMessageState::getLinearId, "id must be provided")
                            .isNotEmpty(ChatMessageState::getSender, "sender must be provided")
                            .isNotEmpty(ChatMessageState::getReceiver, "receiver must be provided")
                            .isNotEmpty(ChatMessageState::getMessage, "message cannot be empty")
                            .isNotEqual(ChatMessageState::getSender, ChatMessageState::getReceiver, "selfie-messages are not allowed")
                            .object();
                    return null;
                });
            }
        }

        class Reply implements ChatMessageContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    ChatMessageState message = verifier
                            .input()
                            .one()
                            .one(ChatMessageState.class)
                            .object();
                    ChatMessageState origMessage = verifier
                            .output()
                            .count(2)
                            .filterWhere(
                                x -> ((ChatMessageState)x).getLinearId().equals(message.getLinearId()))
                            .one()
                            .one(ChatMessageState.class)
                            .object();
                    req.using("input and output original message must identical", message.equals(origMessage));

                    ChatMessageState reply = verifier
                            .output()
                            .count(2)
                            .filterWhere(
                                    x -> message.getLinearId().equals(((ChatMessageState)x).getBaseMessageId()))
                            .one()
                            .one(ChatMessageState.class)
                            .isNotEmpty(ChatMessageState::getLinearId, "id must be provided")
                            .isNotEmpty(ChatMessageState::getSender, "sender must be provided")
                            .isNotEmpty(ChatMessageState::getReceiver, "receiver must be provided")
                            .isNotEmpty(ChatMessageState::getMessage, "message cannot be empty")
                            .isNotEqual(ChatMessageState::getSender, ChatMessageState::getReceiver, "selfie-messages are not allowed")
                            .object();
                    req.using("receiver must be sender", reply.getReceiver().equals(message.getSender()));
                    req.using("sender must be receiver", reply.getSender().equals(message.getReceiver()));
                    req.using("reply base id must be id from input", reply.getBaseMessageId().equals(message.getLinearId()));
                    return null;
                });
            }
        }


        class Delete implements ChatMessageContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.output().empty("output must be empty");
                    ChatMessageState message = verifier
                            .input()
                            .one()
                            .one(ChatMessageState.class)
                            .isNotEmpty(ChatMessageState::getLinearId, "id must be provided")
                            .isNotEmpty(ChatMessageState::getSender, "sender must be provided")
                            .isNotEmpty(ChatMessageState::getReceiver, "receiver must be provided")
                            .isNotEmpty(ChatMessageState::getMessage, "message cannot be empty")
                            .object();
                    return null;
                });
            }
        }


    }
}
