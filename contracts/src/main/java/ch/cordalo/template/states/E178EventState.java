package ch.cordalo.template.states;

import ch.cordalo.corda.common.states.CordaloLinearState;
import ch.cordalo.corda.common.states.Parties;
import ch.cordalo.template.contracts.E178EventContract;
import ch.cordalo.template.contracts.E178StateMachine;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@BelongsToContract(E178EventContract.class)
public class E178EventState extends CordaloLinearState {

    @JsonIgnore
    @Nullable
    private final Party regulator;

    @JsonIgnore
    @Nullable
    private final Party retailer;

    @JsonIgnore
    @Nullable
    private final Party leasing;

    @JsonIgnore
    @Nullable
    private final Party insurer;

    @NotNull
    private final String state;

    @NotNull
    private final String stammNr;

    @NotNull
    private final String status;

    @NotNull
    @JsonIgnore
    @Override
    protected Parties getParties() {
        return new Parties(
                this.retailer,
                this.leasing,
                this.regulator,
                this.insurer
        );
    }

    @ConstructorForDeserialization
    public E178EventState(@NotNull UniqueIdentifier linearId, String stammNr,
                          @Nullable Party retailer,
                          @Nullable Party leasing,
                          @Nullable Party insurer,
                          @Nullable Party regulator,
                          @NotNull String state, @NotNull String status) {
        super(linearId);
        this.stammNr = stammNr;
        this.retailer = retailer;
        this.leasing = leasing;
        this.insurer = insurer;
        this.regulator = regulator;
        this.state = state;
        this.status = status;
    }

    public E178EventState(@NotNull UniqueIdentifier linearId, String stammNr,
                          @Nullable Party retailer,
                          @Nullable Party leasing,
                          @Nullable Party insurer,
                          @Nullable Party regulator,
                          @NotNull String state, @NotNull E178StateMachine.State status) {
        this(linearId, stammNr, retailer, leasing, insurer, regulator, state, status.getValue());
    }

    @Nullable
    public Party getRegulator() {
        return regulator;
    }

    public String getRegulatorX500() {
        return Parties.partyToX500(this.getRegulator());
    }

    @Nullable
    public Party getRetailer() {
        return retailer;
    }

    public String getRetailerX500() {
        return Parties.partyToX500(this.getRetailer());
    }

    @Nullable
    public Party getLeasing() {
        return leasing;
    }

    public String getLeasingX500() {
        return Parties.partyToX500(this.getLeasing());
    }

    @Nullable
    public Party getInsurer() {
        return insurer;
    }

    public String getInsurerX500() {
        return Parties.partyToX500(this.getInsurer());
    }

    @NotNull
    public String getState() {
        return state;
    }

    @NotNull
    public String getStammNr() {
        return stammNr;
    }

    @NotNull
    public String getStatus() {
        return this.status;
    }

    @NotNull

    @JsonIgnore
    public E178StateMachine.State getStatusObject() {
        return E178StateMachine.State(this.status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        E178EventState that = (E178EventState) o;
        return this.getLinearId().equals(that.getLinearId()) &&
                getStammNr().equals(that.getStammNr()) &&
                Objects.equals(getRegulator(), that.getRegulator()) &&
                Objects.equals(getRetailer(), that.getRetailer()) &&
                Objects.equals(getLeasing(), that.getLeasing()) &&
                Objects.equals(getInsurer(), that.getInsurer()) &&
                getState().equals(that.getState()) &&
                getStatus() == that.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getLinearId(), getStammNr(), getRegulator(), getRetailer(), getLeasing(), getInsurer(), getState(), getStatus());
    }

    /* action */

    protected E178EventState changeState(E178StateMachine.State status) {
        return new E178EventState(this.getLinearId(), this.getStammNr(), this.getRetailer(), this.getLeasing(), this.getInsurer(), this.getRegulator(), this.getState(), status);
    }

    @NotNull
    @Contract("_, _, _, _ -> new")
    public static E178EventState request(String stammNr, Party retail, Party leasing, String state) {
        return new E178EventState(new UniqueIdentifier(), stammNr, retail, leasing, null, null, state, E178StateMachine.State("REQUESTED"));
    }

    public E178EventState issue(String state, Party regulator) {
        return new E178EventState(this.getLinearId(), this.getStammNr(), this.getRetailer(), this.getLeasing(), this.getInsurer(), regulator, state, E178StateMachine.State("ISSUED"));
    }

    public E178EventState issue(Party regulator) {
        return new E178EventState(this.getLinearId(), this.getStammNr(), this.getRetailer(), this.getLeasing(), this.getInsurer(), regulator, this.getState(), E178StateMachine.State("ISSUED"));
    }

    public E178EventState requestInsurance(Party insurer) {
        return new E178EventState(this.getLinearId(), this.getStammNr(), this.getRetailer(), this.getLeasing(), insurer, this.getRegulator(), this.getState(), E178StateMachine.State("INSURANCE_REQUESTED"));
    }

    public E178EventState insure() {
        return this.changeState(E178StateMachine.State("INSURED"));
    }

    public E178EventState registered() {
        return this.changeState(E178StateMachine.State("REGISTERED"));
    }

    public E178EventState cancel() {
        return this.changeState(E178StateMachine.State("CANCELED"));
    }

}
