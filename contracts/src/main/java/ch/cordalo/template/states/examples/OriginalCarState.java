/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.states.examples;

import ch.cordalo.template.contracts.ChatMessageContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@BelongsToContract(ChatMessageContract.class)
public class OriginalCarState implements LinearState {
    UniqueIdentifier linearId;
    String make;
    private final Party creator;
    private final Party holder;
    private final Party regulator;
    private final List<Party> owners;

    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> list = new ArrayList<>();
        if (creator != null) list.add(creator);
        if (owners != null) list.addAll(owners);
        return list;
    }

    @ConstructorForDeserialization
    public OriginalCarState(@NotNull UniqueIdentifier linearId, String make, Party creator, Party holder, Party regulator, List<Party> owners) {
        this.linearId = linearId;
        this.make = make;
        this.creator = creator;
        this.holder = holder;
        this.regulator = regulator;
        this.owners = owners;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.linearId;
    }

    public String getMake() {
        return make;
    }

    public Party getCreator() {
        return creator;
    }

    public Party getHolder() {
        return holder;
    }

    public Party getRegulator() {
        return regulator;
    }

    public List<Party> getOwners() {
        return owners;
    }
}
