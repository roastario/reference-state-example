package com.example.flow

import co.paralleluniverse.fibers.Suspendable
import com.example.flow.GetSanctionsListFlow.Acceptor
import com.example.flow.GetSanctionsListFlow.Initiator
import com.example.state.SanctionedEntities
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party

/**
 * This flow allows two parties (the [Initiator] and the [Acceptor]) to come to an agreement about the IOU encapsulated
 * within an [IOUState].
 *
 * In our simple example, the [Acceptor] always accepts a valid IOU.
 *
 * These flows have deliberately been implemented by using only the call() method for ease of understanding. In
 * practice we would recommend splitting up the various stages of the flow into sub-routines.
 *
 * All methods called within the [FlowLogic] sub-class need to be annotated with the @Suspendable annotation.
 */
object GetSanctionsListFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val otherParty: Party) : FlowLogic<List<StateAndRef<SanctionedEntities>>>() {

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        override fun call(): List<StateAndRef<SanctionedEntities>> {
            val session = initiateFlow(otherParty)
            val newestSanctionsList = subFlow(ReceiveStateAndRefFlow<SanctionedEntities>(session))
            return newestSanctionsList
        }
    }

    @InitiatedBy(Initiator::class)
    class Acceptor(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            subFlow(
                SendStateAndRefFlow(
                    otherPartySession,
                    serviceHub.vaultService.queryBy(SanctionedEntities::class.java).states
                )
            )
        }
    }
}
