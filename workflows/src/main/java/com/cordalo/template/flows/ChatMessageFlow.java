package com.cordalo.template.flows;

import ch.cordalo.corda.common.flows.BaseFlow;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import co.paralleluniverse.fibers.Suspendable;
import com.cordalo.template.contracts.ChatMessageContract;
import com.cordalo.template.states.ChatMessageState;
import kotlin.Unit;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Random;

public class ChatMessageFlow {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Send extends BaseFlow {

        private final Party to;
        private final String message;

        public Send(Party to, String message) {
            this.to = to;
            this.message = message;
        }
        public Send(Party to) {
            this(to, ChatMessageFlow.randomJoke());
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return super.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            Party me = getOurIdentity();
            if (me.equals(this.to)) {
                throw new FlowException("message sender and receiver must be different");
            }

            getProgressTracker().setCurrentStep(PREPARATION);
            ChatMessageState message = new ChatMessageState(
                    new UniqueIdentifier(), me,  this.to, this.message);

            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    message,
                    new ChatMessageContract.Commands.Send());
            transactionBuilder.addOutputState(message);

            return signSyncCollectAndFinalize(message.getReceiver(), transactionBuilder);
        }
    }

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Reply extends BaseFlow {

        private final UniqueIdentifier id;
        private final String message;

        public Reply(UniqueIdentifier id, String message) {
            this.id = id;
            this.message = message;
        }
        public Reply(UniqueIdentifier id) {
            this(id, ChatMessageFlow.randomJoke());
        }


        @Override
        public ProgressTracker getProgressTracker() {
            return super.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            Party me = getOurIdentity();

            getProgressTracker().setCurrentStep(PREPARATION);
            StateAndRef<ChatMessageState> messageStateRef = this.getLastStateByLinearId(ChatMessageState.class, this.id);
            ChatMessageState messageState = this.getStateByRef(messageStateRef);
            if (!messageState.getReceiver().equals(me)) {
                throw new FlowException("reply message must be send from message receiver");
            }
            ChatMessageState replyMessage = messageState.reply(this.message);

            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    replyMessage,
                    new ChatMessageContract.Commands.Reply());
            transactionBuilder.addInputState(messageStateRef);
            transactionBuilder.addOutputState(messageState);
            transactionBuilder.addOutputState(replyMessage);

            return signSyncCollectAndFinalize(replyMessage.getParticipants(), transactionBuilder);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Delete extends BaseFlow {

        private final UniqueIdentifier id;

        public Delete(UniqueIdentifier id) {
            this.id = id;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return super.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            Party me = getOurIdentity();

            getProgressTracker().setCurrentStep(PREPARATION);
            StateAndRef<ChatMessageState> messageStateRef = this.getLastStateByLinearId(ChatMessageState.class, this.id);
            ChatMessageState messageState = this.getStateByRef(messageStateRef);

            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    messageState,
                    new ChatMessageContract.Commands.Delete());
            transactionBuilder.addInputState(messageStateRef);

            return signSyncCollectAndFinalize(messageState.getParticipants(), transactionBuilder);
        }
    }

    @InitiatedBy(ChatMessageFlow.Send.class)
    public static class SendResponder extends ResponderBaseFlow<ChatMessageState> {

        public SendResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }


    @InitiatedBy(ChatMessageFlow.Reply.class)
    public static class ReplyResponder extends ResponderBaseFlow<ChatMessageState> {

        public ReplyResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

    @InitiatedBy(ChatMessageFlow.Delete.class)
    public static class DeleteResponder extends ResponderBaseFlow<ChatMessageState> {

        public DeleteResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }


    private static final String[] jokes = {
            "Ich hasse warten. Egal wann, egal wie lange, egal wo, egal worauf.",
            "Auf meinem Grabstein soll stehen: 'Ich wäre jetzt auch lieber am Strand!'",
            "Wir leben für die Nächte an die sich keiner erinnert - mit Leuten die wir nie vergessen werden.",
            "Hab mich in diese Schuhe verliebt und jetzt wohnen wir zusammen :-)",
            "Du hast das Gaspedal mitbezahlt ... . Also benutz es auch....",
            "Woher soll ich wissen was ich denke, bevor ich höre was ich sage",
            "Wenn dir das Leben eine Zitrone gibt, frag nach Salz & Tequila",
            "Für eine Mindest-Geh-Geschwindigkeit in der Fußgängerzone",
            "Warum liegt hier alles auf dem Boden?? Schwerkraft, Mama.",
            "Mein Sport am Morgen ist der Sprint zum Bus.",
            "80 Prozent meines Lernaufwandes ist organisierter Selbstbetrug!",
            "Im Niveau flexibel...",
            "Keiner weiß was er kann aber alle nennen ihn Chef.. ;)",
            "Mein Handy kennt mehr Leute als ich...!",
            "Ich bin nicht pervers! Ihr redet einfach zweideutig!",
            "Wie können die 2 Bier von gestern 50€ gekostet haben?!",
            "Das CHAOS verfolgt mich- ich kann nix dafür!!!",
            "Wir lästern nicht, wir stellen nur fest!!",
            "Ich habe dir vertraut. Mein Fehler",
            "Heute trag´ ich mal ein fröhliches schwarz",
            "Mein Gewissen ist rein! Denn ich habe es nie benutzt!",
            "Ja verdammt: Meine Eltern wohnen noch bei mir!",
            "Wenn ich 18 bin, klär ich meine Eltern über meine Jugend auf!",
            "Bei 3 ist das Gemüse von meinem Grill, sonst raste ich aus!!!",
            "Liebe ist nur ein Wort, Quark auch.",
            "Pubertät ist wenn die Eltern anfangen komisch zu werden",
            "Lache nie über jemanden, der einen Schritt zurück macht - er könnte Anlauf nehmen!",
            "Ich brauch mich nicht in den Mittelpunkt zu stellen, meine Taten reichen schon dafür",
            "Ich komme dir vielleicht entgegen aber ich renn dir ganz bestimmt nicht hinterher",
            "Mein Leben war einfacher, als ich Jungs noch doof fand",
            "Ich hasse Leute, die meinen man verbringe zu wenig Zeit miteinander, einen aber zu nichts miteinladen!",
            "Na, mal wieder zu cool um 'Hallo' zu sagen?",
            "Ich war schon im Kindergarten eine Legende.",
            "Wenn die Männer so weitermachen, heirate ich meine beste Freundin!",
            "Auch Du wirst das tun, denn Du bist selber ziemlich gaga.",
            "Ich bin nicht dumm! ... Du sollst nur nicht alles wissen was ich weiß.",
            "Wir sind nicht betrunken, wir sind von Natur aus laut, lustig und ungeschickt :)",
            "Dich mag ich! Du hast genauso einen an der Klatsche wie ich :-)",
            "Die Stimmen in meinem Kopf versichern mir, dass du vollkommen normal bist.",
            "Aber ein Meteoriten-Einschlag kommt häufiger vor ...",
            "Ich bin nicht wie die anderen — ich bin schlimmer",
            "An die Person die meine Schuhe versteckt hat, während ich auf der Hüpfburg war: Werd Erwachsen!",
            "Mein Bett & Ich lieben uns — Mein Wecker akzeptiert das nicht.",
            "Wenn Du mit Gott sprechen willst, dann bete.", "Willst Du ihm begegnen, dann schreibe eine SMS am Steuer",
            "Gebet: Lieber Gott, erhalte mir meine guten Ausreden.",
            "Lieber nackt aus dem Haus ... als ohne Handy. [Tina Turner]"};

    public static String randomJoke() {
        return jokes[new Random().nextInt(jokes.length)];
    }
}
