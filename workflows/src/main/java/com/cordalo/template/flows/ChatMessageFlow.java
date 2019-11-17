package com.cordalo.template.flows;

import ch.cordalo.corda.common.flows.BaseFlow;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import ch.cordalo.corda.common.flows.SimpleBaseFlow;
import ch.cordalo.corda.common.flows.SimpleFlow;
import co.paralleluniverse.fibers.Suspendable;
import com.cordalo.template.contracts.ChatMessageContract;
import com.cordalo.template.states.ChatMessageState;
import kotlin.Unit;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.internal.Emoji;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Random;

public class ChatMessageFlow {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Send extends SimpleBaseFlow implements SimpleFlow.Create<ChatMessageState> {

        private final Party to;
        private final String message;

        public Send(Party to, String message) {
            this.to = to;
            this.message = message;
        }
        public Send(Party to) {
            this(to, ChatMessageFlow.randomMessage());
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            Party me = getOurIdentity();
            if (me.equals(this.to)) {
                throw new FlowException("message sender and receiver must be different");
            }
            return this.simpleFlow_Create(this, new ChatMessageContract.Commands.Send());
        }

        @Override
        @Suspendable
        public ChatMessageState create() {
            return new ChatMessageState(
                    new UniqueIdentifier(), this.getOurIdentity(),  this.to, this.message);
        }
    }

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Reply extends SimpleBaseFlow implements SimpleFlow.UpdateBuilder<ChatMessageState> {

        private final UniqueIdentifier id;
        private final String message;

        public Reply(UniqueIdentifier id, String message) {
            this.id = id;
            this.message = message;
        }
        public Reply(UniqueIdentifier id) {
            this(id, ChatMessageFlow.randomMessage());
        }


        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_UpdateBuilder(
                    ChatMessageState.class,
                    this.id,
                    this,
                    new ChatMessageContract.Commands.Reply());
        }

        @Override
        @Suspendable
        public ChatMessageState update(ChatMessageState state) throws FlowException {
            if (!state.getReceiver().equals(this.getOurIdentity())) {
                throw new FlowException("reply message must be send from message receiver");
            }
            return state.reply(this.message);
        }

        @Override
        public void updateBuilder(TransactionBuilder transactionBuilder, StateAndRef<ChatMessageState> stateRef, ChatMessageState state, ChatMessageState newState) throws FlowException {
            transactionBuilder.addInputState(stateRef);
            transactionBuilder.addOutputState(state);
            transactionBuilder.addOutputState(newState);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Delete extends SimpleBaseFlow implements SimpleFlow.Delete<ChatMessageState> {

        private final UniqueIdentifier id;
        public Delete(UniqueIdentifier id) {
            this.id = id;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Delete(
                    ChatMessageState.class, this.id,
                    this,
                    new ChatMessageContract.Commands.Delete());
        }

        @Override
        public void validateToDelete(ChatMessageState state) throws FlowException {

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

    private static final String[] messageOfTheDay = {
            "The only distributed ledger that pays\nhomage to Pac Man in its logo.",
            "You know, I was a banker\nonce ... but I lost interest.",
            "It's not who you know, it's who you know\nknows what you know you know.",
            "It runs on the JVM because QuickBasic\nis apparently not 'professional' enough.",
            "\"It's OK computer, I go to sleep after\ntwenty minutes of inactivity too!\"",
            "It's kind of like a block chain but\ncords sounded healthier than chains.",
            "Computer science and finance together.\nYou should see our crazy Christmas parties!",
            "I met my bank manager yesterday and asked\nto check my balance ... he pushed me over!",
            "A banker left to their own devices may find\nthemselves .... a-loan! <applause>",
            "Whenever I go near my bank\nI get withdrawal symptoms",
            "There was an earthquake in California,\na local bank went into de-fault.",
            "I asked for insurance if the nearby\nvolcano erupted. They said I'd be covered.",
            "I had an account with a bank in the\nNorth Pole, but they froze all my assets",
            "Check your contracts carefully. The fine print\nis usually a clause for suspicion",
            "Some bankers are generous ...\nto a vault!",
            "What you can buy for a dollar these\ndays is absolute non-cents!",
            "Old bankers never die, they\njust... pass the buck",
            "I won $3M on the lottery so I donated a quarter\nof it to charity. Now I have $2,999,999.75.",
            "There are two rules for financial success:\n1) Don't tell everything you know.",
            "Top tip: never say \"oops\", instead\nalways say \"Ah, Interesting!\"",
            "Computers are useless. They can only\ngive you answers.  -- Picasso",
            "Regular naps prevent old age, especially\nif you take them whilst driving.",
            "Always borrow money from a pessimist.\nHe won't expect it back.",
            "War does not determine who is right.\nIt determines who is left.",
            "A bus stops at a bus station. A train stops at a\ntrain station. What happens at a workstation?",
            "I got a universal remote control yesterday.\nI thought, this changes everything.",
            "Did you ever walk into an office and\nthink, whiteboards are remarkable!",
            "The good thing about lending out your time machine\nis that you basically get it back immediately.",
            "I used to work in a shoe recycling\nshop. It was sole destroying.",
            "What did the fish say\nwhen he hit a wall? Dam.",
            "You should really try a seafood diet.\nIt's easy: you see food and eat it.",
            "I recently sold my vacuum cleaner,\nall it was doing was gathering dust.",
            "My professor accused me of plagiarism.\nHis words, not mine!",
            "Change is inevitable, except\nfrom a vending machine.",
            "If at first you don't succeed, destroy\nall the evidence that you tried.",
            "If at first you don't succeed, \nthen we have something in common!",
            "Moses had the first tablet that\ncould connect to the cloud.",
            "How did my parents fight boredom before the internet?\nI asked my 17 siblings and they didn't know either.",
            "Cats spend two thirds of their lives sleeping\nand the other third making viral videos.",
            "The problem with troubleshooting\nis that trouble shoots back.",
            "I named my dog 'Six Miles' so I can tell\npeople I walk Six Miles every day.",
            "People used to laugh at me when I said I wanted\nto be a comedian. Well they're not laughing now!",
            "My wife just found out I replaced our bed\nwith a trampoline; she hit the roof.",
            "My boss asked me who is the stupid one, me or him?\nI said everyone knows he doesn't hire stupid people.",
            "Don't trust atoms.\nThey make up everything.",
            "Keep the dream alive:\nhit the snooze button.",
            "Rest in peace, boiled water.\nYou will be mist.",
            "When I discovered my toaster wasn't\nwaterproof, I was shocked.",
            "Where do cryptographers go for\nentertainment? The security theatre.",
            "How did the Java programmer get rich?\nThey inherited a factory.",
            "Why did the developer quit his job?\nHe didn't get ar-rays.",
            "Quantum computer jokes are both\n funny and not funny at the same time."};
    public static String randomJoke() {
        return jokes[new Random().nextInt(jokes.length)];
    }
    public static String randomMessageOfTheDay() {
        return messageOfTheDay[new Random().nextInt(messageOfTheDay.length)];
    }

    public static String randomMessage() {
        return randomMessageOfTheDay();
    }
}
