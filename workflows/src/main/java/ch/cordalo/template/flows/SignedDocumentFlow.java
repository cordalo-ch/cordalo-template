/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.flows;

import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import ch.cordalo.corda.common.flows.SimpleBaseFlow;
import ch.cordalo.corda.common.flows.SimpleFlow;
import ch.cordalo.template.contracts.SignedDocumentContract;
import ch.cordalo.template.states.SignedDocument;
import co.paralleluniverse.fibers.Suspendable;
import kotlin.Unit;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

import java.security.DigestException;
import java.util.ArrayList;

import static ch.cordalo.template.states.SignedDocument.hashString;

public class SignedDocumentFlow {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Create extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Create<SignedDocument> {

        private final String content;
        private final String docRefId;
        private final String title;
        private final String mimeType;

        public Create(String content, String docRefId, String title, String mimeType) {
            this.content = content;
            this.docRefId = docRefId;
            this.title = title;
            this.mimeType = mimeType;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Create(this, new SignedDocumentContract.Commands.Create());
        }

        @Override
        @Suspendable
        public SignedDocument create() throws FlowException {
            try {
                return new SignedDocument(
                        new UniqueIdentifier(), this.getOurIdentity(), hashString(this.content), this.docRefId, this.title, this.mimeType, null, new ArrayList<>());
            } catch (DigestException e) {
                throw new FlowException("Error in hashing document");
            }
        }
    }

    @InitiatedBy(SignedDocumentFlow.Create.class)
    public static class CreateResponder extends ResponderBaseFlow<SignedDocument> {

        public CreateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

}
