package ch.cordalo.template.contracts;

import ch.cordalo.corda.common.contracts.Permissions;

import static ch.cordalo.template.contracts.ServiceStateMachine.STATEMACHINE_NAME;

public class ServicePermissions extends Permissions {

    private final static ServicePermissions INSTANCE = new ServicePermissions();

    public final static ServicePermissions getInstance() {
        return INSTANCE;
    }

    public ServicePermissions() {
        super(STATEMACHINE_NAME);
    }

    @Override
    protected void initPermissions() {
        this.addStateActionsForRole("admin",
                "CREATE",
                "REGISTER",
                "INFORM",
                "CONFIRM",
                "TIMEOUT",
                "WITHDRAW",
                "NO_SHARE",
                "DUPLICATE",
                "SHARE",
                "SEND_PAYMENT",
                "DECLINE",
                "ACCEPT"
        );
    }

    @Override
    protected void initPartiesAndRoles() {
        this.addPartyAndRoles("O=Company-A,L=Zurich,ST=ZH,C=CH", "admin");
        this.addPartyAndRoles("O=Company-B,L=Winterthur,ST=ZH,C=CH", "admin");
        this.addPartyAndRoles("O=Company-C,L=Zug,ST=ZG,C=CH", "admin");
        this.addPartyAndRoles("O=Company-D,L=Geneva,ST=ZH,C=CH", "admin");
        this.addPartyAndRoles("O=Company-E,L=Uster,ST=ZH,C=CH", "admin");
    }

    @Override
    protected void initPartiesAndAttributes() {

    }
}
