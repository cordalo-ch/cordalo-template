package ch.cordalo.template.contracts;

import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.template.states.SignedDocument;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class SignedDocumentContract implements Contract {
    public static final String ID = "ch.cordalo.template.contracts.SignedDocumentContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, ChatMessageContract.Commands.class);
        SignedDocumentContract.Commands commandData = (SignedDocumentContract.Commands) verifier.command();
        commandData.verify(tx, verifier);
    }

    public interface Commands extends CommandData {
        public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;

        class Create implements SignedDocumentContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().empty("input must be empty");
                    SignedDocument document = verifier
                            .output()
                            .one()
                            .one(SignedDocument.class)
                            .isNotEmpty(SignedDocument::getLinearId, "linear id must be provided")
                            .isNotEmpty(SignedDocument::getOwner, "owner must be provided")
                            .isNotEmpty(SignedDocument::getSharedParties, "shared parties must be provided")
                            .isNotEmpty(SignedDocument::getChecksum, "checksum must be provided")
                            .isNotEmpty(SignedDocument::getReferenceId, "document reference id must be provided")
                            .object();
                    req.using("hash must be valid", SignedDocument.isValidHash(document.getChecksum()));
                    return null;
                });
            }
        }
    }
}
