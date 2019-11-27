package ch.cordalo.template.states.examples;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class OriginalCarContract implements Contract {
    public interface Commands extends CommandData {
        class Create extends TypeOnlyCommandData implements Commands {
        }

        class Update extends TypeOnlyCommandData implements Commands {
        }

        class Delete extends TypeOnlyCommandData implements Commands {
        }
    }

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();

        if (commandData.equals(new Commands.Create())) {

            requireThat(require -> {

                require.using("No inputs should be consumed when create a car.", tx.getInputStates().size() == 0);
                require.using("Only one output state should be created when create a car.", tx.getOutputStates().size() == 1);

                OriginalCarState outputState = tx.outputsOfType(OriginalCarState.class).get(0);
                require.using("A newly created car must have a valid make and is not empty.", outputState.make != null && !outputState.make.isEmpty());
                require.using("The creator and regulator cannot have the same identity.", outputState.getCreator().getOwningKey() != outputState.getRegulator().getOwningKey());

                List<PublicKey> signers = tx.getCommands().get(0).getSigners();
                HashSet<PublicKey> signersSet = new HashSet<>();
                for (PublicKey key : signers) {
                    signersSet.add(key);
                }

                List<AbstractParty> participants = tx.getOutputStates().get(0).getParticipants();
                HashSet<PublicKey> participantKeys = new HashSet<>();
                for (AbstractParty party : participants) {
                    participantKeys.add(party.getOwningKey());
                }

                require.using("Both owner and regulator together only may sign car creation transaction.", signersSet.containsAll(participantKeys) && signersSet.size() == 2);

                return null;
            });

        } else if (commandData.equals(new Commands.Update())) {
            // ...
        }
    }
}
